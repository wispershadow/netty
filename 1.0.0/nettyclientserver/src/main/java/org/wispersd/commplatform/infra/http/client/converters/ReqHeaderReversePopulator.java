package org.wispersd.commplatform.infra.http.client.converters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import org.wispersd.commplatform.infra.http.HttpRequestEntity;

public class ReqHeaderReversePopulator implements Populator<HttpRequest, HttpRequestEntity>{

	public void populate(HttpRequest request, HttpRequestEntity reqEntity) {
		HttpHeaders headers = request.headers();
		if (headers != null) {
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			Set<String> names = headers.names();
			for(String nextHeaderName: names) {
				List<String> nextHeaderVals = headers.getAll(nextHeaderName);
				params.put(nextHeaderName, nextHeaderVals);
			}
			reqEntity.setHeaders(params);
		}
		
	}

}
