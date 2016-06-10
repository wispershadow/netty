package org.wispersd.commplatform.infra.http.client;

import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

import org.wispersd.commplatform.infra.http.SslContextFactory;
import org.wispersd.commplatform.infra.http.client.pool.ResourcePool;

public class NettyHttpClientInitializer extends ChannelInitializer<SocketChannel>{
	private SslContext sslCtx;
	private long readTimeout = 3000;
	private ResourcePool<Channel> nettyChannelPool;
	
	public NettyHttpClientInitializer(SslContextFactory sslContextFactory) throws Exception {
        //this.sslCtx = sslContextFactory.create();
    }
	
	public void setReadTimeout(long readTimeout) {
		this.readTimeout = readTimeout;
	}


	public void setNettyChannelPool(ResourcePool<Channel> nettyChannelPool) {
		this.nettyChannelPool = nettyChannelPool;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();

        // Enable HTTPS if necessary.
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
       
        p.addLast(new HttpClientCodec());
        // Remove the following line if you don't want automatic content decompression.
        //p.addLast(new HttpContentDecompressor());
        p.addLast(new ChunkedWriteHandler());
       
        p.addLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS));
        p.addLast(new NettyHttpClientHandler(nettyChannelPool));
	}

}
