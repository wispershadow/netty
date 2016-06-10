package org.wispersd.commplatform.infra.http.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import org.wispersd.commplatform.infra.http.SslContextFactory;

public class NettyHttpServerInitializer extends ChannelInitializer<SocketChannel>{
	private boolean runInSeparateExecutorGrp = true;
	private SslContext sslCtx;
	private final RequestProcessorFactory requestProcessorFactory;
	private final RequestDispatcher requestDispatcher;

	public NettyHttpServerInitializer(SslContextFactory sslContextFactory, RequestProcessorFactory requestProcessorFactory, RequestDispatcher requestDispatcher) throws Exception {
		//this.sslCtx = sslContextFactory.create();
		this.requestProcessorFactory = requestProcessorFactory;
		this.requestDispatcher = requestDispatcher;
	}
	
	
	public boolean isRunInSeparateExecutorGrp() {
		return runInSeparateExecutorGrp;
	}

	public void setRunInSeparateExecutorGrp(boolean runInSeparateExecutorGrp) {
		this.runInSeparateExecutorGrp = runInSeparateExecutorGrp;
	}




	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		if (sslCtx != null) {
			p.addLast(sslCtx.newHandler(ch.alloc()));
		}
		p.addLast(new HttpServerCodec());
		//p.addLast(new HttpContentCompressor());
		if (runInSeparateExecutorGrp) {
			EventExecutorGroup separateExecutorGroup = new DefaultEventExecutorGroup(10);
			p.addLast(separateExecutorGroup, new SyncNettyHttpServerHandler(requestProcessorFactory, requestDispatcher));
		}
		else {
			p.addLast(new SyncNettyHttpServerHandler(requestProcessorFactory, requestDispatcher));
		}
	}
	

}
