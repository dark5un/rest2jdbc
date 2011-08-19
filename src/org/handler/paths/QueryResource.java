package org.handler.paths;

import org.handler.utilities.Caster;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class QueryResource extends ServerResource {
	private String id;

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
	 * Run predefined query
	 * POST /_query/{id} -d '{"param1":"value1", "param2":"value2", ...}'
	 */

	@Override
	protected void doInit() throws ResourceException {
		id = Caster.instance().as(getRequestAttributes().get("id"), String.class);
	}

	@Get()
	public String toJson() {
		return getId();
	}
}
