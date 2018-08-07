package com.ashwin.RestDemo.filters;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.springframework.util.StringUtils;

import com.ashwin.RestDemo.config.ApplicationContextProvider;
import com.ashwin.RestDemo.service.AuthenticationService;
import com.ashwin.RestDemo.service.AuthenticationTokens;

public class ApplicationResponseFilter implements ContainerResponseFilter {
	public static final String AUTHENTICATION_HEADER = "Authorization";

	@Override
	public void filter(ContainerRequestContext arg0, ContainerResponseContext arg1) throws IOException {
		// TODO Auto-generated method stub
		MultivaluedMap<String, Object> responseHeaders = arg1.getHeaders();
		MultivaluedMap<String, String> requestHeaders = arg0.getHeaders();

		final List<String> authorization = requestHeaders.get(AUTHENTICATION_HEADER);

		if (authorization != null && !authorization.isEmpty()  && !arg1.getStatusInfo().toEnum().equals(Status.UNAUTHORIZED)) {
			AuthenticationService authenticationService = (AuthenticationService) ApplicationContextProvider
					.getApplicationContext().getBean("authenticationService");
			String userName = authenticationService.getUserName(authorization.get(0));

			if (userName != null) {
				String authToken = authenticationService.getAuthTokenFromTokenMap(userName);
				if (!StringUtils.isEmpty(authToken))
					responseHeaders.add(AUTHENTICATION_HEADER, "AuthToken " + authToken);
			}
		}
	}
}
