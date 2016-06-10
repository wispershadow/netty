package org.wispersd.commplatform.infra.http.disruptoraddon;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

import org.wispersd.commplatform.infra.http.SslContextFactory;
import org.wispersd.commplatform.infra.http.server.RequestProcessorFactory;

public class NettyHttpServerInitializer extends ChannelInitializer<SocketChannel> {
	private SslContext sslCtx;
	private final RequestProcessorFactory requestProcessorFactory;
	private final RequestEventProducer requestEventProducer;

	public NettyHttpServerInitializer(SslContextFactory sslContextFactory, RequestProcessorFactory requestProcessorFactory, RequestEventProducer requestEventProducer) throws Exception {
		this.sslCtx = sslContextFactory.create();
		this.requestProcessorFactory = requestProcessorFactory;
		this.requestEventProducer = requestEventProducer;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		if (sslCtx != null) {
			//p.addLast(sslCtx.newHandler(ch.alloc()));
		}
		p.addLast(new HttpServerCodec());
		//p.addLast(new HttpContentCompressor());
		p.addLast(new AsyncNettyHttpServerHandler(requestProcessorFactory, requestEventProducer));
	}

}
