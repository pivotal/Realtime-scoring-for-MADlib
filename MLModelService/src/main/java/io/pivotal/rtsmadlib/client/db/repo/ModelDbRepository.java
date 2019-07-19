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

import io.pivotal.rtsmadlib.client.meta.AppProperties;


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
	AppProperties appProps;

	public List<Map<String, Object>> fetchModel() {
		List<Map<String, Object>> restults = new ArrayList<Map<String, Object>>();
		log.debug("querying source database model table ....");
		for(String modelTable: appProps.getModeltables()) {
			String sqlQry = "SELECT * FROM " + appProps.getModelSchema() + "." + modelTable;
			modeldbJdbcTemplate.query(sqlQry, (ResultSet rs) -> {
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
			});
		}
		return restults;
	}
	
	public void runDDL(String sql) {
		modeldbJdbcTemplate.execute(sql);
	}
}
