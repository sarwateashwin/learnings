package com.ashwin.RestDemo.model;

import java.io.Serializable;

public class Link implements Serializable{
	private String uri;
	private String rel;

	public Link(String uri, String rel) {
		super();
		this.uri = uri;
		this.rel = rel;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}
}
