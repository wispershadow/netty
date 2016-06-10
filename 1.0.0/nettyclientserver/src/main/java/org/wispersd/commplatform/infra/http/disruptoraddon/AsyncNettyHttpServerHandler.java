package org.wispersd.commplatform.infra.http.disruptoraddon;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import org.wispersd.commplatform.infra.http.HttpRequestEntity;
import org.wispersd.commplatform.infra.http.server.AbstractNettyHttpServerHandler;
import org.wispersd.commplatform.infra.http.server.RequestProcessorFactory;

public class AsyncNettyHttpServerHandler extends AbstractNettyHttpServerHandler{
	private final RequestEventProducer requestEventProducer;
	
	public AsyncNettyHttpServerHandler(RequestProcessorFactory requestProcessorFactory,
			RequestEventProducer requestEventProducer) {
		super(requestProcessorFactory);
		this.requestEventProducer = requestEventProducer;
	}


	@Override
	protected void processRequest(HttpRequestEntity reqEntity, Channel channel,
			HttpRequest request) {	
		requestEventProducer.produceRequestEvent(channel, reqEntity, HttpHeaders.isKeepAlive(request));
	}

}
