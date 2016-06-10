package org.wispersd.commplatform.infra.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public class HttpUtils {
	public static void readByteBuf(ByteBuf byteBuf, StringBuilder result) {
		try {
			result.append(byteBuf.toString(CharsetUtil.UTF_8));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String addRequestParams(String reqPath, Map<String, List<String>> params) {
		if (params == null || params.isEmpty()) {
			return reqPath;
		}
		StringBuilder sb = new StringBuilder(reqPath);
		int paramInd = sb.lastIndexOf("?");
		if (paramInd >= 0) {
			sb.append("&");
		}
		else {
			sb.append("?");
		}
		int i=0;
		for(String nextParamName: params.keySet()) {
			List<String> nextParamVals = params.get(nextParamName);
			if (i>0) {
				sb.append("&");
			}
			for(String nextParamVal: nextParamVals) {
				sb.append(nextParamName).append("=").append(encodeUrlParam(nextParamVal));
			}
			i++;
		}
		return sb.toString();
	}
	
	private static String encodeUrlParam(String paramVal) {
		try {
			return URLEncoder.encode(paramVal, "UTF-8").replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			return paramVal;
		}
	}
}
