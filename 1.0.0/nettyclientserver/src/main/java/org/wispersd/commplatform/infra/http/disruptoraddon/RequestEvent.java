package org.wispersd.commplatform.infra.http.disruptoraddon;

import io.netty.channel.Channel;

import org.wispersd.commplatform.infra.http.HttpRequestEntity;

public class RequestEvent {
	private Channel channel; 
	private boolean keepAlive;
	private HttpRequestEntity reqEntity;

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public HttpRequestEntity getReqEntity() {
		return reqEntity;
	}

	public void setReqEntity(HttpRequestEntity reqEntity) {
		this.reqEntity = reqEntity;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}
	
	
}
