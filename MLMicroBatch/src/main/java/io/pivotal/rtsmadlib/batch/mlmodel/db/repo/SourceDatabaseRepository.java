/**********************************************************************************************
Ø   Copyright 2019 Pivotal Software

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

package io.pivotal.rtsmadlib.batch.mlmodel.db.repo;

import java.sql.Array;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import io.pivotal.rtsmadlib.batch.mlmodel.meta.ApplicationProperties;


/**
 * @author sridhar paladugu
 *
 */
@Repository
public class SourceDatabaseRepository {
	static final Log log = LogFactory.getLog(SourceDatabaseRepository.class.getName());

	@Autowired
	ApplicationProperties appProps;
	
	
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	NamedParameterJdbcTemplate namedJdbcTemplate;

	
	public void runDDL(String ddl) {
		jdbcTemplate.execute(ddl);
	}

	public SimpleJdbcInsert jdbcInsert(String tableName) {
		return new SimpleJdbcInsert(jdbcTemplate.getDataSource()).withTableName(tableName);
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
		}else if (v instanceof Character) {
			type = "CHAR";
		} else if (v instanceof Timestamp) {
			type = "TIMESTAMP";
		} else if (v instanceof Date || v instanceof java.sql.Date) {
			type = "DATE";
		}
		Array dSqlArray;
		try {
			dSqlArray = jdbcTemplate.getDataSource().getConnection().createArrayOf(type, array);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return dSqlArray;
	}
	public boolean tableExists(String tableName) {
		DatabaseMetaData dbm;
		Boolean exists = false;
		try {
			dbm = jdbcTemplate.getDataSource().getConnection().getMetaData();
			ResultSet rs = dbm.getTables(null, null, tableName, null);
			if (rs.next()) {
				exists = true;
			} 
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return exists;
	}
	
	public Long fetchRowCount(String schemaName, String featureTableName) {
		log.debug("querying for row count from source table "+featureTableName +"....");
		String sqlQry = "SELECT count(*) FROM " +schemaName +"."+ featureTableName;
		Long rowCount = jdbcTemplate.queryForObject(sqlQry, Long.class);
		return rowCount;
	}
	
	public List<Object> fetchIds(String schemaName, String featureTableName, String pk) {
		log.debug("querying for row count from source table "+featureTableName +"....");
		String sqlQry = "SELECT "+pk+" FROM " +schemaName +"."+ featureTableName+" ORDER BY "+pk+" ASC";
		List<Object> ids = jdbcTemplate.queryForList(sqlQry, Object.class);
		return ids;
	}
	
	public List<Map<String, Object>> fetchModelScores(String schemaName, String featureTableName,
			String pk, List<Object> pkList) {
		List<Map<String, Object>> restults = new ArrayList<Map<String, Object>>();
		log.debug("querying  source table "+featureTableName +" ....");
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("pks", pkList);
		String sqlQry = "SELECT * FROM " +schemaName +"."+ featureTableName+" WHERE "+ pk +" in (:pks)";
		namedJdbcTemplate.query(sqlQry, parameters, (ResultSet rs) -> {
			mapResults(restults, rs);
		});
		return restults;
	}
	
	public List<Map<String, Object>> fetchModelScores(String schemaName, String featureTableName) {
		List<Map<String, Object>> restults = new ArrayList<Map<String, Object>>();
		log.debug("querying  source table "+featureTableName +"....");
		String sqlQry = "SELECT * FROM " +schemaName +"."+ featureTableName;
		jdbcTemplate.query(sqlQry, (ResultSet rs) -> {
			mapResults(restults, rs);
		});
		return restults;
	}

	private void mapResults(List<Map<String, Object>> restults, ResultSet rs) throws SQLException {
		Map<String, Object> map = new HashMap<String, Object>();
		ResultSetMetaData rmd = rs.getMetaData();
		List<String> columns = new ArrayList<String>();
		for (int i = 1; i <= rmd.getColumnCount(); i++) {
			columns.add(rmd.getColumnLabel(i));
		}
		for (String k : columns) {
			if(rs.getObject(k) instanceof org.postgresql.jdbc.PgArray) {
				map.put(k, rs.getArray(k).getArray());
			}else {
				map.put(k, rs.getObject(k));
			}
		}
		restults.add(map);
	}
	
	public int runMicroBatch(String schemaName, String functionName, Map<String, Object> parameters) {
		log.debug(printStr(schemaName, functionName, parameters));
		SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate).withFunctionName(functionName)
				.withSchemaName(schemaName);
		SqlParameterSource params = null;
		if (parameters != null && parameters.size() > 0) {
			params = new MapSqlParameterSource(parameters);
		}
		Integer output = 0;
		if (null != parameters) {
			output = call.executeFunction(Integer.class, params);
		} else {
			output = call.executeFunction(Integer.class);
		}
		log.debug("Finished executing the feature generation function " + schemaName + "." + functionName);
		return output;
	}

	public int runMicroBatch(String schemaName, String functionName) {
		log.debug(printStr(schemaName, functionName, null));
		SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate).withFunctionName(functionName)
				.withSchemaName(schemaName);
		Integer output = call.executeFunction(Integer.class);
		log.debug("Finished executing the feature generation function " + schemaName + "." + functionName);
		return output;
	}

	public String printStr(String schemaName, String functionName, Map<String, Object> parameters) {
		StringBuffer sb = new StringBuffer();
		sb.append("Running the featuren generation function :\n").append(schemaName + "." + functionName);
		if (parameters != null && parameters.size() > 0) {
			sb.append("Parameters :\n\t\t");
			parameters.forEach((key, value) -> {
				sb.append(key + " = " + value).append("\n");
			});
		}
		return sb.toString();

	}
}
