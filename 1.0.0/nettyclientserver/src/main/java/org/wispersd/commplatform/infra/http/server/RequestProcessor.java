package org.wispersd.commplatform.infra.http.server;

import org.wispersd.commplatform.infra.http.HttpRequestEntity;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;

public interface RequestProcessor {
	public void onRequestReceived(RequestProcessContext context, HttpRequest httpRequest) throws Exception;
	
	public void onContentReceived(RequestProcessContext context, HttpContent httpContent) throws Exception;
	
	public boolean hasRequestCompleted(RequestProcessContext context);

	public HttpRequestEntity convertRequest(RequestProcessContext context) throws Exception;
}
