/**********************************************************************************************
   Copyright 2019 Pivotal Software

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 *********************************************************************************************/

package io.pivotal.rtsmadlib.plpymodel.db.repo;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.postgresql.jdbc.PgArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.pivotal.rtsmadlib.plpymodel.meta.AppProperties;

/**
 * @author sridhar paladugu
 *
 */
@Repository
public class ContainerDbRepository {

	static final Log log = LogFactory.getLog(ContainerDbRepository.class.getName());

	@Autowired
	AppProperties appProps;

	@Autowired
	@Qualifier("containerdbJdbcTemplate")
	JdbcTemplate containerdbJdbcTemplate;

	@Autowired
	@Qualifier("containerdbNPJdbcTemplate")
	NamedParameterJdbcTemplate containerdbNPJdbcTemplate;

	TableDefinition actorTableMetadata;
	
	TableDefinition resultTableMetadata;
	
	public TableDefinition getActorTableMetadata() {
		return actorTableMetadata;
	}

	public void setActorTableMetadata(TableDefinition actorTableMetadata) {
		this.actorTableMetadata = actorTableMetadata;
	}

	public TableDefinition getResultTableMetadata() {
		return resultTableMetadata;
	}

	public void setResultTableMetadata(TableDefinition resultTableMetadata) {
		this.resultTableMetadata = resultTableMetadata;
	}

	public void runDDL(String ddl) {
		containerdbJdbcTemplate.execute(ddl);
	}

	public SimpleJdbcInsert jdbcInsert(String tableName) {
		return new SimpleJdbcInsert(containerdbJdbcTemplate.getDataSource()).withTableName(tableName);
	}

	public void createPLPythonExtension() {
		containerdbJdbcTemplate.execute("CREATE EXTENSION plpython3u");
	}

	/**
	 * This method is a bootstrap for container postgres engine. This create a model
	 * schema and inserts the model and create functions for operation.
	 * 
	 * @param model
	 * @param functionDefs
	 */
	@Transactional
	public void importModelAndDriverFunction(byte[] model, Map<String, String> functionDefs,
			DriverFunctionReturnType driverFunctionReturnType) {
		// drop schema
		String dropSql = "drop schema if exists " + appProps.getModelSchema() +" cascade; "
				+ " drop schema if exists " + appProps.getModelRepoSchema() + " cascade; ";
		containerdbJdbcTemplate.execute(dropSql);
		// create schema
		String cSql = "create schema " + appProps.getModelSchema() + "; "
				+ " create schema " + appProps.getModelRepoSchema()+ ";";
		containerdbJdbcTemplate.execute(cSql);
		if(driverFunctionReturnType != null) {
			this.createFunctionReturnType(driverFunctionReturnType);
		}
		// create model repository table
		StringBuffer mtSql = new StringBuffer();
		mtSql.append("create table ")
				.append(appProps.getModelRepoSchema())
				.append(".")
				.append(appProps.getModelRepoTable())
				.append(" ( id serial primary key,")
				.append(" model_name text,")
				.append(" model bytea not null, ")
				.append(" model_description text not null,")
				.append(" model_version int not null ) ");
		containerdbJdbcTemplate.execute(mtSql.toString());
		// insert data to model table
		StringBuffer miSql = new StringBuffer();
		miSql.append("insert into ")
				.append(appProps.getModelRepoSchema())
				.append(".")
				.append(appProps.getModelRepoTable())
				.append(" ( model_name, model, model_description, model_version) ")
				.append(" values(:modelname, :model, :modeldesc, :modelversion)");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("modelname", appProps.getModelName());
		paramMap.put("model", model);
		paramMap.put("modeldesc", appProps.getModelDescription());
		paramMap.put("modelversion", appProps.getModelVersion());
		containerdbNPJdbcTemplate.update(miSql.toString(), paramMap);
		// create all functions imported
		functionDefs.forEach((functionName, functionSource) -> {
			containerdbJdbcTemplate.execute(functionSource);
		});
	}

	@Transactional
	public void createPayloadTable(TableDefinition payloadTableDef) {
		actorTableMetadata = payloadTableDef;
		StringBuffer sqb = new StringBuffer();
		sqb.append("CREATE TABLE ")
			.append(actorTableMetadata.schemaName)
			.append(".")
			.append(actorTableMetadata.getTableName())
			.append(" ( ");
		for (int i = 0; i < actorTableMetadata.getColumns().size(); i++) {
			TableColumn tc = actorTableMetadata.getColumns().get(i);
			sqb.append(tc.getColumnName()).append(" ").append(tc.getColumnDataType()).append(" ")
					.append(tc.getNullCondition());
			if (i != actorTableMetadata.getColumns().size() - 1)
				sqb.append(", ");
		}
		;
		sqb.append(" ) ");
		containerdbJdbcTemplate.execute(sqb.toString());
	}

	@Transactional
	public void createResultTable(TableDefinition resultTableDef) {
		resultTableMetadata = resultTableDef;
		StringBuffer sqb = new StringBuffer();
		sqb.append("CREATE TABLE ")
			.append(resultTableMetadata.schemaName)
			.append(".")
			.append(resultTableMetadata.getTableName())
			.append(" ( ");
		for (int i = 0; i < resultTableMetadata.getColumns().size(); i++) {
			TableColumn tc = resultTableMetadata.getColumns().get(i);
			sqb.append(tc.getColumnName()).append(" ").append(tc.getColumnDataType()).append(" ")
					.append(tc.getNullCondition());
			if (i != resultTableMetadata.getColumns().size() - 1)
				sqb.append(", ");
		}
		;
		sqb.append(" ) ");
		containerdbJdbcTemplate.execute(sqb.toString());
	}

	
	
	public  void createFunctionReturnType(DriverFunctionReturnType functionReturnType) {		
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TYPE ")
		.append(functionReturnType.schemaName)
		.append(".")
		.append(functionReturnType.getTypeName())
		.append(" AS ( ");
	for (int i = 0; i < functionReturnType.getColumns().size(); i++) {
		TableColumn tc = functionReturnType.getColumn(++i);
		--i;
		sb.append(tc.getColumnName())
			.append(" ")
			.append(tc.getColumnDataType());
		if (i != functionReturnType.getColumns().size() - 1)
			sb.append(", ");
	}
	;
	sb.append(" ) ");
	containerdbJdbcTemplate.execute(sb.toString());
	}
	
	/**
	 * Run the prediction algorithm using the payload. The method does below; 
	 * 1. CTAS using actor table for current Transaction 
	 * 2. Insert payload to the new actor table 
	 * 3. String replacement of actor table in action query. 
	 * 4. String replacement of results table in results query(if provided) 
	 * 5. String replacement of results table if provided. 
	 * 6. Cleanup all the resources after the execution.
	 * 
	 * @param payload
	 * @return
	 */
	@Transactional
	public List<Map<String, Object>> runPrediction(Map<String, Object> payload) {
		String inputCloneTable = null;
		String resultsCloneTable = null;
		try {
			inputCloneTable = getInputCloneTableName();
			resultsCloneTable =  getResultsCloneTableName();
			//create model input table
			String actorCloneSQL = getCloneTableSQL(appProps.getPayloadTable(), inputCloneTable);
			containerdbJdbcTemplate.execute(actorCloneSQL);
			//create model output table
			String resultsCloneSQL = getCloneTableSQL(appProps.getResultsTable(), resultsCloneTable);
			containerdbJdbcTemplate.execute(resultsCloneSQL);
			//populate model input table
			storeParameters(inputCloneTable, payload);
			//prediction query
			String modelActionQuery = replaceTablesWithClone(appProps.getModelQuery(), inputCloneTable, resultsCloneTable);
			/*
			 * if results query is mentioned that means return query results. if only
			 * results table is mentioned that means return * from table. if non present
			 * then the output is from the modelQuery.
			 */
			List<Map<String, Object>> restults = null;
			
			if ((appProps.getResultsTable() != null && !appProps.getResultsTable().isEmpty())) {
				// result table case
				containerdbJdbcTemplate.execute(modelActionQuery);
				String resultsQuery = "select * from " + appProps.getModelSchema() + "." + resultsCloneTable;
				restults = runQueryAndMapResults(resultsQuery);
			}else {	
				// straight modelQuery case.
				restults = runQueryAndMapResults(modelActionQuery);
				}
			return restults;
		} finally {
			// drop clone table.
			String dropCloneSQL = null;
			if (resultsCloneTable == null) {
				dropCloneSQL = "DROP TABLE IF EXISTS " + appProps.getModelSchema() + "." + inputCloneTable;
			} else {
				dropCloneSQL = "DROP TABLE IF EXISTS " + appProps.getModelSchema() + "." + inputCloneTable + ","
						+ appProps.getModelSchema() + "." + resultsCloneTable;
			}
			containerdbJdbcTemplate.execute(dropCloneSQL);
		}

	}
	
	private String getInputCloneTableName() {
		StringBuffer cloneTable = new StringBuffer().append(appProps.getPayloadTable()).append("_")
				.append((UUID.randomUUID().toString()).replaceAll("-", "_"));
		return cloneTable.toString();
	}
	
	private String getResultsCloneTableName() {
		StringBuffer cloneTable = new StringBuffer().append(appProps.getResultsTable()).append("_")
				.append((UUID.randomUUID().toString()).replaceAll("-", "_"));
		return cloneTable.toString();
	}

	private String getCloneTableSQL(String originalTableName, String cloneTableName) {
		StringBuffer cloneTableSQL = new StringBuffer();
		cloneTableSQL.append("CREATE TABLE ")
			.append(appProps.getModelSchema())
			.append(".")
			.append(cloneTableName)
			.append(" AS ")
			.append(" SELECT * FROM ")
			.append(appProps.getModelSchema())
			.append(".")
			.append(originalTableName);
		return cloneTableSQL.toString();
	}
	
	/**
	 * Store incoming payload to actor table
	 * 
	 * @param actorTable
	 * @param payload
	 */
	@Transactional
	private void storeParameters(String actorTable, Map<String, Object> payload) {
		Object[] args = new Object[payload.size()];
		Map<String, Object> parameters = new HashMap<String, Object>();
		processPayloadForInsert(payload, parameters);
		StringBuffer qryPart1 = new StringBuffer("insert into ");
		StringBuffer qryPart2 = new StringBuffer(" values (");
		qryPart1.append(appProps.getModelSchema() + "." + actorTable).append("(  ");
		int c = 0;
		for (Iterator<String> iter = payload.keySet().iterator(); iter.hasNext();) {
			String column = iter.next();
			qryPart1.append("\"").append(column).append("\"");
			qryPart2.append("?");
			if (iter.hasNext()) {
				qryPart1.append(",");
				qryPart2.append(",");
			}
			args[c] = parameters.get(column);
			c++;
		}
		qryPart1.append(") ");
		qryPart2.append(")");
		String sqlString = qryPart1.append(qryPart2.toString()).toString();
		containerdbJdbcTemplate.update(sqlString, args);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void processPayloadForInsert(Map<String, Object> payload, Map<String, Object> parameters) {
		for (String key : payload.keySet()) {
			Object value = payload.get(key);
			// process nested elements
			if (value instanceof Map) {
				processPayloadForInsert((Map) value, parameters);
			} else {
				if (value instanceof ArrayList) {
					parameters.put(key, this.createSqlArray(((ArrayList) value).toArray()));
				} else
					parameters.put(key.toLowerCase(), value);
			}
		}
	}
	
	public java.sql.Array createSqlArray(Object[] array) {
		String type = null;
		Object v = array[0];
		if (v instanceof Boolean) {
			type = "BIT";
		} else if (v instanceof Integer) {
			type = "INT";
		} else if (v instanceof Long) {
			type = "BIGINT";
		} else if (v instanceof Double) {
			type = "DOUBLE";
		} else if (v instanceof String) {
			type = "TEXT";
		} else if (v instanceof Character) {
			type = "CHAR";
		} else if (v instanceof Timestamp) {
			type = "TIMESTAMP";
		} else if (v instanceof Date || v instanceof java.sql.Date) {
			type = "DATE";
		}
		Array dSqlArray;
		try {
			dSqlArray = containerdbJdbcTemplate.getDataSource().getConnection().createArrayOf(type, array);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return dSqlArray;
	}

	/**
	 * replace the input and result tables in model query with instance tables.
	 * @param originalQry
	 * @param actorCloneTable
	 * @param resultsCloneTable
	 * @return {@link String} 
	 */
	private String replaceTablesWithClone(String originalQry, String actorCloneTable, String resultsCloneTable) {
		String inputTableName = "\\b" + appProps.getModelSchema() + "." + appProps.getPayloadTable() + "\\b";
		String cloneInputTableName = appProps.getModelSchema() + "." + actorCloneTable;
		String qry1 = originalQry.replaceAll(inputTableName, cloneInputTableName);
		
		String resultTableName = "\\b" + appProps.getModelSchema() + "." + appProps.getResultsTable() + "\\b";
		String cloneResultTableName = appProps.getModelSchema() + "." + resultsCloneTable;
		String qry = qry1.replaceAll(resultTableName, cloneResultTableName);
		
		return qry;
	}

	private List<Map<String, Object>> runQueryAndMapResults(String query) {
		List<Map<String, Object>> restults = new ArrayList<Map<String, Object>>();
		containerdbJdbcTemplate.query(query, (ResultSet rs) -> {
			Map<String, Object> map = new HashMap<String, Object>();
			ResultSetMetaData rmd = rs.getMetaData();
			List<String> columns = new ArrayList<String>();
			for (int i = 1; i <= rmd.getColumnCount(); i++) {
				columns.add(rmd.getColumnLabel(i));
			}
			for (String k : columns) {
				Object v = rs.getObject(k);
				if( v instanceof PgArray) {
					map.put(k, (Object[]) ((PgArray)v).getArray());
				}else {
				map.put(k, rs.getObject(k));
				}
			}
			restults.add(map);
		});
		return restults;
	}
}
