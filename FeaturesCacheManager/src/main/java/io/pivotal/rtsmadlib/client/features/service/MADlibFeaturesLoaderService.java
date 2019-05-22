/**********************************************************************************************
 Copyright 2019 Sridhar Paladugu

 Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 software and associated documentation files (the "Software"), to deal in the Software 
 without restriction, including without limitation the rights to use, copy, modify, 
 merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
 permit persons to whom the Software is furnished to do so, subject to the following 
 conditions:

 The above copyright notice and this permission notice shall be included in all copies 
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 OTHER DEALINGS IN THE SOFTWARE.
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
