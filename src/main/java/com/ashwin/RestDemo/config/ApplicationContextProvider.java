package com.ashwin.RestDemo.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextProvider {

	private static ApplicationContext applicationContext;

	public static ApplicationContext getApplicationContext() {
		if(applicationContext == null) {
			applicationContext = new ClassPathXmlApplicationContext("spring.xml");
		}
		return applicationContext;
	}
}
