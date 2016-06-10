package org.wispersd.commplatform.infra.http.client.converters;

import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.wispersd.commplatform.infra.http.HttpRequestEntity;

public class PostContentConverter implements Converter<HttpRequestEntity, HttpContent>{

	public HttpContent convert(HttpRequestEntity source) {
		try {
			DefaultLastHttpContent content = new DefaultLastHttpContent();
			content.content().writeBytes(source.getContent().getBytes("UTF-8"));
			
			return content;
		} catch (Exception e) {
			throw new ConversionFailedException(TypeDescriptor.valueOf(HttpRequestEntity.class), TypeDescriptor.valueOf(HttpRequest.class), "", e);
		}
	}

}
