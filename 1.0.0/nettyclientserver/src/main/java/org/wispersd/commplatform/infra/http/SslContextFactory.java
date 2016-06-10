package org.wispersd.commplatform.infra.http;

import io.netty.handler.ssl.SslContext;

public interface SslContextFactory {
	public SslContext create() throws Exception;
}
