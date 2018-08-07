package com.ashwin.RestDemo.model;

import java.io.Serializable;

public enum UserRole implements Serializable{
	BUYER("BUYER"),
	SELLER("BUYER"),
	ADMIN("ADMIN");
	public String userRoleCode;
	private UserRole(String userRoleCode) {
		this.userRoleCode = userRoleCode;
	}
}
