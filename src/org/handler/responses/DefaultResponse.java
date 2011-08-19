package org.handler.responses;

import java.io.Serializable;

public class DefaultResponse implements Serializable {

	private static final long serialVersionUID = 3654320909970300486L;

	private Boolean ok;
	private String id;
	private String rev;
	
	public Boolean getOk() {
		return ok;
	}
	public void setOk(Boolean ok) {
		this.ok = ok;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRev() {
		return rev;
	}
	public void setRev(String rev) {
		this.rev = rev;
	}
	
	public DefaultResponse(Boolean ok, String id, String rev) {
		super();
		this.ok = ok;
		this.id = id;
		this.rev = rev;
	}
	
	public DefaultResponse() { 
		this.ok = false;
	}
	
}
