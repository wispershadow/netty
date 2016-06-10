package org.wispersd.commonplatform.infra.http.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class RestTemplatePostFormTest {
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
		restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
		
		int numberOfThreads = 1;
		final CountDownLatch latch = new CountDownLatch(numberOfThreads);
		Runnable r = new Runnable() {
			public void run() {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				for(int i=1; i<=2; i++) {
					long start = System.currentTimeMillis();
					MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
					map.add("requestId", "request_"+ String.valueOf(i));
					map.add("checked", "true");
					map.add("endDate", sdf.format(new Date()));
					String result = restTemplate.postForObject("http://localhost:8443/testpost/submitFormReq", map, String.class);
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
