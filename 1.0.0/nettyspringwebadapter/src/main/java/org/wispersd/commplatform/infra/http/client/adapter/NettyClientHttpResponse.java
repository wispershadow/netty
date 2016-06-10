package org.wispersd.commplatform.infra.http.client.adapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.AbstractClientHttpResponse;
import org.wispersd.commplatform.infra.http.HttpResponseEntity;

public class NettyClientHttpResponse extends AbstractClientHttpResponse implements Constants{
	
	private final HttpResponseEntity responseEntity;
	private ByteArrayInputStream bis;
	private HttpHeaders headers;
	
	public NettyClientHttpResponse(HttpResponseEntity responseEntity) {
		this.responseEntity = responseEntity;
	}

	public int getRawStatusCode() throws IOException {
		return responseEntity.getStatusCode().code();
	}

	public String getStatusText() throws IOException {
		return responseEntity.getStatusCode().reasonPhrase();
	}

	public void close() {
		
	}

	public InputStream getBody() throws IOException {
		if (this.bis == null) {
			this.bis = new ByteArrayInputStream(responseEntity.getBody().getBytes(DEFAULT_CHARSET));
		}
		return this.bis;
	}

	public HttpHeaders getHeaders() {
		if (this.headers == null) {
			this.headers = new HttpHeaders();
			io.netty.handler.codec.http.HttpHeaders tmpHeaders = responseEntity.getHeaders();
			Set<String> headers = tmpHeaders.names();
			for(String nextHeader: headers) {
				List<String> headerValues = tmpHeaders.getAll(nextHeader);
				for(String nextHeaderValue: headerValues) {
					this.headers.add(nextHeader, nextHeaderValue);
				}
			}
		}
		return this.headers;
	}

}
