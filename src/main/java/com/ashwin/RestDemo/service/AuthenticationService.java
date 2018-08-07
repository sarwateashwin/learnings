package com.ashwin.RestDemo.service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ashwin.RestDemo.exceptions.AuthenticationException;
import com.ashwin.RestDemo.exceptions.AuthenticationException.Failure;
import com.ashwin.RestDemo.model.UserAccess;
import com.ashwin.RestDemo.model.UserRole;
@Service("authenticationService")
public class AuthenticationService {
	@Autowired
	private SessionFactory sessionFactory;
	private StringTokenizer getUserNamePasswordToken(String authCredentials) {
		if (null == authCredentials)
			return null;
		// header value format will be "Basic encodedstring" for Basic
		// authentication. Example "Basic YWRtaW46YWRtaW4="
		final String encodedUserPassword = authCredentials.split("\\s+")[1];
		String usernameAndPassword = null;
		try {
			byte[] decodedBytes = Base64.getDecoder().decode(encodedUserPassword);
			usernameAndPassword = new String(decodedBytes, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
		return tokenizer;
	}

	public String getUserName(String authCredentials) {
		StringTokenizer tokens = getUserNamePasswordToken(authCredentials);
		if(tokens == null) return null;
		return tokens.nextToken();

	}

	public UserRole getUserRoleFromTokenMap(String userName) {
		Map<String,String> tokenMap = AuthenticationTokens.getTokenMap();

		if(tokenMap == null || tokenMap.isEmpty()) return null;
		if(!tokenMap.containsKey(userName)) return null;

		return UserRole.valueOf(tokenMap.get(userName).split(":")[0]);
	}

	public String getAuthTokenFromTokenMap(String userName) {
		Map<String,String> tokenMap = AuthenticationTokens.getTokenMap();

		if(tokenMap == null || tokenMap.isEmpty()) return null;
		if(!tokenMap.containsKey(userName)) return null;

		return tokenMap.get(userName).split(":")[1];
	}

	public void authenticate(String authCredentials, Set<String> allowedRoles) throws Exception {

		String userName = getUserName(authCredentials);
		if(userName == null) throw new AuthenticationException(Failure.INVALID_CREDENTIALS);

		if(authCredentials.startsWith("Basic")) {
			authenticateUsingPassword(authCredentials);
			authorizeRole(userName, allowedRoles);
		} else if(authCredentials.startsWith("AuthToken")) {
			authenticateUsingToken(authCredentials);
			authorizeRole(userName, allowedRoles);
		} else {
			throw new AuthenticationException(Failure.INVALID_CREDENTIALS);
		}
	}

	private void generateAuthToken(String userName, String password, UserRole userRole) {
		String userNamePassword = userName + ":" + "password";
		AuthenticationTokens.getTokenMap().put(userName, userRole.toString() + ":" +Base64.getEncoder().encodeToString(userNamePassword.getBytes()));
	}

	private void authenticateUsingPassword(String authCredentials) throws AuthenticationException {

		StringTokenizer tokenizer = getUserNamePasswordToken(authCredentials);

		if (tokenizer == null) new AuthenticationException(Failure.INVALID_CREDENTIALS);

		final String userName = tokenizer.nextToken();
		final String password = tokenizer.nextToken();

		Session session = sessionFactory.openSession();
		Query<UserAccess> query = session.createQuery("from UserAccess where user.userName = '" + userName + "'");
		UserAccess userAccess = null;
		List<UserAccess> userList = query.list();

		if (userList.isEmpty())	throw new AuthenticationException(Failure.INVALID_CREDENTIALS);

		userAccess = userList.get(0);

		if(!password.equals(userAccess.getPassword())) throw new AuthenticationException(Failure.INVALID_CREDENTIALS);

		generateAuthToken(userName, password, userAccess.getUserRole());

	}

	private void authorizeRole(String userName, Set<String> allowedRoles) throws AuthenticationException {
		UserRole userRole = getUserRoleFromTokenMap(userName);
		if(!allowedRoles.contains(userRole.toString())) throw new AuthenticationException(Failure.UNAUTHORIZED_ACCESS);
	}

	private void authenticateUsingToken(String authCredentials) throws AuthenticationException {

		String userName = getUserName(authCredentials);
		String userToken = authCredentials.split("\\s+")[1];

		if(!AuthenticationTokens.getTokenMap().containsKey(userName)) throw new AuthenticationException(Failure.EXPIRED_TOKEN);

		String authToken = getAuthTokenFromTokenMap(userName);

		if(!userToken.equals(authToken)) throw new AuthenticationException(Failure.INVALID_CREDENTIALS);

	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

}