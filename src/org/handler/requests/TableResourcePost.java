package org.handler.requests;

import java.io.Serializable;
import java.util.Map;

public class TableResourcePost implements Serializable {

	private static final long serialVersionUID = -2425549281895580715L;

	private Map<String, Object> data;

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public TableResourcePost(Map<String, Object> data) {
		this.data = data;
	}

	public TableResourcePost() {
	}	
}
