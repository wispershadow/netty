package org.wispersd.commplatform.infra.http.client.adapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.wispersd.commplatform.infra.http.HttpRequestEntity;
import org.wispersd.commplatform.infra.http.HttpResponseEntity;
import org.wispersd.commplatform.infra.http.client.NettyHttpClient;

public class NettyClientHttpRequest extends AbstractClientHttpRequest implements Constants{
	private static final Logger logger = LoggerFactory.getLogger(NettyClientHttpRequest.class);
	private HttpMethod method;
	private URI uri;
	private NettyHttpClient nettyHttpClient;
	private ByteArrayOutputStream bufferedOutput = new ByteArrayOutputStream();
	
	public NettyClientHttpRequest(URI uri, HttpMethod method, NettyHttpClient nettyHttpClient) {
		this.uri = uri;
		this.method = method;
		this.nettyHttpClient = nettyHttpClient;
	}
	
	public HttpMethod getMethod() {
		return method;
	}

	public URI getURI() {
		return uri;
	}

	@Override
	protected OutputStream getBodyInternal(HttpHeaders headers) throws IOException {
		return this.bufferedOutput;
	}

	@Override
	protected ClientHttpResponse executeInternal(HttpHeaders headers)
			throws IOException {
		byte[] bytes = this.bufferedOutput.toByteArray();
		if (headers.getContentLength() == -1) {
			headers.setContentLength(bytes.length);
		}
		ClientHttpResponse result = executeInternal(headers, bytes);
		this.bufferedOutput = null;
		return result;
	}
	
	
	protected ClientHttpResponse executeInternal(HttpHeaders headers, byte[] bufferedOutput) throws IOException {
		HttpRequestEntity reqEntity = new HttpRequestEntity();
		reqEntity.setUrl(uri.toString());
		reqEntity.setHeaders(headers);
		if (headers.getContentType() != null) {
			reqEntity.setContentType(headers.getContentType().toString());
		}
		try {
			HttpResponseEntity respEntity = null;
			if (method == HttpMethod.GET) {
				respEntity = nettyHttpClient.doGet(reqEntity);
			}
			else if (method == HttpMethod.POST) {
				reqEntity.setContent(new String(bufferedOutput, DEFAULT_CHARSET));
				if (logger.isDebugEnabled()) {
					logger.debug("Setting content for post: " + reqEntity.getContent());
				}
				respEntity = nettyHttpClient.doPost(reqEntity);
			}
			else {
				throw new UnsupportedOperationException("Http method is not supported: " + method);
			}
			if (respEntity != null) {
				return new NettyClientHttpResponse(respEntity);
			}
			else {
				throw new IOException("Empty response body returned"); 
			}
			
		} catch(IOException ie) {
			throw ie;
		} catch (Exception e) {
			throw new IOException("Error sending netty request ", e);
		}
	}


}
