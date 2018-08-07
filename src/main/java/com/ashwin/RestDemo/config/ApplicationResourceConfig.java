package com.ashwin.RestDemo.config;

import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;

import com.ashwin.RestDemo.filters.ApplicationResponseFilter;
import com.ashwin.RestDemo.filters.ApplicationRequestFilter;

public class ApplicationResourceConfig extends ResourceConfig {

	public ApplicationResourceConfig() {
		// TODO Auto-generated constructor stub
		 packages("com.ashwin.RestDemo.restapi");
	       // register(LoggingFilter.class);
	       // register(GsonMessageBodyHandler.class);

	        register(ApplicationRequestFilter.class);
	        register(ApplicationResponseFilter.class);
	}

	public ApplicationResourceConfig(Set<Class<?>> classes) {
		super(classes);
		// TODO Auto-generated constructor stub
	}

	public ApplicationResourceConfig(Class<?>... classes) {
		super(classes);
		// TODO Auto-generated constructor stub
	}

	public ApplicationResourceConfig(ResourceConfig original) {
		super(original);
		// TODO Auto-generated constructor stub
	}

}
