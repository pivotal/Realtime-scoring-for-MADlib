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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import io.pivotal.rtsmadlib.client.features.cache.CacheLoader;
import io.pivotal.rtsmadlib.client.features.db.repo.SourceDatabaseRepository;
import io.pivotal.rtsmadlib.client.features.meta.ApplicationProperties;
import io.pivotal.rtsmadlib.client.features.model.MADlibFeature;
import io.pivotal.rtsmadlib.client.features.model.MADlibFeatureKey;

/**
 * @author sridhar paladugu
 *
 */
@Component
public class MADlibFeaturesLoaderService {
	static final Log log = LogFactory.getLog(MADlibFeaturesLoaderService.class.getName());
	@Autowired
	ApplicationProperties appProps;

	@Autowired
	SourceDatabaseRepository sourceDatabaseRepository;

	@Autowired
	CacheLoader cacheRepo;
	
	public void generateFeatures() {
		String schemaName = appProps.getFeatureSourceSchema();
		appProps.getFeatureFunctions().parallelStream().forEach(functionName -> {
			sourceDatabaseRepository.runFeatureGeneration(schemaName, functionName);
		});
	}

	public void loadFeatures() {
		String schemaName = appProps.getFeatureSourceSchema();
		if (null != appProps.getFeatureSourceTables() && !appProps.getFeatureSourceTables().isEmpty()) {
			for (String featureTableName : appProps.getFeatureSourceTables().keySet()) {
				pumpCache(schemaName, featureTableName, appProps.getFeatureSourceTables().get(featureTableName));
			}
		}
	}

	public void pumpCache(String schemaName, String featureTableName, String pk) {
		List<Object> pks = sourceDatabaseRepository.fetchIds(schemaName, featureTableName, pk);
		if (null == pks || pks.size() < 1)
			return;
		List<List<Object>> groups = Lists.partition(pks, appProps.getLoadBatchSize());
		// TODO: may be create controlled threads ?
		groups.forEach(l -> {
			List<Map<String, Object>> records = sourceDatabaseRepository.fetchFeatures(schemaName, featureTableName, pk,
					l);
			loadCache(records, featureTableName, appProps.getFeatureSourceTables().get(featureTableName));
		});
			
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void loadCache(List<Map<String, Object>> records, String regionName, String keyColumn) {
		List<MADlibFeature> features = new LinkedList<MADlibFeature>();
		for (Map record : records) {
			MADlibFeatureKey featureKey = new MADlibFeatureKey(regionName, String.valueOf(record.get(keyColumn)));
			MADlibFeature feature = new MADlibFeature(featureKey, record);
			features.add(feature);
		}
		cacheRepo.saveFeatures(features);
	}

}
