package org.wispersd.commplatform.infra.http.disruptoraddon;

import com.lmax.disruptor.EventFactory;

public class RequestEventFactory implements EventFactory<RequestEvent>{

	public RequestEvent newInstance() {
		return new RequestEvent();
	}

}
