package org.wispersd.commplatform.infra.http.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wispersd.commplatform.infra.http.HttpRequestEntity;
import org.wispersd.commplatform.infra.http.HttpResponseEntity;


public abstract class AbstractNettyHttpServerHandler extends ChannelInboundHandlerAdapter{
	private static final Logger logger = LoggerFactory.getLogger(AbstractNettyHttpServerHandler.class);
	private final RequestProcessContext requestProcessContext;
	private final RequestProcessorFactory requestProcessorFactory;
	private RequestProcessor curProcessor;
	
	
	public AbstractNettyHttpServerHandler(RequestProcessorFactory requestProcessorFactory) {
		this.requestProcessContext = new RequestProcessContext();
		this.requestProcessorFactory = requestProcessorFactory;
	}

	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) msg;
			if (logger.isDebugEnabled()) {
				logger.debug("Receiving http request: {}", request);
			}
			curProcessor = requestProcessorFactory.getRequestProcessorForReq(request);
			if (curProcessor == null) {
				logger.error("Unable to create request processor, creating error response");
				this.createErrorResponse(ctx.channel(), "");
				ctx.channel().close();
				return;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Obtaining request processor: {}", curProcessor.getClass().getName());
			}
			try {
				curProcessor.onRequestReceived(requestProcessContext, request);
			} catch (Exception ex) {
				logger.error("Unable to process http request", ex);
				requestProcessContext.clear();
				curProcessor = null;
				this.createErrorResponse(ctx.channel(), "");
				ctx.channel().close();
				return;
			}
		}
		else if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;
			if (logger.isDebugEnabled()) {
				logger.debug("Receiving http content: {}", content);
			}
			try {
				curProcessor.onContentReceived(requestProcessContext, content);
			} catch (Exception ex) {
				logger.error("Unable to process http content", ex);
				requestProcessContext.clear();
				curProcessor = null;
				this.createErrorResponse(ctx.channel(), "");
				ctx.channel().close();
				return;
			}
		}
		
		if (curProcessor.hasRequestCompleted(requestProcessContext)) {
			try {
				HttpRequestEntity reqEntity = curProcessor.convertRequest(requestProcessContext);
				processRequest(reqEntity, ctx.channel(), requestProcessContext.getRequest());
			} catch (Exception ex) {
				logger.error("Unable to convert and dispatch request", ex);
				this.createErrorResponse(ctx.channel(), "");
				ctx.channel().close();
			}
			finally {
				requestProcessContext.clear();
				curProcessor = null;
			}
		}
	}
	
	protected abstract void processRequest(HttpRequestEntity reqEntity, Channel channel, HttpRequest request);
	
	
	protected void createErrorResponse(Channel channel, String errorMessage) {
		ServerUtils.createErrorResponse(channel, errorMessage);
	}
	
	
	protected void createResponse(Channel channel, HttpRequest request, HttpResponseEntity respEntity) {
		boolean keepAlive = HttpHeaders.isKeepAlive(request);
		ServerUtils.createResponse(channel, respEntity, keepAlive);
	}
    

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.error("Error caught in server handler", cause);
    	cause.printStackTrace();
        ctx.close();
    }
	

}
