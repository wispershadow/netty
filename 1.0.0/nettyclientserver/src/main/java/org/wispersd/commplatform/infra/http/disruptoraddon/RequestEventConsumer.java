package org.wispersd.commplatform.infra.http.disruptoraddon;

import org.wispersd.commplatform.infra.http.HttpRequestEntity;
import org.wispersd.commplatform.infra.http.HttpResponseEntity;
import org.wispersd.commplatform.infra.http.server.RequestDispatcher;
import org.wispersd.commplatform.infra.http.server.ServerUtils;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

public class RequestEventConsumer implements WorkHandler<RequestEvent>{
	private RequestDispatcher requestDispatcher;
	

	
	public RequestDispatcher getRequestDispatcher() {
		return requestDispatcher;
	}


	public void setRequestDispatcher(RequestDispatcher requestDispatcher) {
		this.requestDispatcher = requestDispatcher;
	}


	public void onEvent(RequestEvent event) throws Exception {
		HttpRequestEntity reqEntity = event.getReqEntity();
		HttpResponseEntity respEntity = requestDispatcher.dispatch(reqEntity);	
		ServerUtils.createResponse(event.getChannel(), respEntity, event.isKeepAlive());
	}

	
}
