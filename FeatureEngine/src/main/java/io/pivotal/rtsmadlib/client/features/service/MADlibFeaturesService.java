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

package io.pivotal.rtsmadlib.client.features.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pivotal.rtsmadlib.client.features.cache.CacheReader;
import io.pivotal.rtsmadlib.client.features.db.repo.ModelDbRepository;
import io.pivotal.rtsmadlib.client.features.db.repo.PostgresRepository;
import io.pivotal.rtsmadlib.client.features.meta.ApplicationProperties;
import io.pivotal.rtsmadlib.client.features.model.MADlibFeature;
import io.pivotal.rtsmadlib.client.features.model.MADlibFeatureKey;

/**
 * @author sridhar paladugu
 *
 */
@Service
public class MADlibFeaturesService {
	static final Log log = LogFactory.getLog(MADlibFeaturesService.class.getName());
	@Autowired
	PostgresRepository postgresRepository;

	@Autowired
	ApplicationProperties appProps;

	@Autowired
	CacheReader cacheLoader;

	@Autowired
	Environment env;

	@Autowired
	ModelDbRepository modelDbRepository;

	@Transactional(rollbackFor = Throwable.class)
	public List<Map<String, Object>> calculateFeatures(Map<String, Object> payload) {
		// TODO validations and checked exceptions
		String tranKey = UUID.randomUUID().toString().replaceAll("-", "_");
		String schema = appProps.getFeaturesSchema() + tranKey;
		String payloadTbl = appProps.getPayloadTable();
		List<Map<String, Object>> fout = new LinkedList<Map<String, Object>>();
		try {
			// load incoming message to a table. This table name is provided in manifest,
			// Default is message.
			payload.forEach((k, v) -> log.debug((k + " : " + v)));
			loadPayloadToPostgres(payload, schema, payloadTbl);
			// load functions if specified
			if (appProps.getFeatureFunctions() != null && appProps.getFeatureFunctions().size() > 0) {
				postgresRepository.crateFunctions(schema, modelDbRepository.getFunctionDefinitions());
			}
			// load postgresql table with a single cache entry if cache lookup needed
			// this is also instrumented in manifest.
			if (appProps.cacheEnabled) {
				log.info("cache is enabled with " + env.getActiveProfiles()[0] + " ............");
				if (appProps.getCacheEntities() != null && appProps.getCacheEntities().size() > 0) {
					loadFeaturesFromCache(payload, schema);
				} else {
					throw new RuntimeException(
							"Caching enabled, but no Cache Entities Specified. Not processing the request.");
				}

			}
			// run the features query passed in manifest.
			fout = runFeaturesQuery(payloadTbl, schema);
			// cleanup tables data
			log.debug("Cleaning the schema for this transaction ......");
			postgresRepository.runDDL("DROP SCHEMA " + schema + " CASCADE");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return fout;
	}

	/**
	 * @param tranKey
	 * @param payloadTbl
	 * @return
	 */
	private List<Map<String, Object>> runFeaturesQuery(String payloadTbl, String schema) {
		String sql1 = appProps.getFeatureQuery();
		String featueQuery = sql1.replaceAll(appProps.getFeaturesSchema(), schema);
		log.info("Running feature Query => \n\t\t" + featueQuery);
		List<Map<String, Object>> fout = null;
		if (appProps.getFeatureFunctions() != null && appProps.getFeatureFunctions().size() > 0) {
			if (featueQuery.contains(" from ") || featueQuery.contains(" FROM ")) {
				fout = postgresRepository.runFeaturesQuery(featueQuery);
			} else {
				postgresRepository.runFeatureFunction(featueQuery);
				fout = postgresRepository.runFeaturesQuery("select * from " + schema + "." + "out_message");
			}
		} else {
			fout = postgresRepository.runFeaturesQuery(featueQuery);
		}
		return fout;
	}

	/**
	 * @param payload
	 * @param tranKey
	 * @param schema
	 */
	private void loadFeaturesFromCache(Map<String, Object> payload, String schema) {
		log.info("loading cache table entries relavent to this payload.........");
		for (String entity : appProps.getCacheEntities().keySet()) {
			String id = appProps.getCacheEntities().get(entity);
			log.info("entity =" + entity + ", Key = " + id);
			Object o = payload.get(id);
			MADlibFeatureKey key = new MADlibFeatureKey(entity, payload.get(id).toString());
			MADlibFeature feature = cacheLoader.lookUpFeature(key);
			if (feature == null) {
				throw new RuntimeException(" No cache found for key " + key.toString());
			}
			// convert features to a db table
			StringBuffer sb1 = new StringBuffer();
			sb1.append("CREATE TABLE IF NOT EXISTS ").append(schema).append(".").append(entity).append("( ");
			processPayloadForDDL(feature.getFeatureValues(), sb1);
			sb1.append(" ) ");
			postgresRepository.runDDL(sb1.toString());

			// insert data to payload table
			SimpleJdbcInsert fsimpleJdbcInsert = postgresRepository.jdbcInsert(entity).withSchemaName(schema);
			Map<String, Object> fparameters = new HashMap<String, Object>();
			processPayloadForInsert(feature.getFeatureValues(), fparameters);
			fsimpleJdbcInsert.execute(fparameters);
		}
	}

	/**
	 * @param payload
	 * @param tranKey
	 * @param schema
	 * @param payloadTbl
	 */
	private void loadPayloadToPostgres(Map<String, Object> payload, String schema, String payloadTbl) {
		log.debug("loading paylod to postgres table => " + schema + "." + payloadTbl + " .........");
		postgresRepository.runDDL("CREATE SCHEMA IF NOT EXISTS " + schema);
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE IF NOT EXISTS ").append(schema).append(".").append(payloadTbl).append("( ");
		processPayloadForDDL(payload, sb);
		sb.append(" ) ");
		postgresRepository.runDDL(sb.toString());
		// insert payload to message table
		SimpleJdbcInsert simpleJdbcInsert = postgresRepository.jdbcInsert("message").withSchemaName(schema);
		Map<String, Object> parameters = new HashMap<String, Object>();
		processPayloadForInsert(payload, parameters);
		simpleJdbcInsert.execute(parameters);
	}

	/**
	 * Iterate thru payload map and create SQL create statement string.
	 * 
	 * @param payload
	 * @param StringBuilder
	 */
	private void processPayloadForDDL(Map<String, Object> payload, StringBuffer sb) {
		int count = 1;
		for (String key : payload.keySet()) {
			Object value = payload.get(key);
			// process nested elements
			if (value instanceof Map) {
				processPayloadForDDL((Map) value, sb);
			} else {
				sb.append(key.toLowerCase());
				if (value instanceof Boolean) {
					sb.append(" BOOLEAN null");
				} else if (value instanceof Integer) {
					sb.append(" INT null");
				} else if (value instanceof Long) {
					sb.append(" BIGINT null");
				} else if (value instanceof Double) {
					sb.append(" DOUBLE PRECISION null");
				} else if (value instanceof String) {
					sb.append(" TEXT null");
				}
				if (value instanceof Character) {
					sb.append(" CHAR(1) null");
				} else if (value instanceof Timestamp) {
					sb.append(" TIMESTAMP null");
				} else if (value instanceof Date || value instanceof java.sql.Date) {
					sb.append(" DATE null");
				} else if (value instanceof ArrayList) {
					processArrayList(sb, value);
				}
			}
			if (count < payload.size()) {
				sb.append(", ");
				count++;
			}
		}
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
					parameters.put(key, postgresRepository.createSqlArray(((ArrayList) value).toArray()));
				} else
					parameters.put(key.toLowerCase(), value);
			}
		}
	}
}
