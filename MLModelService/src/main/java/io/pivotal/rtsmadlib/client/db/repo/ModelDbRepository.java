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
