package org.wispersd.commplatform.infra.http.client;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;

public class HttpPostRequestWrapper {
	private HttpRequest request;
	private HttpContent content;
	private HttpPostRequestEncoder encoder;

	public HttpRequest getRequest() {
		return request;
	}

	public void setRequest(HttpRequest request) {
		this.request = request;
	}

	public HttpContent getContent() {
		return content;
	}

	public void setContent(HttpContent content) {
		this.content = content;
	}

	public HttpPostRequestEncoder getEncoder() {
		return encoder;
	}

	public void setEncoder(HttpPostRequestEncoder encoder) {
		this.encoder = encoder;
	}
}
