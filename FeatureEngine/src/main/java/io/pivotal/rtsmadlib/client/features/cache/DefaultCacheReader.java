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

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.pivotal.rtsmadlib.client.features.model.MADlibFeature;
import io.pivotal.rtsmadlib.client.features.model.MADlibFeatureKey;

/**
 * @author sridhar paladugu
 *
 */
@Component
@Profile(value = {"nocache", "test"})
public class DefaultCacheReader implements CacheReader {
	
	//TODO: need to find a better implementation to handle no cache
	
	/* (non-Javadoc)
	 * @see io.pivotal.madlib.client.features.service.CacheLoader#lookUpFeatures(io.pivotal.madlib.client.features.model.MADlibFeatureKey)
	 */
	@Override
	public MADlibFeature lookUpFeature(MADlibFeatureKey key) {
		// TODO Auto-generated method stub
		return null;
	}

}
