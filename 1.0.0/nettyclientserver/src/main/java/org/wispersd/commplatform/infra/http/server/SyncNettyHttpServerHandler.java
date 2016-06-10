package org.wispersd.commplatform.infra.http.server;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import org.wispersd.commplatform.infra.http.HttpRequestEntity;
import org.wispersd.commplatform.infra.http.HttpResponseEntity;

public class SyncNettyHttpServerHandler extends AbstractNettyHttpServerHandler{
	private final RequestDispatcher requestDispatcher;

	public SyncNettyHttpServerHandler(RequestProcessorFactory requestProcessorFactory,
			RequestDispatcher requestDispatcher) {
		super(requestProcessorFactory);
		this.requestDispatcher = requestDispatcher;
	}




	@Override
	protected void processRequest(HttpRequestEntity reqEntity, Channel channel,
			HttpRequest request) {
		HttpResponseEntity respEntity = requestDispatcher.dispatch(reqEntity);
		super.createResponse(channel, request, respEntity);
	}

}
