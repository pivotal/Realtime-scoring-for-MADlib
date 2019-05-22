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
	}
}
