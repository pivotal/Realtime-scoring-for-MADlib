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

package io.pivotal.rtsmadlib.client.features;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.pivotal.rtsmadlib.client.features.db.repo.PostgresRepository;
import io.pivotal.rtsmadlib.client.features.meta.ApplicationProperties;


/**
 * @author Sridhar Paladugu
 *
 */
@Component
public class MADlibFeaturesServiceInformation implements InfoContributor {
 
	@Autowired
	PostgresRepository postgresRepository;
	@Autowired
	ApplicationProperties props;
	@Autowired
	Environment env;
	
	@Override
	public void contribute(Builder builder) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("Feature Name", props.getFeatureName());
		m.put("Description", props.getFeatureDescription());
		if(!props.getCacheEnabled()) {
			m.put("Cache used", false);
		}else {
			m.put("Cache used", true);
			m.put("Cache Provider", env.getActiveProfiles());
			m.put("Cache Entities:", props.getCacheEntities());
		}
		builder.withDetails(m);
	}
}
