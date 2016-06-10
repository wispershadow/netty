package org.wispersd.commplatform.infra.http.disruptoraddon;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;

public class RequestEventProducerFactory {
	private Disruptor<RequestEvent> requestEventDisruptor;
	private RequestEventConsumer requestEventConsumer;
	private int numOfConsumers = 5;
	
	
	public Disruptor<RequestEvent> getRequestEventDisruptor() {
		return requestEventDisruptor;
	}



	public void setRequestEventDisruptor(
			Disruptor<RequestEvent> requestEventDisruptor) {
		this.requestEventDisruptor = requestEventDisruptor;
	}



	public RequestEventConsumer getRequestEventConsumer() {
		return requestEventConsumer;
	}



	public void setRequestEventConsumer(RequestEventConsumer requestEventConsumer) {
		this.requestEventConsumer = requestEventConsumer;
	}



	public int getNumOfConsumers() {
		return numOfConsumers;
	}



	public void setNumOfConsumers(int numOfConsumers) {
		this.numOfConsumers = numOfConsumers;
	}



	public RequestEventProducer getRequestEventProducer() {
		WorkHandler<RequestEvent>[] arr = new WorkHandler[numOfConsumers];
		for(int i=0; i<arr.length; i++) {
			arr[i] = requestEventConsumer;
		}
		requestEventDisruptor.handleEventsWithWorkerPool(arr);
		requestEventDisruptor.start();
		RingBuffer<RequestEvent> ringBuffer = requestEventDisruptor.getRingBuffer();
		RequestEventProducer reqEventProducer = new RequestEventProducer(ringBuffer);
		return reqEventProducer;
	}
	

}
