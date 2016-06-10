package org.wispersd.commplatform.infra.http.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wispersd.commplatform.infra.http.HttpConstants;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

public class RequestProcessContext {
	private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
	private HttpPostRequestDecoder decoder;
	private HttpRequest request;
	private String contentType;
	private boolean requestCompleted = false;
	private boolean readingChunks = false;
	private final StringBuilder contentBuffer = new StringBuilder();
	private final Map<String, List<String>> requestAttribs = new HashMap<String, List<String>>();
	
	static {
		DiskFileUpload.deleteOnExitTemporaryFile = true;
		DiskFileUpload.baseDirectory = null; 
		DiskAttribute.deleteOnExitTemporaryFile = true;
		DiskAttribute.baseDirectory = null;
	}
	
		
	public HttpPostRequestDecoder getDecoder() {
		return decoder;
	}


	public void setDecoder(HttpPostRequestDecoder decoder) {
		this.decoder = decoder;
	}


	public HttpRequest getRequest() {
		return request;
	}

	public void setRequest(HttpRequest request) throws Exception {
		this.request = request;
		this.readingChunks = HttpHeaders.isTransferEncodingChunked(request);
		this.contentType = request.headers().get(HttpHeaders.Names.CONTENT_TYPE);
		if (isDecoderRequired()) {
			 decoder = new HttpPostRequestDecoder(factory, request);
		}
	
	}

	public boolean isDecoderRequired() {
		return contentType != null && (
			   contentType.startsWith(HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED) || 
			   contentType.startsWith(HttpHeaders.Values.MULTIPART_FORM_DATA) || 
			   contentType.startsWith(HttpConstants.FILEUPLOAD_CONTENTTYPE));
	}


	public boolean isReadingChunks() {
		return readingChunks;
	}
	


	public boolean isRequestCompleted() {
		return requestCompleted;
	}


	public void setRequestCompleted(boolean requestCompleted) {
		this.requestCompleted = requestCompleted;
	}
	

	public StringBuilder getContentBuffer() {
		return contentBuffer;
	}

	
	public void addAttribute(String attrName, String attrValue) {
		List<String> attrVals = requestAttribs.get(attrName);
		if (attrVals == null) {
			attrVals = new ArrayList<String>();
			requestAttribs.put(attrName, attrVals);
		}
		attrVals.add(attrValue);
	}
	
	public Map<String, List<String>> getRequestAttribs() {
		return requestAttribs;
	}


	public void clear() {
		if (decoder != null) {
			decoder.destroy();
		}
		decoder = null;
		request = null;
		readingChunks = false;
		requestCompleted = false;
		contentBuffer.setLength(0);
		requestAttribs.clear();
	}

}
