package org.handler.paths;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.handler.requests.TableResourcePost;
import org.handler.requests.TableResourcePut;
import org.handler.responses.DefaultResponse;
import org.handler.utilities.Caster;
import org.restlet.data.MediaType;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.DropQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

public class TableResource extends ServerResource {
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
	 * Create table
	 * PUT /{table} -d '{"idColumn":"id","columns":{"id":"INT NOT NULL AUTO_INCREMENT","datecolumn":"DATETIME","intcolumn":"INT"}}'
	 * 
	 * Delete table
	 * DELETE /{table}
	 * 
	 * Create row
	 * POST /{table} -d '{"data":{"datecolumn":"2011-08-18","intcolumn":10}}'
	 * {"ok":true, "id":"value", "rev":"revisionhash"}
	 */

	@Override
	protected void doInit() throws ResourceException {
		table = Caster.instance().as(getRequestAttributes().get("table"), String.class);
	}

	@Delete()
	public Representation delete() {
		Representation result = null;

		String deleteWorkingTable = new DropQuery(DropQuery.Type.TABLE, table).validate().toString();
		System.out.println(deleteWorkingTable);

		Context initCtx;
		try {
			initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/rest2jdbc");
			Connection conn = ds.getConnection();
			conn.createStatement().execute(deleteWorkingTable);

			conn.close();
		} catch (NamingException e) {
			return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
		} catch (SQLException e) {
			return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
		}

		result = new JacksonRepresentation<DefaultResponse>(new DefaultResponse(true, "", ""));
		return result;
	}

	@Post()
	public Representation post(Representation entity) {
		Representation result = null;
		String rowId = null;

		if (entity.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
			JsonFactory f = new JsonFactory();
			ObjectMapper mapper = new ObjectMapper(f);

			TypeReference<TableResourcePost> typeRef = 
				new TypeReference<TableResourcePost>() {};

				Context initCtx;
				try {
					TableResourcePost o = mapper.readValue(entity.getText(), typeRef);
					
					InsertQuery insertWorkingQuery = new InsertQuery(table);
					for(String columnName: o.getData().keySet()) {
						insertWorkingQuery.addCustomColumn(columnName, o.getData().get(columnName));
					}

					System.out.println(insertWorkingQuery.validate().toString());
					
					initCtx = new InitialContext();
					Context envCtx = (Context) initCtx.lookup("java:comp/env");
					DataSource ds = (DataSource) envCtx.lookup("jdbc/rest2jdbc");
					Connection conn = ds.getConnection();
					Statement statement = conn.createStatement(); 
					statement.executeUpdate(insertWorkingQuery.validate().toString(), Statement.RETURN_GENERATED_KEYS);
					
					ResultSet keys = statement.getGeneratedKeys();

					if(keys.next()) {
						rowId = keys.getString(1);
					}
					
					conn.close();

				} catch (NamingException e) {
					return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
				} catch (SQLException e) {
					return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
				} catch (JsonParseException e) {
					return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
				} catch (JsonMappingException e) {
					return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
				} catch (JsonProcessingException e) {
					return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
				} catch (IOException e) {
					return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
				}

				result = new JacksonRepresentation<DefaultResponse>(new DefaultResponse(true, rowId, ""));
		}
		
		return result;
	}
	
	@Put()
	public Representation put(Representation entity) {
		Representation result = null;

		if (entity.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {

			try {
				JsonFactory f = new JsonFactory();
				ObjectMapper mapper = new ObjectMapper(f);

				//				TypeReference<HashMap<String,Object>> typeRef = 
				//					new TypeReference<HashMap<String,Object>>() {};

				TypeReference<TableResourcePut> typeRef = 
					new TypeReference<TableResourcePut>() {};

					TableResourcePut o = mapper.readValue(entity.getText(), typeRef);
					
					if(o.equals(null)) return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());

					DbSpec spec = new DbSpec();
					DbSchema schema = spec.addDefaultSchema();
					DbTable workingTable = schema.addTable(table);
					for(String columnName: o.getColumns().keySet()) {
						workingTable.addColumn(columnName, o.getColumns().get(columnName).toString(), null);						
					}
					if(!o.getIdColumn().equals(null)) {
						workingTable.primaryKey(table +"_" + o.getIdColumn(), o.getIdColumn());
					}

					String createWorkingTable = new CreateTableQuery(workingTable, true).validate().toString();
					System.out.println(createWorkingTable);

					//				jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
					//				while (jp.nextToken() != JsonToken.END_OBJECT) {
					//					String variable = jp.getCurrentName();
					//					String value = jp.getText();
					//					System.out.println(variable + " : " + value);
					//					jp.nextToken();
					//				}

					
					DefaultResponse dr = new DefaultResponse(true, "", "");
					result = new JacksonRepresentation<DefaultResponse>(dr);

//					TableResourcePut trp = new TableResourcePut();
//
//					HashMap<String, Object> map = new HashMap<String, Object>();
//					map.put("id", String.class);
//					map.put("intcolumn", Integer.class);
//					map.put("datecolumn", Date.class);
//
//					trp.setIdColumn("id");
//					trp.setColumns(map);
//
//					result = new JacksonRepresentation<TableResourcePut>(trp);

//					// create default schema
//					DbSpec spec = new DbSpec();
//					DbSchema schema = spec.addDefaultSchema();
//
//					// add table with basic customer info
//					DbTable customerTable = schema.addTable("customer");
//					DbColumn custIdCol = customerTable.addColumn("cust_id", "number", null);
//					DbColumn custNameCol = customerTable.addColumn("name", "varchar", 255);
//
//					// add order table with basic order info
//					DbTable orderTable = schema.addTable("order");
//					DbColumn orderIdCol = orderTable.addColumn("order_id", "number", null);
//					DbColumn orderCustIdCol = orderTable.addColumn("cust_id", "number", null);
//					DbColumn orderTotalCol = orderTable.addColumn("total", "number", null);
//					DbColumn orderDateCol = orderTable.addColumn("order_date", "timestamp", null);
//
//					// add a join from the customer table to the order table (on cust_id)
//					DbJoin custOrderJoin = spec.addJoin(null, "customer",
//							null, "order",
//					"cust_id");
//
//					String createCustomerTable = new CreateTableQuery(customerTable, true).validate().toString();
//					System.out.println(createCustomerTable);
//
//					String createOrderTable = new CreateTableQuery(orderTable, true).validate().toString();
//					System.out.println(createOrderTable);
//
//					String insertCustomerQuery =
//						new InsertQuery(customerTable)
//					.addColumn(custIdCol, 1)
//					.addColumn(custNameCol, "bob")
//					.validate().toString();
//					System.out.println(insertCustomerQuery);
//
//					// => INSERT INTO customer (cust_id,name)
//					//      VALUES (1,'bob')
//
//					String preparedInsertCustomerQuery =
//						new InsertQuery(customerTable)
//					.addPreparedColumns(custIdCol, custNameCol)
//					.validate().toString();
//					System.out.println(preparedInsertCustomerQuery);
//
//					// => INSERT INTO customer (cust_id,name)
//					//      VALUES (?,?)
//
//					String insertOrderQuery =
//						new InsertQuery(orderTable)
//					.addColumn(orderIdCol, 37)
//					.addColumn(orderCustIdCol, 1)
//					.addColumn(orderTotalCol, 37.56)
//					.addColumn(orderDateCol, JdbcEscape.timestamp(new Date()))
//					.validate().toString();
//					System.out.println(insertOrderQuery);
//
//					// => INSERT INTO order (order_id,cust_id,total,order_date)
//					//      VALUES (37,1,37.56,{ts '2008-04-01 14:39:00.914'})
//
//					////
//					// find a customer name by id
//					String query1 =
//						new SelectQuery()
//					.addColumns(custNameCol)
//					.addCondition(BinaryCondition.equalTo(custIdCol, 1))
//					.validate().toString();
//					System.out.println(query1);
//
//					// => SELECT t0.name FROM customer t0
//					//      WHERE (t0.cust_id = 1)
//
//					////
//					// find all the orders for a customer, given name, order by date
//					String query2 =
//						new SelectQuery()
//					.addAllTableColumns(orderTable)
//					.addJoins(SelectQuery.JoinType.INNER, custOrderJoin)
//					.addCondition(BinaryCondition.equalTo(custNameCol, "bob"))
//					.addOrderings(orderDateCol)
//					.validate().toString();
//					System.out.println(query2);
//
//					// => SELECT t1.*
//					//      FROM customer t0 INNER JOIN order t1 ON (t0.cust_id = t1.cust_id)
//					//      WHERE (t0.name = 'bob')
//					//      ORDER BY t1.order_date
//
//					////
//					// find the totals of all orders for people named bob who spent over $100
//					// this year, grouped by name
//					String query3 =
//						new SelectQuery()
//					.addCustomColumns(
//							custNameCol,
//							FunctionCall.sum().addColumnParams(orderTotalCol))
//							.addJoins(SelectQuery.JoinType.INNER, custOrderJoin)
//							.addCondition(BinaryCondition.like(custNameCol, "%bob%"))
//							.addCondition(BinaryCondition.greaterThan(
//									orderDateCol,
//									JdbcEscape.date(new Date(108, 0, 1)), true))
//									.addGroupings(custNameCol)
//									.addHaving(BinaryCondition.greaterThan(
//											FunctionCall.sum().addColumnParams(orderTotalCol),
//											100, false))
//											.validate().toString();
//					System.out.println(query3);
//
//					// => SELECT t0.name,SUM(t1.total)
//					//      FROM customer t0 INNER JOIN order t1 ON (t0.cust_id = t1.cust_id)
//					//      WHERE ((t0.name LIKE '%bob%') AND (t1.order_date >= {d '2008-01-01'}))
//					//      GROUP BY t0.name
//					//      HAVING (SUM(t1.total) > 100)
//
//					////
//					// find addresses for customers from PA,NJ,DE from table:
//					//   address(cust_id, street, city, state, zip)
//					String customQuery1 =
//						new SelectQuery()
//					.addCustomColumns(
//							custNameCol,
//							new CustomSql("a1.street"),
//							new CustomSql("a1.city"),
//							new CustomSql("a1.state"),
//							new CustomSql("a1.zip"))
//							.addCustomJoin(SelectQuery.JoinType.INNER, customerTable,
//									"address a1",
//									BinaryCondition.equalTo(custIdCol,
//											new CustomSql("a1.cust_id")))
//											.addCondition(new InCondition("a1.state",
//													"PA", "NJ", "DE"))
//													.validate().toString();
//					System.out.println(customQuery1);
//
//					// => SELECT t0.name,a1.street,a1.city,a1.state,a1.zip
//					//      FROM customer t0 INNER JOIN address a1 ON (t0.cust_id = a1.cust_id)
//					//      WHERE ('a1.state' IN ('PA','NJ','DE') )
					Context initCtx;
					initCtx = new InitialContext();
					Context envCtx = (Context) initCtx.lookup("java:comp/env");
					DataSource ds = (DataSource) envCtx.lookup("jdbc/rest2jdbc");
					Connection conn = ds.getConnection();

					conn.createStatement().execute(createWorkingTable);

					conn.close();

			} catch (NamingException e) {
				return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
			} catch (SQLException e) {
				return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
			} catch (JsonParseException e) {
				return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
			} catch (JsonMappingException e) {
				return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
			} catch (JsonProcessingException e) {
				return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
			} catch (IOException e) {
				return new JacksonRepresentation<DefaultResponse>(new DefaultResponse());
			}
		}
		return result;
	}
}
