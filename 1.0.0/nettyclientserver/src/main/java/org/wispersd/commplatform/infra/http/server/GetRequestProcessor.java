package org.wispersd.commplatform.infra.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

public class GetRequestProcessor extends AbstractRequestProcessor {
	private static final Logger logger = LoggerFactory.getLogger(GetRequestProcessor.class);
	
	@Override
	public void onRequestReceived(RequestProcessContext context,
			HttpRequest httpRequest) throws Exception {
		super.onRequestReceived(context, httpRequest);
	}

	public void onContentReceived(RequestProcessContext context,
			HttpContent httpContent) throws Exception {
		if (httpContent instanceof LastHttpContent) {
			context.setRequestCompleted(true);
		}
		else if (logger.isWarnEnabled()) {
			logger.warn("Content should not received for get request!");
		}
	}

}
