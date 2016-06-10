package org.wispersd.commplatform.infra.http.client.converters;

import io.netty.handler.codec.http.HttpRequest;

import org.springframework.core.convert.converter.Converter;
import org.wispersd.commplatform.infra.http.HttpRequestEntity;

public class RequestReverseConverter implements Converter<HttpRequest, HttpRequestEntity>{
	private Populator<HttpRequest, HttpRequestEntity> reqParamReversePopulator;
	private Populator<HttpRequest, HttpRequestEntity> reqHeaderReversePopulator;

	public void setReqParamReversePopulator(
			Populator<HttpRequest, HttpRequestEntity> reqParamReversePopulator) {
		this.reqParamReversePopulator = reqParamReversePopulator;
	}

	public void setReqHeaderReversePopulator(
			Populator<HttpRequest, HttpRequestEntity> reqHeaderReversePopulator) {
		this.reqHeaderReversePopulator = reqHeaderReversePopulator;
	}



	public HttpRequestEntity convert(HttpRequest req) {
		HttpRequestEntity reqEntity = new HttpRequestEntity();
		reqHeaderReversePopulator.populate(req, reqEntity);
		reqParamReversePopulator.populate(req, reqEntity);
		return reqEntity;
	}

}
