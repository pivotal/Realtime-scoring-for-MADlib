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
package io.pivotal.rtsmadlib.client.features.meta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author sridhar paladugu
 *
 */
@Component
@ConfigurationProperties(prefix = "feature-cache")
public class ApplicationProperties {
	String featureName;
	String featureSourceSchema;
	Integer loadBatchSize;
	private List<String> featureFunctions;
	
	Map<String, String> featureSourceTables;

	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public String getFeatureSourceSchema() {
		return featureSourceSchema;
	}

	public void setFeatureSourceSchema(String featureSourceSchema) {
		this.featureSourceSchema = featureSourceSchema;
	}

	public Map<String, String> getFeatureSourceTables() {
		return featureSourceTables;
	}

	public void setFeatureSourceTables(Map<String, String> featureSourceTables) {
		this.featureSourceTables = featureSourceTables;
	}

	public void addFeatureSourceTable(String table, String idColumn) {
		if (null == this.featureSourceTables) {
			featureSourceTables = new HashMap<String, String>();
		}
		featureSourceTables.put(table, idColumn);
	}

	public Integer getLoadBatchSize() {
		return loadBatchSize;
	}

	public void setLoadBatchSize(Integer loadBatchSize) {
		this.loadBatchSize = loadBatchSize;
	}

	public List<String> getFeatureFunctions() {
		return featureFunctions;
	}

	public void setFeatureFunctions(List<String> featureFunctions) {
		this.featureFunctions = featureFunctions;
	}

}
