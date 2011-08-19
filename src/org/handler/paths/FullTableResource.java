package org.handler.paths;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.handler.utilities.Caster;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;



public class FullTableResource extends ServerResource {
	private String table;

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
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
	 */

	@Override
	protected void doInit() throws ResourceException {
		table = Caster.instance().as(getRequestAttributes().get("table"), String.class);
	}

	@Get()
	public Representation get() {
		Representation result = null;
		Boolean descending = null;
		Integer limit = 0;
		Integer start = 0;
		Boolean include_data = null;

		try {
			Form form = getRequest().getResourceRef().getQueryAsForm();
			for (Parameter parameter : form) {
				if(parameter.getName().toLowerCase().equals("descending")) {
					descending = Boolean.parseBoolean(parameter.getValue().toLowerCase());
				}
				else if(parameter.getName().toLowerCase().equals("limit")) {
					limit = Integer.parseInt(parameter.getValue());
				}
				else if(parameter.getName().toLowerCase().equals("start")) {
					start = Integer.parseInt(parameter.getValue());				
				}
				else if(parameter.getName().toLowerCase().equals("include_data")) {
					include_data = Boolean.parseBoolean(parameter.getValue().toLowerCase());
				}
			}
		}
		catch (NumberFormatException e) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}

		DbSpec spec = new DbSpec();
		DbSchema schema = spec.addDefaultSchema();
		DbTable workingTable = schema.addTable(table);

		String selectWorkingTable = 
			new SelectQuery()
				.addAllTableColumns(workingTable)
				.addFromTable(workingTable)
				.validate()
				.toString();
				
		System.out.println(selectWorkingTable);

		Context initCtx;
		try {
			initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/rest2jdbc");
			Connection conn = ds.getConnection();
			Statement statement = conn.createStatement();
			if(!limit.equals(null)) statement.setMaxRows(limit.intValue());
			statement.execute(selectWorkingTable);
			
			ResultSet resultSet = statement.getResultSet();
			ResultSetMetaData metadata = resultSet.getMetaData();
			
			ArrayList<HashMap<String, Object>> generatedResponse = new ArrayList<HashMap<String,Object>>();
			
			while (resultSet.next()) {
				HashMap<String, Object> row = new HashMap<String, Object>();
				for(int i=1; i<=metadata.getColumnCount(); i++) {
					row.put(metadata.getColumnName(i), resultSet.getObject(i));
				}
				generatedResponse.add(row);
			}
			
			result = new JacksonRepresentation<ArrayList<HashMap<String, Object>>>(generatedResponse);
			  
			conn.close();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	//	@Get()
	//	public String toJson() {
	//		
	//		Context initCtx;
	//		try {
	//			
	//			initCtx = new InitialContext();
	//			Context envCtx = (Context) initCtx.lookup("java:comp/env");
	//			DataSource ds = (DataSource) envCtx.lookup("jdbc/rest2jdbc");
	//			Connection conn = ds.getConnection();
	//			
	//			conn.close();
	//			
	//		} catch (NamingException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		} catch (SQLException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//
	//		
	//		Rest2JdbcApplication app = Caster.instance().as(getApplication(), Rest2JdbcApplication.class);
	//		System.out.println(getRequest().getAttributes());
	//		if(!app.authenticate(getRequest(), getResponse())) {
	//			return "Cannot authenticate";
	//		}
	//		return getTable();
	//	}
}
