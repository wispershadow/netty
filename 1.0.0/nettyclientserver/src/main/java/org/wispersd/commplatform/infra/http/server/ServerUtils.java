package org.wispersd.commplatform.infra.http.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wispersd.commplatform.infra.http.HttpConstants;
import org.wispersd.commplatform.infra.http.HttpResponseEntity;

public class ServerUtils {
	private static final Logger logger = LoggerFactory.getLogger(ServerUtils.class);
	
	private static final String CHANNELATTR_RESPQUEUE = "RESP_QUEUE";
	
	public static void createResponse(Channel channel, HttpResponseEntity respEntity, boolean keepAlive) {
		try {
			String content = respEntity.getBody();
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(content.getBytes(CharsetUtil.UTF_8)));
			
			response.headers().set(HttpHeaders.Names.CONTENT_TYPE, HttpConstants.JSON_CONTENTTYPE);
			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
			
			if (!keepAlive) {
			    channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			} else {
			    response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			    channel.writeAndFlush(response);
			}
		} catch (Exception e) {
			logger.error("Error creating normal response", e);
		}
	}
	
	
	public static void createErrorResponse(Channel channel, String errorMessage) {
		try {
			ByteBuf contentBuf = Unpooled.copiedBuffer(errorMessage.getBytes(CharsetUtil.UTF_8));
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, contentBuf);
			response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, contentBuf.readableBytes());
			channel.writeAndFlush(response);
		} catch (Exception e) {
			logger.error("Error creating error response", e);
		}
	}
	
	
	public static BlockingQueue<HttpResponseEntity> getResponseQueueForChannel(Channel channel, int size) {
		AttributeKey<BlockingQueue<HttpResponseEntity>> respQueueKey = AttributeKey.valueOf(CHANNELATTR_RESPQUEUE);
		Attribute<BlockingQueue<HttpResponseEntity>> respQueueVal = channel.attr(respQueueKey);
		BlockingQueue<HttpResponseEntity> existing = respQueueVal.get();
		if (existing != null) {
			return existing;
		}
		else {
			BlockingQueue<HttpResponseEntity> newQueue = new ArrayBlockingQueue<HttpResponseEntity>(size);
			existing = respQueueVal.getAndSet(newQueue);
			if (existing != null) {
				return existing;
			}
			else {
				return newQueue;
			}
		}
	}
}
