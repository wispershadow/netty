package org.wispersd.commplatform.infra.http.server;

import org.wispersd.commplatform.infra.http.HttpRequestEntity;
import org.wispersd.commplatform.infra.http.HttpResponseEntity;

public interface RequestDispatcher {
	public HttpResponseEntity dispatch(HttpRequestEntity reqEntity);

}
