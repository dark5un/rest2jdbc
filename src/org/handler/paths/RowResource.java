package org.handler.paths;

import org.handler.utilities.Caster;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class RowResource extends ServerResource {
	private String table;
	private String id;

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/*
	 * Routing protocol
	 * 
	 * Standard response
	 * {"ok":true, "id":"value", "rev":"revisionhash"}
	 * * ok = marks the success of the operation
	 * * id = the id of the row affected if any 
	 * * rev = the revision hash of the operation
	 * 
	 * Update row with id
	 * PUT /{table}/{id} -d '{"column1":"value1", "column2":"value2", ...}'
	 * {"ok":true, "id":"value", "rev":"revisionhash"}
	 * 
	 * Delete row
	 * DELETE /{table}/{id}
	 * {"ok":true, "id":"value", "rev":"revisionhash"}
	 */

	@Override
	protected void doInit() throws ResourceException {
		table = Caster.instance().as(getRequestAttributes().get("table"), String.class);
		id = Caster.instance().as(getRequestAttributes().get("id"), String.class);
	}

	@Get()
	public String toJson() {
		return getTable() + "/" + getId();
	}
}
