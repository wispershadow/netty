package org.wispersd.commplatform.infra.http.client;

import org.wispersd.commplatform.infra.http.SslContextFactory;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class NettyClientSslContextFactory implements SslContextFactory{
	private boolean useSsl;
	
	public void setUseSsl(boolean useSsl) {
		this.useSsl = useSsl;
	}
	
	public SslContext create() throws Exception {
		final SslContext sslCtx;
        if (useSsl) {
            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }
        return sslCtx;
	}

}
