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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.pivotal.rtsmadlib.client.features.cache.repo.MADlibFeaturesGemfireRepository;
import io.pivotal.rtsmadlib.client.features.model.MADlibFeature;


/**
 * @author Sridhar Paladugu
 *
 */
@Component
@Profile("gemfire")
public class GemfireLoader implements CacheLoader {
	@Autowired
	MADlibFeaturesGemfireRepository madlibFeaturesCacheRepo;	
	@Override
	public void saveFeature(MADlibFeature feature){
		madlibFeaturesCacheRepo.save(feature);
	}
	@Override
	public void saveFeatures(List<MADlibFeature> features) {
		madlibFeaturesCacheRepo.saveAll(features);		
	}
	
	@Override
	public Long keyCount() {
		return madlibFeaturesCacheRepo.count();		
	}
}
