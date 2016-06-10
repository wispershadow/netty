package org.wispersd.commplatform.infra.http.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wispersd.commplatform.infra.http.HttpResponseEntity;
import org.wispersd.commplatform.infra.http.HttpUtils;
import org.wispersd.commplatform.infra.http.client.pool.ResourcePool;

public class NettyHttpClientHandler extends SimpleChannelInboundHandler<HttpObject>{
	private static final Logger logger = LoggerFactory.getLogger(NettyHttpClientHandler.class);
	private volatile Channel channel;
	private HttpResponseEntity curRespEntity;
	
	private final ResourcePool<Channel> nettyChannelPool;
	private final StringBuilder curHttpContentBuffer = new StringBuilder();
	private final BlockingQueue<HttpResponseEntity> httpRespQueue = new ArrayBlockingQueue<HttpResponseEntity>(1);
	
	
	public NettyHttpClientHandler(ResourcePool<Channel> nettyChannelPool) {
		this.nettyChannelPool = nettyChannelPool;
	}

	public HttpResponseEntity exchange(HttpRequest request) {
		channel.writeAndFlush(request);
		return getResponse();
	}
	
	public HttpResponseEntity exchange(HttpPostRequestWrapper postReqWrapper) {
		if (logger.isDebugEnabled()) {
			logger.debug("writing http request");
		}
		channel.write(postReqWrapper.getRequest());
		if (postReqWrapper.getContent() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("writing http content");
			}
			channel.write(postReqWrapper.getContent());
		}
		else if (postReqWrapper.getEncoder() != null) {
			if (postReqWrapper.getEncoder().isChunked()) {
				if (logger.isDebugEnabled()) {
					logger.debug("writting http encoder");
				}
				channel.write(postReqWrapper.getEncoder());
			}
		}
		channel.flush();
		/*
		 * it seems clear file here will cause problem, multipart data will be missing
		if (postReqWrapper.getEncoder() != null) {
			postReqWrapper.getEncoder().cleanFiles();
		}*/
		return getResponse();
	}
	
	
	protected HttpResponseEntity getResponse() {
		boolean interrupted = false;
		try {
			for (;;) {
				try {
					return httpRespQueue.take();
				} catch (InterruptedException ignore) {
					interrupted = true;
				}
			}
		} finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		channel = ctx.channel();
	}	
	

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg)
			throws Exception {
		if (msg instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) msg;
			if (logger.isDebugEnabled()) {
				logger.debug("Received http response: {}", response);
			}
            curRespEntity = new HttpResponseEntity();
            curRespEntity.setHeaders(response.headers());
            curRespEntity.setStatusCode(response.getStatus());
		}
		if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            if (logger.isDebugEnabled()) {
            	logger.debug("Received http content: {}", content);
			}
            HttpUtils.readByteBuf(content.content(), curHttpContentBuffer);
            if (content instanceof LastHttpContent) {
            	curRespEntity.setBody(curHttpContentBuffer.toString());
            	if (logger.isInfoEnabled()) {
            		logger.info("Received complete http repsonse: {}", curRespEntity);
            	}
            	if (!httpRespQueue.offer(curRespEntity)) {
            		logger.warn("Unable to put response in queue, previous response is not taken yet!");
            	}
				if (toCloseConnection(curRespEntity)) {
					ctx.close();
					nettyChannelPool.releaseResource(channel, true);
				}
				else {
					nettyChannelPool.releaseResource(channel, false);
				}
				reset();
            }
		}    
	}
	
	private static boolean toCloseConnection(HttpResponseEntity respEntity) {
		if (respEntity.getHeaders() == null) {
			return false;
		}
		return  HttpHeaders.Values.CLOSE.equalsIgnoreCase(respEntity.getHeaders().get(HttpHeaders.Names.CONNECTION));
	}
	
	
	private void reset() {
		curRespEntity = null;
		curHttpContentBuffer.setLength(0);
	}

	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error("Error caught when reading http data", cause);
		if (curRespEntity != null) {
			curRespEntity.setException(cause);
		}
		ctx.close();
    }
}
