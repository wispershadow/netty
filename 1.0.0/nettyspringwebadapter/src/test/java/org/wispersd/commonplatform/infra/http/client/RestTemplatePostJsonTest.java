package org.wispersd.commonplatform.infra.http.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.client.RestTemplate;
import org.wispersd.commonplatform.infra.http.entity.PostRequest;

public class RestTemplatePostJsonTest {
	public static void main(String[] args) {
		ApplicationContext applContext = new ClassPathXmlApplicationContext(new String[]{"applicationContext-restTemplate.xml", "applicationContext-fastjson.xml"});
		/*
		try {
			Thread.sleep(200000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		System.out.println("============ start processing");
		final RestTemplate restTemplate = (RestTemplate)applContext.getBean("restTemplate");
		final Map<String, Object> urlParams = new HashMap<String, Object>();
		int numberOfThreads = 1;
		final CountDownLatch latch = new CountDownLatch(numberOfThreads);
		Runnable r = new Runnable() {
			public void run() {
				for(int i=1; i<=2; i++) {
					long start = System.currentTimeMillis();
					PostRequest postReq = new PostRequest();
					postReq.setRequestId("request_"+ String.valueOf(i));
					postReq.setSequence(Long.valueOf(i));
					postReq.setStartDate(new Date());
					String result = restTemplate.postForObject("http://localhost:8443/testpost/submitJsonReq", postReq, String.class);
					long end = System.currentTimeMillis();
					System.out.println("Total time: " + (end-start) + " response " + result);
				}
				latch.countDown();
			}
			
		};
		for(int i=0; i<numberOfThreads; i++) {
			Thread t = new Thread(r);
			t.start();
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

}
