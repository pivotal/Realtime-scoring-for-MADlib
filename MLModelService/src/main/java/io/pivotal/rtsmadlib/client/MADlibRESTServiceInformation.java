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
