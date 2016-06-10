package org.wispersd.commonplatform.infra.http.client;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.wispersd.commonplatform.infra.http.entity.TestObj;

public class RestTemplateGetTest {
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
		int numberOfThreads = 2;
		final CountDownLatch latch = new CountDownLatch(numberOfThreads);
		Runnable r = new Runnable() {
			public void run() {
				for(int i=1; i<=5; i++) {
					long start = System.currentTimeMillis();
					ResponseEntity<TestObj> respEntity = restTemplate.getForEntity("http://localhost:8443/testget/findById?param1="+i+"&param2=val2", TestObj.class);
					long end = System.currentTimeMillis();
					System.out.println("Total time: " + (end-start) + " message body: " + respEntity.getBody());
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
