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

package io.pivotal.rtsmadlib.client.db.repo;

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

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import io.pivotal.rtsmadlib.client.meta.AppProperties;

/**
 * @author Sridhar Paladugu
 *
 */
@Repository
public class ContainerDbRepository {

	static final Log log = LogFactory.getLog(ContainerDbRepository.class.getName());
	static final Map<String, String> typeMaps = new HashMap<String, String>();
	/*
	    This is not 100% java to sql data type maps; 
		used for only payload JSON validation
	*/
	static {
		
		typeMaps.put("int4", "java.lang.Integer");
		typeMaps.put("int8", "java.lang.Integer");
		typeMaps.put("text", "java.lang.String");
		typeMaps.put("float8", "java.lang.Double");
		typeMaps.put("bool", "java.lang.Boolean");
		typeMaps.put("character", "java.lang.String");
		typeMaps.put("varchar", "java.lang.String");
		typeMaps.put("longvarchar", "java.lang.String");
		typeMaps.put("numeric", "java.math.BigDecimal");
		typeMaps.put("decimal", "java.math.BigDecimal");
		typeMaps.put("bit", "java.lang.Boolean");
		typeMaps.put("tinyint", "java.lang.Integer");
		typeMaps.put("smallint", "java.lang.Integer");
		typeMaps.put("integer", "java.lang.Integer");
		typeMaps.put("bigint", "java.lang.Long");
		typeMaps.put("real", "java.lang.Float");
		typeMaps.put("float", "java.lang.Double");
		typeMaps.put("double precesion", "java.lang.Double");
		typeMaps.put("binary", "java.lang.Byte[]");
		typeMaps.put("varbinary", "java.lang.Byte[]");
		typeMaps.put("longvarbinary", "java.lang.Byte[]");
		typeMaps.put("date", "java.lang.String");
		typeMaps.put("timestamp", "java.lang.String");
		typeMaps.put("time", "java.lang.Long");
	}
	@Autowired
	@Qualifier("containerdbJdbcTemplate")
	JdbcTemplate containerdbJdbcTemplate;

	@Autowired
	AppProperties appProps;

	Map<String, String> actorTableMetada;

	/**
	 * query local database for deployed madlib mode.
	 * 
	 * @return Map of models.
	 */
	@Transactional
	public Map<String, Map<String, Object>> fetchModel() {
		Map<String, Map<String, Object>> models = new HashMap<String, Map<String, Object>>();
		log.debug("querying local postgresql table....");
		appProps.getModeltables().forEach(modelTable -> {
			String sqlQry = "SELECT * FROM " + appProps.getModelSchema() + "." + modelTable;
			containerdbJdbcTemplate.query(sqlQry, (ResultSet rs) -> {
				Map<String, Object> map = new HashMap<String, Object>();
				ResultSetMetaData rmd = rs.getMetaData();
				List<String> columns = new ArrayList<String>();
				for (int i = 1; i <= rmd.getColumnCount(); i++) {
					columns.add(rmd.getColumnLabel(i));
				}
				for (String k : columns) {
					if (rs.getObject(k) instanceof org.postgresql.jdbc.PgArray) {
						map.put(k, rs.getArray(k).getArray());
					} else {
						map.put(k, rs.getObject(k));
					}
				}
				models.put(modelTable, map);
			});

		});

		return models;
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
		String resultsCloneTable = null;
		String actorCloneTable = null;
		try {
			actorCloneTable = getActorCloneTableName();

			if (appProps.getResultsTable() != null && !appProps.getResultsTable().isEmpty()) {
				resultsCloneTable = getResultsCloneTableName();
			}
			String cloneSQL = getCloneTableSQL(actorCloneTable);
			containerdbJdbcTemplate.execute(cloneSQL);
			storeParameters(actorCloneTable, payload);
			String modelActionQuery = replaceTablesWithClone(appProps.getModelQuery(), actorCloneTable,
					resultsCloneTable);

			/*
			 * if results query is mentioned that means return query results. if only
			 * results table is mentioned that means return * from table. if non present
			 * then the output is from the modelQuery.
			 */

			List<Map<String, Object>> restults = null;
			// only results table present
			if ((appProps.getResultsTable() != null && !appProps.getResultsTable().isEmpty())
					&& (appProps.getResultsQuery() == null && appProps.getResultsQuery().isEmpty())) {
				// result table case
				containerdbJdbcTemplate.execute(modelActionQuery);
				String resultsQuery = "select * from " + appProps.getModelSchema() + "." + resultsCloneTable;
				restults = runQueryAndMapResults(resultsQuery);
			} else if (appProps.getResultsQuery() != null && !appProps.getResultsQuery().isEmpty()) {
				// result query case.
				containerdbJdbcTemplate.execute(modelActionQuery);
				String resultsQuery = replaceTablesWithClone(appProps.getResultsQuery(), actorCloneTable,
						resultsCloneTable);
				restults = runQueryAndMapResults(resultsQuery);
			} else {
				// straight modelQuery case.
				restults = runQueryAndMapResults(modelActionQuery);
			}
			return restults;
		} finally {
			// drop clone table.
			String dropCloneSQL = null;
			if (resultsCloneTable == null) {
				dropCloneSQL = "DROP TABLE IF EXISTS " + appProps.getModelSchema() + "." + actorCloneTable;
			} else {
				dropCloneSQL = "DROP TABLE IF EXISTS " + appProps.getModelSchema() + "." + actorCloneTable + ","
						+ appProps.getModelSchema() + "." + resultsCloneTable;
			}
			containerdbJdbcTemplate.execute(dropCloneSQL);
		}

	}

	private String replaceTablesWithClone(String originalQry, String actorCloneTable, String resultsCloneTable) {
		String finalQry = null;
		String aorig = "\\b" + appProps.getModelSchema() + "." + appProps.getModelInputTable() + "\\b";
		String arepl = appProps.getModelSchema() + "." + actorCloneTable;
		String qry1 = originalQry.replaceAll(aorig, arepl);
		if (resultsCloneTable != null) {
			String rorig = "\\b" + appProps.getModelSchema() + "." + appProps.getResultsTable() + "\\b";
			String rrepl = appProps.getModelSchema() + "." + resultsCloneTable;
			String qry2 = qry1.replaceAll(rorig, rrepl);
			finalQry = qry2;
		} else {
			finalQry = qry1;
		}
		return finalQry;
	}

	private String getActorCloneTableName() {
		StringBuffer cloneTable = new StringBuffer().append(appProps.getModelInputTable()).append("_")
				.append((UUID.randomUUID().toString()).replaceAll("-", "_"));
		return cloneTable.toString();
	}

	private String getResultsCloneTableName() {
		StringBuffer cloneTable = new StringBuffer().append(appProps.getResultsTable()).append("_")
				.append((UUID.randomUUID().toString()).replaceAll("-", "_"));
		return cloneTable.toString();
	}

	private String getCloneTableSQL(String cloneTable) {
		StringBuffer cloneTableSQL = new StringBuffer().append("CREATE TABLE ").append(appProps.getModelSchema())
				.append(".").append(cloneTable).append(" AS SELECT * FROM ").append(appProps.getModelSchema())
				.append(".").append(appProps.getModelInputTable());
		return cloneTableSQL.toString();
	}
	@Transactional
	public void fetchActorTableMetada() {
		Map<String, String> columnsMap = new HashMap<String, String>();
		ResultSet columns = null;
		java.sql.Connection conn = null;
		try {
			conn = containerdbJdbcTemplate.getDataSource().getConnection();
			columns = conn.getMetaData()
					.getColumns(null, appProps.getModelSchema(), appProps.getModelInputTable(), "%");
			while (columns.next()) {
				String columnName=columns.getString("COLUMN_NAME");
				String coumnType=columns.getString("TYPE_NAME");
				columnsMap.put(columnName, typeMaps.get(coumnType));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}finally {
			if(conn != null) {
				try {
					conn.close();
				}catch (SQLException e) {
					throw new RuntimeException("Exception while closing connection!", e);
				}
			}
		}
		actorTableMetada = columnsMap;
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
				map.put(k, rs.getObject(k));
			}
			restults.add(map);
		});
		return restults;
	}

	public void runDDL(String sql) {
		containerdbJdbcTemplate.execute(sql);
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
	 * if the payload value is ArrayList then map to postgres arrays.
	 * 
	 * @param sb
	 * @param value
	 */
	private void processArrayList(StringBuffer sb, Object value) {
		Object v = ((ArrayList) value).get(0);
		if (v instanceof Boolean) {
			sb.append(" BIT[] null");
		} else if (v instanceof Integer) {
			sb.append(" INT[] null");
		} else if (v instanceof Long) {
			sb.append(" BIGINT[] null");
		} else if (v instanceof Double) {
			sb.append(" DOUBLE[] PRECISION null");
		} else if (v instanceof String) {
			sb.append(" TEXT[] null");
		}
		if (v instanceof Character) {
			sb.append(" CHAR(1)[] null");
		} else if (v instanceof Timestamp) {
			sb.append(" TIMESTAMP[] null");
		} else if (v instanceof Date || v instanceof java.sql.Date) {
			sb.append(" DATE[] null");
		} else {
			throw new RuntimeException("Unsupported Data type");
		}
	}

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

	public Map<String, String> getActorTableMetada() {
		return actorTableMetada;
	}

}
