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
package io.pivotal.rtsmadlib.client.features.cache;

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

import io.pivotal.rtsmadlib.client.features.model.MADlibFeature;
import io.pivotal.rtsmadlib.client.features.model.MADlibFeatureKey;


/**
 * @author Sridhar Paladugu
 *
 */
@Component
@Profile("redis")
public class RedisLoader implements CacheLoader {
	static final Log log = LogFactory.getLog(RedisLoader.class.getName());
	@Autowired
	RedisTemplate<MADlibFeatureKey, MADlibFeature> redisTemplate;

	@Override
	public void saveFeature(MADlibFeature feature) {
		redisTemplate.opsForValue().set(feature.getFeaureKey(), feature);
	}

	@Override
	public void saveFeatures(List<MADlibFeature> features) {
		Map<MADlibFeatureKey, MADlibFeature> map = new HashMap<MADlibFeatureKey, MADlibFeature>();
		features.forEach(feature -> {
			map.put(feature.getFeaureKey(), feature);
		});
		redisTemplate.opsForValue().multiSet(map);
		log.debug("KEYS => "+ keyCount());
	}

	public Long keyCount() {
		Long size = redisTemplate.getConnectionFactory().getConnection().dbSize();
		return size;
	}
	public void printKeys(String featureName) {
		MADlibFeatureKey keyPattern = new MADlibFeatureKey();
		keyPattern.setFeatureName(featureName);
		Set<MADlibFeatureKey> keys = redisTemplate.keys(keyPattern);
		for(Object k: keys) {
			log.debug(k.getClass());
			log.debug(k.toString());
		}
	}
	
}
