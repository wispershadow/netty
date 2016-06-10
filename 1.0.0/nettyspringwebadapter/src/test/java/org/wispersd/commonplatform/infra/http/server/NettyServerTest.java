package org.wispersd.commonplatform.infra.http.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class NettyServerTest {
	public static void main(String[] args) {
		ApplicationContext applContext = new ClassPathXmlApplicationContext(new String[]{"applicationContextServer.xml","applicationContext-springmvc.xml", "applicationContextDisruptor.xml"});
		applContext.getBean("nettyHttpServer");
		
	}

}
