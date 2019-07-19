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

package io.pivotal.rtsmadlib.workflow.meta;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Sridhar Paladugu
 * Pivotal Software Inc.
 *
 */

@Component
@ConfigurationProperties(prefix="app")
public class ApplicationProperties {
	String featureEngineEndpoint;
	String modelEndpoint;
	public String getFeatureEngineEndpoint() {
		return featureEngineEndpoint;
	}
	public void setFeatureEngineEndpoint(String featureEngineEndpoint) {
		this.featureEngineEndpoint = featureEngineEndpoint;
	}
	public String getModelEndpoint() {
		return modelEndpoint;
	}
	public void setModelEndpoint(String modelEndpoint) {
		this.modelEndpoint = modelEndpoint;
	}
}
