package org.handler.requests;

import java.io.Serializable;
import java.util.Map;

public class TableResourcePut implements Serializable {

	private static final long serialVersionUID = -4948954510481199603L;

	private String idColumn;
	private Map<String, Object> columns;
	
	public String getIdColumn() {
		return idColumn;
	}
	public void setIdColumn(String idColumn) {
		this.idColumn = idColumn;
	}
	public Map<String, Object> getColumns() {
		return columns;
	}
	public void setColumns(Map<String, Object> columns) {
		this.columns = columns;
	}
	@Override
	public String toString() {
		return "TableResourcePut [idColumn=" + idColumn + ", columns="
				+ columns + "]";
	}
}
