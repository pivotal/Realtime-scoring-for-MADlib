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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.pivotal.rtsmadlib.client.features.meta.ApplicationProperties;

/**
 * @author Sridhar Paladugu
 *
 */
@Repository
public class ContainerDbRepository {

	static final Log log = LogFactory.getLog(ContainerDbRepository.class.getName());
	static final Map<String, String> typeMaps = new HashMap<String, String>();
	
	@Autowired
	@Qualifier("containerdbJdbcTemplate")
	JdbcTemplate containerdbJdbcTemplate;
	
	@Autowired
	@Qualifier("containerdbJdbcTemplate")
	NamedParameterJdbcTemplate containerdbNPJdbcTemplate;

	@Autowired
	ApplicationProperties appProps;
	
	@Transactional
	public void crateFunctions(Map<String, String> functionDefs) { 
		functionDefs.forEach((functionName, functionSource) -> {
			containerdbJdbcTemplate.execute(functionSource);
		});
	}
	
	public void crateFunctions(String newSchema, Map<String, String> functionDefs) { 
		functionDefs.forEach((functionName, functionSource) -> {
			String function = functionSource.replace(appProps.getFeaturesSchema(), newSchema);
			containerdbJdbcTemplate.execute(function);
		});
	}
	
	@Transactional
	public void crateSchema(String schemaName) { 
		containerdbJdbcTemplate.execute("create schema "+ schemaName);
	}

	public void createPLPythonExtension() {
		containerdbJdbcTemplate.execute("CREATE EXTENSION plpythonu");
	}
}
