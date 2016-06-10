package org.wispersd.commplatform.infra.http.disruptoraddon;

import io.netty.channel.Channel;

import org.wispersd.commplatform.infra.http.HttpRequestEntity;

import com.lmax.disruptor.RingBuffer;

public class RequestEventProducer {
	private final RingBuffer<RequestEvent> ringBuffer;
	
	public RequestEventProducer(RingBuffer<RequestEvent> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}
	
	 public void produceRequestEvent(Channel channel, HttpRequestEntity reqEntity, boolean keepAlive) {
		 long sequence = ringBuffer.next();
		 try
	     {
			 RequestEvent requestEvent = ringBuffer.get(sequence);
			 requestEvent.setChannel(channel);
			 requestEvent.setReqEntity(reqEntity);
			 requestEvent.setKeepAlive(keepAlive);
	     }
		 finally {
			 ringBuffer.publish(sequence);
		 }
	 }
	

}
