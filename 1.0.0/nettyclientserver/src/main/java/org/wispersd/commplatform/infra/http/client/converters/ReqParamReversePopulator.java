package org.wispersd.commplatform.infra.http.client.converters;

import java.util.List;
import java.util.Map;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import org.wispersd.commplatform.infra.http.HttpRequestEntity;

public class ReqParamReversePopulator implements Populator<HttpRequest, HttpRequestEntity>{

	public void populate(HttpRequest request, HttpRequestEntity reqEntity) {
		QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
        Map<String, List<String>> uriAttributes = decoderQuery.parameters();
        reqEntity.setUrl(request.getUri());
        reqEntity.setRequestParams(uriAttributes);
		reqEntity.setHttpMethod(request.getMethod());
	}

}
