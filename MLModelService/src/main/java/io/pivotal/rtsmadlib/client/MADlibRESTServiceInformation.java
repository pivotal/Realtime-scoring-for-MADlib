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

package io.pivotal.rtsmadlib.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import io.pivotal.rtsmadlib.client.db.repo.ContainerDbRepository;
import io.pivotal.rtsmadlib.client.meta.AppProperties;


/**
 * @author Sridhar Paladugu
 *
 */
@Component
public class MADlibRESTServiceInformation implements InfoContributor {
 
	@Autowired
	ContainerDbRepository containerDbRepository;
	@Autowired
	AppProperties props;
	
	@Override
	public void contribute(Builder builder) {
		builder.withDetail("MADlib Model - Name", props.getModelName());
		builder.withDetail("Description", props.getModelDescription());
		StringBuffer tbls = new StringBuffer();
		props.getModeltables().forEach(table-> {
			tbls.append("   ").append(table);
		});
		builder.withDetail("Model Table(s)", props.getModeltables());
		builder.withDetail("Actor Table", props.getModelInputTable());
		builder.withDetail("Results Table", props.getResultsTable());
		builder.withDetail("Results Query", props.getResultsQuery());
		
//		Map<String, Map<String, Object>> m = containerDbRepository.fetchModel();
//		m.forEach((k,v)->{
//			builder.withDetail("MADlib Model - "+props.getModelDescription(), v);
//		});
	}
}
