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
