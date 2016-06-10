package org.wispersd.commplatform.infra.http;

import io.netty.handler.codec.http.HttpMethod;

import java.util.List;
import java.util.Map;

public class HttpRequestEntity {
	private String url;
	private HttpMethod httpMethod;
	private Map<String, List<String>> requestParams;
	private Map<String, List<String>> headers;
	private Map<String, List<String>> requestAttribs;
	private String uploadFilePath;
	private String contentType;
	private String content;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	
	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}
	
	
	public Map<String, List<String>> getRequestParams() {
		return requestParams;
	}

	public void setRequestParams(Map<String, List<String>> requestParams) {
		this.requestParams = requestParams;
	}

	public Map<String, List<String>> getRequestAttribs() {
		return requestAttribs;
	}

	public void setRequestAttribs(Map<String, List<String>> requestAttribs) {
		this.requestAttribs = requestAttribs;
	}
	

	public String getUploadFilePath() {
		return uploadFilePath;
	}

	public void setUploadFilePath(String uploadFilePath) {
		this.uploadFilePath = uploadFilePath;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	
	

}
