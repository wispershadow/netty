package org.wispersd.commplatform.infra.http.server;

import org.wispersd.commplatform.infra.http.HttpRequestEntity;
import org.wispersd.commplatform.infra.http.HttpUtils;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;

public class PostRequestContentProcessor extends AbstractRequestProcessor{

	public void onContentReceived(RequestProcessContext context,
			HttpContent httpContent) throws Exception {
		HttpUtils.readByteBuf(httpContent.content(), context.getContentBuffer());
		if (httpContent instanceof LastHttpContent) {
			context.setRequestCompleted(true);
		}
	}

	@Override
	public HttpRequestEntity convertRequest(RequestProcessContext context) throws Exception{
		HttpRequestEntity reqEntity = super.convertRequest(context);
		reqEntity.setContent(context.getContentBuffer().toString());
		return reqEntity;
	}

	
}
