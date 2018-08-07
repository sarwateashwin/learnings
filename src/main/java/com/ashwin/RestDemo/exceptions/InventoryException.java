package com.ashwin.RestDemo.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class InventoryException implements ExceptionMapper {
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public InventoryException(String message) {
		//super(message);
		this.message = message;
	}

	@Override
	public Response toResponse(Throwable exception) {
		// TODO Auto-generated method stub
		return null;
	}
}
