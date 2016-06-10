package org.wispersd.commplatform.infra.http.server;

import java.util.Map;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

public class RequestProcessorFactory {
	private static final String MATCHALL_CHAR = "*";
	private Map<String, RequestProcessor> requestProcessors;
	
	public void setRequestProcessors(Map<String, RequestProcessor> requestProcessors) {
		this.requestProcessors = requestProcessors;
	}


	public RequestProcessor getRequestProcessorForReq(HttpRequest request) {
		HttpMethod reqMethod = request.getMethod();
		String contentType = request.headers().get(HttpHeaders.Names.CONTENT_TYPE);
		for (String nextKey: requestProcessors.keySet()) {
			if (matchKey(nextKey, reqMethod.name(), contentType)) {
				return requestProcessors.get(nextKey);
			}
		}
		return null;
	}
	
	protected static boolean matchKey(String curKey, String methodName, String contentType) {
		if (contentType != null) {
			int sepInd = contentType.indexOf(";");
			if (sepInd >= 0) {
				contentType = contentType.substring(0, sepInd);
			}
		}
		String[] keyParts = curKey.split("\\|");
		if (keyParts.length > 1) {
			String keyMethodName = keyParts[0];
			if (keyMethodName.equals(methodName)) {
				String keyContentTypes = keyParts[1];
				if (MATCHALL_CHAR.equals(keyContentTypes)) {
					return true;
				}
				else {
					String[] keyContentTypeArr = keyContentTypes.split(",");
					for(String nextKeyContentType: keyContentTypeArr) {
						if (nextKeyContentType.equals(contentType)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
