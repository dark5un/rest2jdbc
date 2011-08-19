package org.handler.paths;

import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class GlobalTableResource extends ServerResource {
	
	/*
	 * Routing protocol
	 * 
	 * Standard response
	 * {"ok":true, "id":"value", "rev":"revisionhash"}
	 * * ok = marks the success of the operation
	 * * id = the id of the row affected if any 
	 * * rev = the revision hash of the operation
	 * 
	 * List tables
	 * GET /_all_tables
	 */

	@Override
	protected void doInit() throws ResourceException {
		System.out.println(getRequest().getAttributes().keySet());
	}

	@Get()
	public String toJson() {
		return "Hello!!!";
	}
}
