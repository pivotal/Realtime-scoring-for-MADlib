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

package io.pivotal.rtsmadlib.client.features.cache;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author sridhar paladugu
 *
 */
@Component
@Profile("gemfire")
public class GemfireCacheProvider {

	static final Log log = LogFactory.getLog(GemfireCacheProvider.class.getName());
	@Autowired
	ClientCache clientCache;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void storeInRegion(List<Map<String, Object>> records, String regionName) {
		Region region = null;

		region = clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create(regionName);
		for (Map map : records) {
			region.putAll(map);
		}
	}
}
