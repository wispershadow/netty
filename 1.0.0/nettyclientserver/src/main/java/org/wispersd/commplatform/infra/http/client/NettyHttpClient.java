package org.wispersd.commplatform.infra.http.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.wispersd.commplatform.infra.http.HttpRequestEntity;
import org.wispersd.commplatform.infra.http.HttpResponseEntity;
import org.wispersd.commplatform.infra.http.client.converters.PostRequestConverter;
import org.wispersd.commplatform.infra.http.client.pool.DefaultNettyChannelPool;
import org.wispersd.commplatform.infra.http.client.pool.ResourcePool;

public class NettyHttpClient {
	private String host;
	private int port;
	private int coreSize = 5;
	private int maxSize = 100;
	
	private ChannelHandler clientInitializer;
	private Map<ChannelOption<Object>, Object> channelOptions;
	private volatile Bootstrap bootstrap;
	private ResourcePool<Channel> nettyChannelPool;
	private Converter<HttpRequestEntity, HttpRequest> getRequestConverter;
	private Converter<HttpRequestEntity, HttpPostRequestWrapper> postRequestConverter;
	
	static {
		DiskFileUpload.deleteOnExitTemporaryFile = true;
		DiskFileUpload.baseDirectory = null; 
		DiskAttribute.deleteOnExitTemporaryFile = true;
		DiskAttribute.baseDirectory = null;
	}
	
	public void setHost(String host) {
		this.host = host;
	}


	public void setPort(int port) {
		this.port = port;
	}
	
	
	public void setCoreSize(int coreSize) {
		this.coreSize = coreSize;
	}


	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}


	public void setChannelOptions(Map<ChannelOption<Object>, Object> channelOptions) {
		this.channelOptions = channelOptions;
	}


	public void setClientInitializer(ChannelHandler clientInitializer) {
		this.clientInitializer = clientInitializer;
	}
	
	
	public void setGetRequestConverter(
			Converter<HttpRequestEntity, HttpRequest> getRequestConverter) {
		this.getRequestConverter = getRequestConverter;
	}


	public void setPostRequestConverter(
			Converter<HttpRequestEntity, HttpPostRequestWrapper> postRequestConverter) {
		this.postRequestConverter = postRequestConverter;
	}


	public synchronized Bootstrap start() throws Exception {
		if (bootstrap == null) {
			EventLoopGroup group = new NioEventLoopGroup();
			try {
				bootstrap = new Bootstrap();
				if (channelOptions != null) {
	            	for(ChannelOption<Object> nextOption: channelOptions.keySet()) {
	            		Object nextOptVal = channelOptions.get(nextOption);
	            		bootstrap.option(nextOption, nextOptVal);
	            	}
	            }
				bootstrap.group(group).channel(NioSocketChannel.class).handler(clientInitializer);
				nettyChannelPool = new DefaultNettyChannelPool(coreSize, maxSize, bootstrap, host, port);
				if (clientInitializer instanceof NettyHttpClientInitializer) {
					((NettyHttpClientInitializer)clientInitializer).setNettyChannelPool(nettyChannelPool);
				}
				
				nettyChannelPool.init();
			}
			catch (Exception ex) {
				group.shutdownGracefully();
				throw ex;
			}
		}
		return bootstrap;
	}
	
	
	public HttpResponseEntity doGet(HttpRequestEntity reqEntity) throws Exception {
		Channel c = nettyChannelPool.acquireResource();
		NettyHttpClientHandler httpClientHandler = getHttpClientHandler(c);
		HttpRequest httpReq = getRequestConverter.convert(reqEntity);		
		return httpClientHandler.exchange(httpReq);
	}
	
	public HttpResponseEntity doPost(HttpRequestEntity reqEntity) throws Exception {
		try {
			Channel c = nettyChannelPool.acquireResource();
			NettyHttpClientHandler httpClientHandler = getHttpClientHandler(c);
			HttpPostRequestWrapper httpPostReqWrapper = postRequestConverter.convert(reqEntity);
			return httpClientHandler.exchange(httpPostReqWrapper);
		}
		finally {
			if (postRequestConverter instanceof PostRequestConverter) {
				((PostRequestConverter)postRequestConverter).getFactory().cleanAllHttpDatas();
			}
		}
	}
	
	
	protected NettyHttpClientHandler getHttpClientHandler(Channel c) throws Exception{
		return (NettyHttpClientHandler)c.pipeline().last();
	}
	
	public synchronized void stop() {
		nettyChannelPool.destroy();
		bootstrap.group().shutdownGracefully();
	}

}
