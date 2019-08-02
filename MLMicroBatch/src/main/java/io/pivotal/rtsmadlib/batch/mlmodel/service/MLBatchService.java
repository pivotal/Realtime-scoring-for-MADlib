/**
 * 
 */
package io.pivotal.rtsmadlib.batch.mlmodel.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import io.pivotal.rtsmadlib.batch.mlmodel.cache.CacheLoader;
import io.pivotal.rtsmadlib.batch.mlmodel.db.repo.SourceDatabaseRepository;
import io.pivotal.rtsmadlib.batch.mlmodel.meta.ApplicationProperties;
import io.pivotal.rtsmadlib.batch.mlmodel.model.ModelScore;
import io.pivotal.rtsmadlib.batch.mlmodel.model.ModelScoreKey;

/**
 * @author sridhar paladugu
 *
 */
@Component
public class MLBatchService {

	static final Log log = LogFactory.getLog(MLBatchService.class.getName());
	@Autowired
	ApplicationProperties appProps;

	@Autowired
	SourceDatabaseRepository sourceDatabaseRepository;

	@Autowired
	CacheLoader cacheRepo;
	

	public int runBatchFunction() {
		String schemaName = appProps.getSchema();
		String function = appProps.getBatchFunction();
		int result = sourceDatabaseRepository.runMicroBatch(schemaName, function);
		if(result == 1)
			log.debug("Succefully executed batch function.");
		else if (result != 1) {
			log.error("Error occured while executing batch function.");
			throw new RuntimeException("Micro batch function execution failed.");
		}
		return result;
	}
	
	public void runBatch() {
		String schemaName = appProps.getSchema();
		String function = appProps.getBatchFunction();
		int result = sourceDatabaseRepository.runMicroBatch(schemaName, function);
		log.debug("Finished executing features generation function(s).");
		if (result != 1) {
			throw new RuntimeException("Micro batch execution failed.");
		}
		if(appProps.getHydrateCache()) {
			log.debug("hydrating cache....");	
			hydrateCache();
		}
		
	}
	
	public void hydrateCache() {
		String schemaName = appProps.getSchema();
		if (null != appProps.getCacheSourceTables() && !appProps.getCacheSourceTables().isEmpty()) {
			for (String cacheTableName : appProps.getCacheSourceTables().keySet()) {
				pumpCache(schemaName, cacheTableName, appProps.getCacheSourceTables().get(cacheTableName));
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
			List<Map<String, Object>> records = sourceDatabaseRepository.fetchModelScores(schemaName, featureTableName, pk,
					l);
			loadCache(records, featureTableName, appProps.getCacheSourceTables().get(featureTableName));
		});
			
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void loadCache(List<Map<String, Object>> records, String regionName, String keyColumn) {
		List<ModelScore> scores = new LinkedList<ModelScore>();
		for (Map record : records) {
			ModelScoreKey scoreKey = new ModelScoreKey(regionName, String.valueOf(record.get(keyColumn)));
			ModelScore score = new ModelScore(scoreKey, record);
			scores.add(score);
		}
		cacheRepo.saveModelScores(scores);
	}
}
