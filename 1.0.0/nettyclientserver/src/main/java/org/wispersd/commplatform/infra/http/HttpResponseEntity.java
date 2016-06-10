package org.wispersd.commplatform.infra.http;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpResponseEntity {
	private HttpResponseStatus statusCode;
	private HttpHeaders headers;
	private String body;
	private boolean hasError;
	private Throwable exception;

	public HttpResponseStatus getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(HttpResponseStatus statusCode) {
		this.statusCode = statusCode;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.hasError = true;
		this.exception = exception;
	}

	public boolean isHasError() {
		return hasError;
	}

	@Override
	public String toString() {
		return "HttpResponseEntity [statusCode=" + statusCode + ", headers="
				+ headers + ", body=" + body + ", hasError=" + hasError
				+ ", exception=" + exception + "]";
	}
}
