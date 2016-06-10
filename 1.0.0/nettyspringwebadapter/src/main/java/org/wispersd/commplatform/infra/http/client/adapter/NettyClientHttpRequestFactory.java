package org.wispersd.commplatform.infra.http.client.adapter;

import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.wispersd.commplatform.infra.http.HttpRequestEntity;
import org.wispersd.commplatform.infra.http.SslContextFactory;
import org.wispersd.commplatform.infra.http.client.HttpPostRequestWrapper;
import org.wispersd.commplatform.infra.http.client.NettyHttpClient;
import org.wispersd.commplatform.infra.http.client.NettyHttpClientInitializer;

public class NettyClientHttpRequestFactory implements ClientHttpRequestFactory{
	private static final Logger logger = LoggerFactory.getLogger(NettyClientHttpRequestFactory.class);
	private SslContextFactory sslContextFactory;
	private Map<ChannelOption<Object>, Object> channelOptions;
	private Converter<HttpRequestEntity, HttpRequest> getRequestConverter;
	private Converter<HttpRequestEntity, HttpPostRequestWrapper> postRequestConverter;
	private final ConcurrentMap<String, NettyHttpClient> httpClients = new ConcurrentHashMap<String, NettyHttpClient>();
	
	
	
	
	public SslContextFactory getSslContextFactory() {
		return sslContextFactory;
	}

	public void setSslContextFactory(SslContextFactory sslContextFactory) {
		this.sslContextFactory = sslContextFactory;
	}

	public Map<ChannelOption<Object>, Object> getChannelOptions() {
		return channelOptions;
	}

	public void setChannelOptions(
			Map<ChannelOption<Object>, Object> channelOptions) {
		this.channelOptions = channelOptions;
	}

	public Converter<HttpRequestEntity, HttpPostRequestWrapper> getPostRequestConverter() {
		return postRequestConverter;
	}

	public void setPostRequestConverter(
			Converter<HttpRequestEntity, HttpPostRequestWrapper> postRequestConverter) {
		this.postRequestConverter = postRequestConverter;
	}


	public Converter<HttpRequestEntity, HttpRequest> getGetRequestConverter() {
		return getRequestConverter;
	}

	public void setGetRequestConverter(
			Converter<HttpRequestEntity, HttpRequest> getRequestConverter) {
		this.getRequestConverter = getRequestConverter;
	}
	
	
	public void stop() {
		for(NettyHttpClient nextClient: httpClients.values()) {
			nextClient.stop();
		}
		httpClients.clear();
	}

	protected NettyHttpClient getOrCreateHttpClient(String urlStr) {
		URL url = null;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			logger.error("Error creating netty client, invalid url", e);
			return null;
		}
		String host = url.getHost();
		int port = url.getPort();
		String concat = host + ":" + port;
		NettyHttpClient client = httpClients.get(concat);
		if (client == null) {
			try {
				client = new NettyHttpClient();
				client.setHost(host);
				client.setPort(port);
				client.setChannelOptions(channelOptions);
				client.setClientInitializer(new NettyHttpClientInitializer(sslContextFactory));
				client.setGetRequestConverter(getRequestConverter);
				client.setPostRequestConverter(postRequestConverter);
				NettyHttpClient existing = httpClients.putIfAbsent(concat, client);
				if (existing == null) {
					client.start();
					return client;
				}
				else {
					existing.start();
					return existing;
				}
			} catch (Exception e) {
				logger.error("Error creating netty client", e);
				return null;
			}
		}
		else {
			try {
				client.start();
			} catch (Exception e) {
				logger.error("Error starting netty client", e);
			}
			return client;
		}
	}
	
	

	public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod)
			throws IOException {
		NettyHttpClient nettyClient = getOrCreateHttpClient(uri.toString());
		return new NettyClientHttpRequest(uri, httpMethod, nettyClient);
		
	}

}
