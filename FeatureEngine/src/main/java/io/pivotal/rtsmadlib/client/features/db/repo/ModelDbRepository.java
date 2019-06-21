/**********************************************************************************************
 MIT License

 Copyright (c) 2019 Pivotal

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 *********************************************************************************************/

package io.pivotal.rtsmadlib.client.features.db.repo;

import static io.pivotal.rtsmadlib.client.features.db.repo.PostgresCatalogQueries.functionFullMetaExtractQuery;
import static io.pivotal.rtsmadlib.client.features.db.repo.PostgresCatalogQueries.functionMetaExtractQuery;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import io.pivotal.rtsmadlib.client.features.meta.ApplicationProperties;
import io.pivotal.rtsmadlib.client.features.model.FunctionDefinition;
/**
 * @author Sridhar Paladugu
 *
 */
@Repository
public class ModelDbRepository {

	static final Log log = LogFactory.getLog(ModelDbRepository.class.getName());

	@Autowired
	@Qualifier("modeldbJdbcTemplate")
	JdbcTemplate modeldbJdbcTemplate;

	@Autowired
	ApplicationProperties appProps;

	/**
	 * extract full function metadata Return a Map of key value pairs for full
	 * function definition
	 */
	public List<Map<String, Object>> fetchFunctionDef() {
		List<Map<String, Object>> restults = new ArrayList<Map<String, Object>>();
		log.debug("querying source database funrtion definitions....");
		for (String functionName : appProps.getFeatureFunctions()) {
			String sqlQry = (functionFullMetaExtractQuery.replace("<FUNC_NAME>", functionName)).replace("<SCHEMA_NAME>",
					appProps.getFeaturesSchema());
			modeldbJdbcTemplate.query(sqlQry, (ResultSet rs) -> {
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
				restults.add(map);
			});
		}
		return restults;
	}

	/**
	 * Extract the function name, language type, input params and output params, and
	 * source
	 * 
	 * @return Map of FunctionDefinition class.
	 */
	public List<FunctionDefinition> fetchFunctionDefinitions() {
		List<FunctionDefinition> functions = new ArrayList<FunctionDefinition>();
		log.debug("querying source database funrtion definitions....");
		for (String functionName : appProps.getFeatureFunctions()) {
			String sqlQry = (functionMetaExtractQuery.replace("<FUNC_NAME>", functionName)).replace("<SCHEMA_NAME>",
					appProps.getFeaturesSchema());
			modeldbJdbcTemplate.query(sqlQry, (ResultSet rs) -> {
				FunctionDefinition fd = new FunctionDefinition();
				fd.setSchema(rs.getString("Schema"));
				fd.setName(rs.getString("Name"));
				fd.setInputArgs(rs.getString("Argument data types"));
				fd.setOutputArgs(rs.getString("Result data type"));
				fd.setLanguage(rs.getString("Language"));
				fd.setSrc(rs.getString("Source code"));
				functions.add(fd);
			});
		}
		return functions;
	}
	/**
	 * Extract function definitions from source database catalog and
	 * reassemble the code
	 * @return map of function source
	 */
	public Map<String, String> getFunctionDefinitions() {
		Map<String, String> functions = new HashMap<String, String>();
		List<FunctionDefinition> defs = fetchFunctionDefinitions();
		defs.forEach(fd -> {
			functions.put(fd.getSchema()+"."+fd.getName(), constructFunctionDefinition(fd));
		});
		return functions;
	}
	
	public String constructFunctionDefinition(FunctionDefinition fdMeta) {
		if (fdMeta == null) {
			throw new RuntimeException("Function metadata is null.");
		}
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE OR REPLACE FUNCTION ");
		sb.append(fdMeta.getSchema()).append(".").append(fdMeta.getName());
		sb.append(" ( ").append(fdMeta.getInputArgs()).append(" ) ");
		if (null != fdMeta.getOutputArgs()) {
			sb.append(" RETURNS ");
			sb.append(fdMeta.getOutputArgs());
		}
		sb.append(" AS $$ ");
		sb.append(fdMeta.getSrc());
		sb.append(" $$ LANGUAGE ");
		sb.append((fdMeta.getLanguage().replace("{", "")).replace("}", ""));
		return sb.toString();
	}
}