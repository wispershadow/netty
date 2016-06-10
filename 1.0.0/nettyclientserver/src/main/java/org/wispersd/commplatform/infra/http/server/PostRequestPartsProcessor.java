package org.wispersd.commplatform.infra.http.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.wispersd.commplatform.infra.http.HttpRequestEntity;

public class PostRequestPartsProcessor extends AbstractRequestProcessor{

	public void onContentReceived(RequestProcessContext context,
			HttpContent httpContent) throws Exception {
		HttpPostRequestDecoder decoder = context.getDecoder();
		if (decoder != null) {
			decoder.offer(httpContent);
			try {
				readHttpDataChunkByChunk(decoder, context);
			} catch (HttpPostRequestDecoder.EndOfDataDecoderException ee) {
				//ignore this error
			}
		}
		
		if (httpContent instanceof LastHttpContent) {
			context.setRequestCompleted(true);
		}
	}

	@Override
	public HttpRequestEntity convertRequest(RequestProcessContext context) throws Exception{
		HttpRequestEntity reqEntity = super.convertRequest(context);
		reqEntity.setContent(context.getContentBuffer().toString());
		reqEntity.setRequestAttribs(new HashMap<String, List<String>>(context.getRequestAttribs()));
		return reqEntity;
	}
	
	protected void readHttpDataChunkByChunk(HttpPostRequestDecoder decoder,
			RequestProcessContext context) throws Exception {
		while (decoder.hasNext()) {
			InterfaceHttpData data = decoder.next();
			if (data != null) {
				try {
					if (data.getHttpDataType() == HttpDataType.Attribute) {
						Attribute attribute = (Attribute) data;
						context.addAttribute(attribute.getName(), attribute.getValue());
					} else if (data.getHttpDataType() == HttpDataType.FileUpload) {
						FileUpload fileUpload = (FileUpload) data;
						if (fileUpload.isCompleted()) {
							if (fileUpload.length() < 10000) {
								context.getContentBuffer().append(fileUpload.getString(fileUpload.getCharset()));
							} else {
								throw new ConversionFailedException(TypeDescriptor.valueOf(HttpRequest.class), TypeDescriptor.valueOf(HttpRequestEntity.class), "File size too large", null);
							}
						} else {
							throw new ConversionFailedException(TypeDescriptor.valueOf(HttpRequest.class), TypeDescriptor.valueOf(HttpRequestEntity.class), "File not complete", null);
						}
					}
				} finally {
					data.release();
				}
			}
		}

	}
	
}
