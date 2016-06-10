package org.wispersd.commplatform.infra.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyHttpServer {
	private static final Logger logger = LoggerFactory.getLogger(NettyHttpServer.class);
	private int port;
	private Map<ChannelOption<Object>, Object> channelOptions;
	private ChannelHandler serverInitializer;
	private ServerBootstrap bootstrap;
	private volatile Channel channel;


	public void setPort(int port) {
		this.port = port;
	}


	public void setChannelOptions(Map<ChannelOption<Object>, Object> channelOptions) {
		this.channelOptions = channelOptions;
	}

	public void setServerInitializer(ChannelHandler serverInitializer) {
		this.serverInitializer = serverInitializer;
	}


	public synchronized Channel start() throws Exception {
		if (channel == null) {
			// Configure the server.
	        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	        EventLoopGroup workerGroup = new NioEventLoopGroup();
	        try {
	        	bootstrap = new ServerBootstrap();
	            if (channelOptions != null) {
	            	for(ChannelOption<Object> nextOption: channelOptions.keySet()) {
	            		Object nextOptVal = channelOptions.get(nextOption);
	            		bootstrap.option(nextOption, nextOptVal);
	            	}
	            }
	            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO))
	             .childHandler(serverInitializer);

	            channel = bootstrap.bind(port).sync().channel();
	            if (logger.isInfoEnabled()) {
	            	logger.info("Server started with port: {}", port);
	            }

	            return channel;
	        } catch (Exception ex) {
	            bossGroup.shutdownGracefully();
	            workerGroup.shutdownGracefully();
	            throw ex;
	        }
		}
		else {
			return channel;
		}        
	}
	
	public synchronized void stop() {
		try {
			if (channel != null) {
				channel.close();
			}
		} finally {
			bootstrap.group().shutdownGracefully();
			bootstrap.childGroup().shutdownGracefully();
		}
		
	}

}
