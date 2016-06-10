package org.wispersd.commplatform.infra.http.client.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class RegisterJsonConverterPostProcessor implements BeanFactoryPostProcessor{
	private static final Logger logger = LoggerFactory.getLogger(RegisterJsonConverterPostProcessor.class);

	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		RestTemplate restTemplate = (RestTemplate)beanFactory.getBean("restTemplate");
		try {
			HttpMessageConverter fastJsonConverter = (HttpMessageConverter)beanFactory.getBean("fastJsonMessageConverter");
			restTemplate.getMessageConverters().add(fastJsonConverter);
		} catch (Exception e) {
			logger.error("Error finding fastjson converter ", e);
		}
		
	}

}
