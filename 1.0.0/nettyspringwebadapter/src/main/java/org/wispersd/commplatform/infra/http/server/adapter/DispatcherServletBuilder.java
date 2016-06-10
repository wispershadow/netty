package org.wispersd.commplatform.infra.http.server.adapter;

import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class DispatcherServletBuilder implements Constants{
	private String contextCfgLocation;
	
	
	
	public String getContextCfgLocation() {
		return contextCfgLocation;
	}



	public void setContextCfgLocation(String contextCfgLocation) {
		this.contextCfgLocation = contextCfgLocation;
	}



	public DispatcherServlet createDispatcherServlet() throws Exception{
		MockServletContext servletContext = new MockServletContext();
    	MockServletConfig servletConfig = new MockServletConfig(servletContext);
    	servletConfig.addInitParameter(CONTEXT_CFG_LOCATION, contextCfgLocation);
    	servletContext.addInitParameter(CONTEXT_CFG_LOCATION, contextCfgLocation);
    	
    	XmlWebApplicationContext wac = new XmlWebApplicationContext();
    	wac.setServletContext(servletContext);
		wac.setServletConfig(servletConfig);
        wac.setConfigLocation("classpath:/" + contextCfgLocation);
		wac.refresh();
		
		DispatcherServlet dispatcherServlet = new DispatcherServlet(wac);
		dispatcherServlet.init(servletConfig);
		return dispatcherServlet;
	}

}
