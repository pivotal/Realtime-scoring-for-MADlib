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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import io.pivotal.rtsmadlib.client.features.cache.CacheLoader;
import io.pivotal.rtsmadlib.client.features.meta.ApplicationProperties;

/**
 * @author Sridhar Paladugu
 *
 */
@Component
public class MADlibFeatureLoaderServiceInformation implements InfoContributor {

	@Autowired
	CacheLoader cacheLoader;
	@Autowired
	ApplicationProperties props;
	static final Log log = LogFactory.getLog(MADlibFeatureLoaderServiceInformation.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.boot.actuate.info.InfoContributor#contribute(org.
	 * springframework.boot.actuate.info.Info.Builder)
	 */
	@Override
	public void contribute(Builder builder) {
		Long keys = -1l;
		try {
			keys = cacheLoader.keyCount();
		} catch (Exception e) {
			log.error(e);
		}
		builder.withDetail("Feature Cache - ", props.getFeatureName());
		if (null != props.getFeatureFunctions()) {
			props.getFeatureFunctions().forEach(featureFunction -> {
				builder.withDetail("Feature Function - ", featureFunction);
			});
		}
		builder.withDetail("Feature source Schema - ", props.getFeatureSourceSchema());
		props.getFeatureSourceTables().keySet().forEach(table -> {
			builder.withDetail("Feature source table - ", table + " :: " + props.getFeatureSourceTables().get(table));
		});
		builder.withDetail("Feature keys size - ", keys);
	}

}
