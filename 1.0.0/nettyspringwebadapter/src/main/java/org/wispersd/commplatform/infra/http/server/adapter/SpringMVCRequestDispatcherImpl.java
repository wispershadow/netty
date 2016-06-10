package org.wispersd.commplatform.infra.http.server.adapter;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.wispersd.commplatform.infra.http.HttpRequestEntity;
import org.wispersd.commplatform.infra.http.HttpResponseEntity;
import org.wispersd.commplatform.infra.http.server.RequestDispatcher;

public class SpringMVCRequestDispatcherImpl implements RequestDispatcher, Constants{
	private DispatcherServlet dispatcherServlet;
	
	public DispatcherServlet getDispatcherServlet() {
		return dispatcherServlet;
	}

	public void setDispatcherServlet(DispatcherServlet dispatcherServlet) {
		this.dispatcherServlet = dispatcherServlet;
	}



	public HttpResponseEntity dispatch(HttpRequestEntity reqEntity) {
		HttpResponseEntity respEntity = null;
		try {
			MockHttpServletRequest servletRequest = createServletRequest(reqEntity);
			MockHttpServletResponse servletResponse = new MockHttpServletResponse();
			this.dispatcherServlet.service(servletRequest, servletResponse);
			respEntity = this.createResponseEntity(servletResponse);
		} catch (Exception e) {
			e.printStackTrace();
			respEntity = this.createErrorResponseEntity(e);
		}
		return respEntity;
		/*
		HttpResponseEntity respEntity = new HttpResponseEntity();
		respEntity.setStatusCode(HttpResponseStatus.OK);
		respEntity.setBody("{a:1,b:2}");
		return respEntity;
		*/
	}

	
	protected MockHttpServletRequest createServletRequest(HttpRequestEntity reqEntity) {
		MockHttpServletRequest servletRequest = new MockHttpServletRequest(dispatcherServlet.getServletContext());
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(reqEntity.getUrl()).build();
		servletRequest.setRequestURI(uriComponents.getPath());
		servletRequest.setPathInfo(uriComponents.getPath());
		servletRequest.setMethod(reqEntity.getHttpMethod().name());

		if (uriComponents.getScheme() != null) {
			servletRequest.setScheme(uriComponents.getScheme());
		}
		if (uriComponents.getHost() != null) {
			servletRequest.setServerName(uriComponents.getHost());
		}
		if (uriComponents.getPort() != -1) {
			servletRequest.setServerPort(uriComponents.getPort());
		}

		Map<String, List<String>> headers = reqEntity.getHeaders();
		for (String name : headers.keySet()) {
			for (String value : headers.get(name)) {
				servletRequest.addHeader(name, value);
			}
		}

		if (reqEntity.getContent() != null) {
			try {
				servletRequest.setContent(reqEntity.getContent().getBytes(DEFAULT_CHARSET));
			} catch (UnsupportedEncodingException e) {
			}
		}
		
		Map<String, List<String>> reqParams = reqEntity.getRequestParams();
		if (reqParams != null) {
			for(String nextParamName: reqParams.keySet()) {
				List<String> nextParamVals = reqParams.get(nextParamName);
				for(String nextParamVal: nextParamVals) {
					servletRequest.addParameter(nextParamName, nextParamVal);
				}
			}
		}
		
		Map<String, List<String>> reqAttribs = reqEntity.getRequestAttribs();
		if (reqAttribs != null) {
			for(String nextAttrName: reqAttribs.keySet()) {
				List<String> nextAttrVals = reqAttribs.get(nextAttrName);
				for(String nextAttrVal: nextAttrVals) {
					servletRequest.addParameter(nextAttrName, nextAttrVal);
				}
			}
		}
		return servletRequest;
	}
	
	protected HttpResponseEntity createResponseEntity(MockHttpServletResponse servletResponse) throws Exception{
		HttpResponseEntity response = new HttpResponseEntity();
		response.setStatusCode(HttpResponseStatus.valueOf(servletResponse.getStatus()));
		Collection<String> headerNames = servletResponse.getHeaderNames();
		if (!CollectionUtils.isEmpty(headerNames)) {
			HttpHeaders headers = new DefaultHttpHeaders();
			for(String nextName: headerNames) {
				headers.add(nextName, servletResponse.getHeaders(nextName));
			}
			response.setHeaders(headers);
		}
		response.setBody(servletResponse.getContentAsString());
		
		return response;
	}
	
	protected HttpResponseEntity createErrorResponseEntity(Exception ex) {
		HttpResponseEntity response = new HttpResponseEntity();
		response.setException(ex);
		return response;
	}
}
