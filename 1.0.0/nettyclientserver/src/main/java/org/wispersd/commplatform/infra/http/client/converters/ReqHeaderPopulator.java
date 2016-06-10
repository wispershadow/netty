package org.wispersd.commplatform.infra.http.client.converters;

import java.util.List;
import java.util.Map;

import io.netty.handler.codec.http.HttpRequest;

import org.wispersd.commplatform.infra.http.HttpRequestEntity;

public class ReqHeaderPopulator implements Populator<HttpRequestEntity, HttpRequest>{

	public void populate(HttpRequestEntity reqEntity, HttpRequest request) {
		Map<String, List<String>> headers = reqEntity.getHeaders();
		if (headers != null && !headers.isEmpty()) {
			for (String nextHeaderName : headers.keySet()) {
				List<String> headerVals = headers.get(nextHeaderName);
				for (String nextHeaderVal : headerVals) {
					request.headers().add(nextHeaderName, nextHeaderVal);
				}
			}
		}	
		
	}
}
