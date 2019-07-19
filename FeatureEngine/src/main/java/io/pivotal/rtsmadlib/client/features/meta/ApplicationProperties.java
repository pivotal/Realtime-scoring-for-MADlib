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

package  io.pivotal.rtsmadlib.client.features.meta;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * 
 * @author sridhar paladugu
 *
 */
@Component
@ConfigurationProperties(prefix = "feature-engine")
@Getter
@Setter
@ToString
public class ApplicationProperties {

	public String featureName;
	public String featureDescription;
	public String payloadTable;
	public String featureQuery;
	public String featuresSchema;
	public Boolean cacheEnabled;
	public Map<String, String> cacheEntities;
	public List<String> featureFunctions;
}
