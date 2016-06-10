package org.wispersd.commplatform.infra.http.client.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

public class DefaultNettyChannelPool extends AbstractResourcePool<Channel> {
	private final Bootstrap bootstrap;
	private final String host;
	private final int port;
	
	
	
	
	public DefaultNettyChannelPool(Bootstrap bootstrap, String host, int port) {
		super();
		this.bootstrap = bootstrap;
		this.host = host;
		this.port = port;
	}

	public DefaultNettyChannelPool(int coreSize, int maxSize,
			Bootstrap bootstrap, String host, int port) {
		super(coreSize, maxSize);
		this.bootstrap = bootstrap;
		this.host = host;
		this.port = port;
	}

	@Override
	protected Channel createPhysical() throws Exception {
		return bootstrap.connect(host, port).sync().channel();
	}

	@Override
	protected void performPhysicalClose(Channel resource) throws Exception {
		resource.close();
		
	}

	@Override
	protected void refreshState(Channel resource) throws Exception {
	}
	
	
	
}
