package org.wispersd.commplatform.infra.http.server;

import io.netty.handler.codec.http.HttpRequest;

import org.springframework.core.convert.converter.Converter;
import org.wispersd.commplatform.infra.http.HttpRequestEntity;

public abstract class AbstractRequestProcessor implements RequestProcessor{
	private Converter<HttpRequest, HttpRequestEntity> requestReverseConverter;
	
	
	public void setRequestReverseConverter(
			Converter<HttpRequest, HttpRequestEntity> requestReverseConverter) {
		this.requestReverseConverter = requestReverseConverter;
	}

	public void onRequestReceived(RequestProcessContext context,
			HttpRequest httpRequest) throws Exception {
		context.setRequest(httpRequest);
	}
	
	public boolean hasRequestCompleted(RequestProcessContext context) {
		return context.isRequestCompleted();
	}

	public HttpRequestEntity convertRequest(RequestProcessContext context) throws Exception {
		return requestReverseConverter.convert(context.getRequest());
	}
}
