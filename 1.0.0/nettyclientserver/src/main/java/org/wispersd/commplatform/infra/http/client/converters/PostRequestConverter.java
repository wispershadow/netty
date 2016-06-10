package org.wispersd.commplatform.infra.http.client.converters;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder.ErrorDataEncoderException;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.wispersd.commplatform.infra.http.HttpConstants;
import org.wispersd.commplatform.infra.http.HttpRequestEntity;
import org.wispersd.commplatform.infra.http.client.HttpPostRequestWrapper;

public class PostRequestConverter implements Converter<HttpRequestEntity, HttpPostRequestWrapper>{
	private static final Logger logger = LoggerFactory.getLogger(PostRequestConverter.class);
	private AtomicLong sequence;
	private final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
	private Converter<HttpRequestEntity, HttpContent> postContentConverter;
	private Populator<HttpRequestEntity, HttpRequest> headerPopulator;
	
	public void setSequence(AtomicLong sequence) {
		this.sequence = sequence;
	}
	
	
	public void setPostContentConverter(
			Converter<HttpRequestEntity, HttpContent> postContentConverter) {
		this.postContentConverter = postContentConverter;
	}




	public void setHeaderPopulator(
			Populator<HttpRequestEntity, HttpRequest> headerPopulator) {
		this.headerPopulator = headerPopulator;
	}


	public HttpPostRequestWrapper convert(HttpRequestEntity reqEntity) {
		HttpPostRequestWrapper wrapper = new HttpPostRequestWrapper();	
		//alternatively, use DefaultHttpRequest and DefaultHttpLastContent to send content separately. internally fullHttpRequest will still be broken into 2 messages
		byte[] contentBytes = null;
		if (reqEntity.getContent() != null) {
			contentBytes = reqEntity.getContent().getBytes(CharsetUtil.UTF_8);
		}
		HttpRequest request = reqEntity.getContent()==null?
							  new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, reqEntity.getUrl()):
							  new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, reqEntity.getUrl(), Unpooled.wrappedBuffer(contentBytes));	  
		wrapper.setRequest(request);
		headerPopulator.populate(reqEntity, request);
		
			
		if (!request.headers().contains(HttpConstants.HTTPHEADER_REQID)) {
			request.headers().add(HttpConstants.HTTPHEADER_REQID, sequence.getAndIncrement());
		}
		if (reqEntity.getContent() != null) {
			if (!request.headers().contains(HttpHeaders.Names.CONTENT_TYPE)) {
				request.headers().add(HttpHeaders.Names.CONTENT_TYPE, reqEntity.getContentType());
			}
			
			if (!request.headers().contains(HttpHeaders.Names.CONTENT_LENGTH)) {	
				request.headers().add(HttpHeaders.Names.CONTENT_LENGTH, contentBytes.length);
			}
		}
		
		HttpPostRequestEncoder bodyRequestEncoder = null;
		Map<String, List<String>> reqAttr = reqEntity.getRequestAttribs();
		try {
			if (reqAttr != null && (!reqAttr.isEmpty())) {
				bodyRequestEncoder = new HttpPostRequestEncoder(factory, request, true);
				for (String nextAttrName : reqAttr.keySet()) {
					List<String> nextAttrVals = reqAttr.get(nextAttrName);
					for(String nextAttrVal: nextAttrVals) {
						bodyRequestEncoder.addBodyAttribute(nextAttrName,nextAttrVal);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error setting request attribute", e);
		}
		
		String filePath = reqEntity.getUploadFilePath();
		try {
			if (filePath != null) {
				File file = new File(filePath);
				if (file.exists() && file.canRead()) {
					if (bodyRequestEncoder == null) {
						bodyRequestEncoder = new HttpPostRequestEncoder(factory, request, true);
					}
					bodyRequestEncoder.addBodyFileUpload(file.getName(), file, null, true);
				}
			}
		} catch (Exception e) {
			logger.error("Error setting request file", e);
		}
		
		if (bodyRequestEncoder != null) {
			wrapper.setEncoder(bodyRequestEncoder);
			try {
				wrapper.setRequest(bodyRequestEncoder.finalizeRequest());
			} catch (ErrorDataEncoderException e) {
				logger.error("Error finalizing request", e);
			}
		}
		/*
		if (reqEntity.getContent() != null) {
			wrapper.setContent(postContentConverter.convert(reqEntity));
		}*/
		return wrapper;
	}


	public HttpDataFactory getFactory() {
		return factory;
	}
}

