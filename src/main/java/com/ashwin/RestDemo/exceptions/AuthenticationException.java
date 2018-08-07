package com.ashwin.RestDemo.exceptions;

public class AuthenticationException extends Exception {

	public enum Failure {
		UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS : You are not allowed to access this resource"), INVALID_CREDENTIALS(
				"UNAUTHORIZED_ACCESS : Invalid username/password or token"), EXPIRED_TOKEN("UNAUTHORIZED_ACCESS : Token has expired! Please authenticate using username/password");
		public String message;

		private Failure(String message) {
			this.message = message;
		}
	}

	public AuthenticationException(Failure failure) {
		// TODO Auto-generated constructor stub
		super(failure.message);
	}

}
