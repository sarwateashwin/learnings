package com.ashwin.RestDemo.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationTokens {

	private static Map<String, String> USER_TOKEN_MAP = null;

	public static Map<String, String> getTokenMap() {

		if(USER_TOKEN_MAP == null) {
			USER_TOKEN_MAP = Collections.synchronizedMap(new HashMap<String,String>());
		}
		return USER_TOKEN_MAP;
	}

}
