package org.handler.restlet;

import org.handler.paths.FullTableResource;
import org.handler.paths.GlobalTableResource;
import org.handler.paths.QueryResource;
import org.handler.paths.RowResource;
import org.handler.paths.TableResource;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;

public class Rest2JdbcApplication extends Application {

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
	 * 
	 * Run predefined query
	 * POST /_query/{id} -d '{"param1":"value1", "param2":"value2", ...}'
	 * 
	 * Create table
	 * PUT /{table} -d '{"id":"idcolumn", "columnname":"columntype", ...}'
	 * 
	 * Delete table
	 * DELETE /{table}
	 * 
	 * Retrieve all rows
	 * GET /{table}/_all_rows
	 * 					& descending=true	ascending or descending
	 * 					& limit=3			limit of result set
	 * 					& start=1			start from row
	 * 					& include_data=true	show ids only or all data
	 * [
	 * 	{	"id":{"column1":"data1", "column2":"data2", ...},
	 * 		...
	 * ]
	 * 
	 * Retrieve rows by keys
	 * POST /{table}/_all_rows -d '{"keys":["key1", "key2", ...]}'
	 * [
	 * 	{	"id":{"column1":"data1", "column2":"data2", ...},
	 * 		...
	 * ]
	 * 
	 * Update row with id
	 * PUT /{table}/{id} -d '{"column1":"value1", "column2":"value2", ...}'
	 * {"ok":true, "id":"value", "rev":"revisionhash"}
	 * 
	 * Create row
	 * POST /{table} -d '{"column1":"value1", "column2":"value2", ...}'
	 * {"ok":true, "id":"value", "rev":"revisionhash"}
	 * 
	 * Delete row
	 * DELETE /{table}/{id}
	 * {"ok":true, "id":"value", "rev":"revisionhash"}
	 * 
	 */

	private ChallengeAuthenticator guard;
	
	private ChallengeAuthenticator createGuard() {
		Context context = getContext();
        boolean optional = true;
        ChallengeScheme challengeScheme = ChallengeScheme.HTTP_BASIC;
        String realm = "Example site";

        // MapVerifier isn't very secure; see docs for alternatives
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("admin", "admin".toCharArray());

        ChallengeAuthenticator auth = new ChallengeAuthenticator(context, optional, challengeScheme, realm, verifier) {
            @Override
            protected boolean authenticate(Request request, Response response) {
                if (request.getChallengeResponse() == null) {
                    return false;
                } else {
                    return super.authenticate(request, response);
                }
            }
        };

        return auth;	
	}
	
	@Override
	public Restlet createInboundRoot() {

		guard = createGuard();
		
		Router router = new Router(getContext());
				
		router.attach("/_all_tables", GlobalTableResource.class);
		router.attach("/_query/{id}", QueryResource.class);
		router.attach("/{table}/_all_rows", FullTableResource.class);
		router.attach("/{table}/{id}", RowResource.class);
		router.attach("/{table}", TableResource.class);

		guard.setNext(router);

		return guard;
	}
	
    public boolean authenticate(Request request, Response response) {
        if (!request.getClientInfo().isAuthenticated()) {
            if(guard != null) guard.challenge(response, false);
            return false;
        }
        return true;
    }
	
}
