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
package io.pivotal.rtsmadlib.batch.mlmodel.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import io.pivotal.rtsmadlib.batch.mlmodel.model.ModelScore;
import io.pivotal.rtsmadlib.batch.mlmodel.model.ModelScoreKey;


/**
 * @author Sridhar Paladugu
 *
 */
@Component
@Profile("redis")
public class RedisLoader implements CacheLoader {
	static final Log log = LogFactory.getLog(RedisLoader.class.getName());
	@Autowired
	RedisTemplate<ModelScoreKey, ModelScore> redisTemplate;

	@Override
	public void saveModelScore(ModelScore modelScore) {
		redisTemplate.opsForValue().set(modelScore.getModelScoreKey(), modelScore);
	}

	@Override
	public void saveModelScores(List<ModelScore> modelScores) {
		Map<ModelScoreKey, ModelScore> map = new HashMap<ModelScoreKey, ModelScore>();
		modelScores.forEach(modelScore -> {
			map.put(modelScore.getModelScoreKey(), modelScore);
		});
		redisTemplate.opsForValue().multiSet(map);
		log.debug("KEYS => "+ keyCount());
	}

	public Long keyCount() {
		Long size = redisTemplate.getConnectionFactory().getConnection().dbSize();
		return size;
	}
	public void printKeys(String scoreName) {
		ModelScoreKey keyPattern = new ModelScoreKey();
		keyPattern.setScoreName(scoreName);
		Set<ModelScoreKey> keys = redisTemplate.keys(keyPattern);
		for(Object k: keys) {
			log.debug(k.getClass());
			log.debug(k.toString());
		}
	}
	
}
