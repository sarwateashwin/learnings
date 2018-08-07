package com.ashwin.RestDemo.filters;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.ashwin.RestDemo.config.ApplicationContextProvider;
import com.ashwin.RestDemo.exceptions.AuthenticationException;
import com.ashwin.RestDemo.service.AuthenticationService;

@Provider
public class ApplicationRequestFilter implements ContainerRequestFilter {

	/*
	 * public void doFilter(ServletRequest request, ServletResponse response,
	 * FilterChain filter) throws IOException, ServletException { if (request
	 * instanceof HttpServletRequest) { HttpServletRequest httpServletRequest =
	 * (HttpServletRequest) request; String authCredentials = httpServletRequest
	 * .getHeader(AUTHENTICATION_HEADER);
	 *
	 * // better injected AuthenticationService authenticationService = new
	 * AuthenticationService();
	 *
	 * boolean authenticationStatus = authenticationService
	 * .authenticate(authCredentials);
	 *
	 * if (authenticationStatus) { filter.doFilter(request, response); } else {
	 * if (response instanceof HttpServletResponse) { HttpServletResponse
	 * httpServletResponse = (HttpServletResponse) response; httpServletResponse
	 * .setStatus(HttpServletResponse.SC_UNAUTHORIZED); } } } }
	 */

	@Context
	private ResourceInfo resourceInfo;
	public static final String AUTHENTICATION_HEADER = "Authorization";
	public static final String AUTHENTICATION_SCHEME = "BASIC";
	private AuthenticationService authenticationService;

	@Override
	public void filter(ContainerRequestContext request) throws IOException {

		authenticate(request);

	}

	private void authenticate(ContainerRequestContext request) {
		// TODO Auto-generated method stub

		Method method = resourceInfo.getResourceMethod();
		if (!method.isAnnotationPresent(PermitAll.class)) {

			final MultivaluedMap<String, String> header = request.getHeaders();
			final List<String> authorization = header.get(AUTHENTICATION_HEADER);

			if (authorization == null || authorization.isEmpty()) {
				request.abortWith(Response.status(Response.Status.UNAUTHORIZED)
						.entity("Authentication not done. You are not allowed to access this resource!").type(MediaType.TEXT_PLAIN).build());
			} else if (method.isAnnotationPresent(RolesAllowed.class)) {
				RolesAllowed allowed = method.getAnnotation(RolesAllowed.class);
				Set<String> allowedRoles = new HashSet<String>(Arrays.asList(allowed.value()));
				if (authenticationService == null) {
					authenticationService = (AuthenticationService) ApplicationContextProvider.getApplicationContext()
							.getBean("authenticationService");
				}
				try {
					authenticationService.authenticate(authorization.get(0), allowedRoles);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					request.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage())
							.type(MediaType.TEXT_PLAIN).build());
				}

			} else {
				request.abortWith(Response.status(Response.Status.UNAUTHORIZED)
						.entity("You are not allowed to access this resource").type(MediaType.TEXT_PLAIN).build());
			}
		}

	}

}
