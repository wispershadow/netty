package org.wispersd.commplatform.infra.http.client.converters;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.wispersd.commplatform.infra.http.HttpConstants;
import org.wispersd.commplatform.infra.http.HttpRequestEntity;
import org.wispersd.commplatform.infra.http.HttpUtils;

public class GetRequestConverter implements Converter<HttpRequestEntity, HttpRequest> {
	private AtomicLong sequence;
	private Populator<HttpRequestEntity, HttpRequest> headerPopulator;
	
	
	public void setSequence(AtomicLong sequence) {
		this.sequence = sequence;
	}

	

	public void setHeaderPopulator(
			Populator<HttpRequestEntity, HttpRequest> headerPopulator) {
		this.headerPopulator = headerPopulator;
	}



	public HttpRequest convert(HttpRequestEntity reqEntity) {
		try {
			String curUrl = reqEntity.getUrl();
			StringBuilder sb = new StringBuilder(curUrl);
			Map<String, List<String>> reqParams = reqEntity.getRequestParams();
			if (reqParams != null && (!reqParams.isEmpty())) {
				int i=0;
				char sep = '&';
				for(String nextParamName: reqParams.keySet()) {
					List<String> nextParamVals = reqParams.get(nextParamName);
					for(String nextParamVal: nextParamVals) {
						if (i == 0 && curUrl.indexOf("?") < 0) {
							sep = '?';
						}
						sb.append(sep).append(nextParamName).append(URLEncoder.encode(nextParamVal, "UTF_8"));
						i++;
					}
				}
			}
			HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, sb.toString());
			headerPopulator.populate(reqEntity, request);
			if (!request.headers().contains(HttpConstants.HTTPHEADER_REQID)) {
				request.headers().add(HttpConstants.HTTPHEADER_REQID, sequence.getAndIncrement());
			}
			return request;
		} catch (Exception e) {
			throw new ConversionFailedException(TypeDescriptor.valueOf(HttpRequestEntity.class), TypeDescriptor.valueOf(HttpRequest.class), "", e);
		}
	}

}
