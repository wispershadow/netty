package org.wispersd.commplatform.infra.http.server;

import org.wispersd.commplatform.infra.http.SslContextFactory;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class NettyServerSslContextFactory implements SslContextFactory{
	private boolean useSsl;
	
	public void setUseSsl(boolean useSsl) {
		this.useSsl = useSsl;
	}

	public SslContext create() throws Exception {
		final SslContext sslCtx;
        if (useSsl) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }
        return sslCtx;
	}
}
