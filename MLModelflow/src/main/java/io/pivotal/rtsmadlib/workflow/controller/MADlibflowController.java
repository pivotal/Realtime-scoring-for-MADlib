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

package io.pivotal.rtsmadlib.workflow.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import io.pivotal.rtsmadlib.workflow.meta.ApplicationProperties;

/**
 * 
 * @author Sridhar Paladugu
 * Pivotal Software Inc.
 *
 */
@RestController
public class MADlibflowController {
	static final Log log = LogFactory.getLog(MADlibflowController.class.getName());

	@Autowired
	ApplicationProperties appProps;

	private RestTemplate restTemplate;

	@Autowired
    public MADlibflowController(RestTemplateBuilder restTemplateBuilder) {
         restTemplate = restTemplateBuilder
          .errorHandler(new MADlibflowControlErrorHandler())
          .build();
    }
	
	@RequestMapping("/predict")
	public List<Map<String, Object>> predict(@RequestBody Map<String, Object> payload) {
		List<Map<String, Object>> predResults = null;
		ResponseEntity<List> ref = null;
		try {
			log.debug("invoking feature service => "+ appProps.getFeatureEngineEndpoint());
			ref = restTemplate.postForEntity(appProps.getFeatureEngineEndpoint(), payload, List.class);
		} catch (Throwable t) {
			throw new RuntimeException("Exception while invoking the feature engine.", t);
		}
		if (ref.getStatusCode().equals(HttpStatus.OK)) {
			List<Map<String, Object>> featuresList = ref.getBody();
			Map<String, Object> featuresMap = featuresList.get(0);
			try {
				log.debug("invoking model service => "+ appProps.getModelEndpoint());
				ResponseEntity<List> rem = restTemplate.postForEntity(appProps.getModelEndpoint(), 
						featuresMap, List.class);
				predResults = rem.getBody();
			} catch (Throwable t) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, t.getMessage(), t);
			}
			
		} else {
			throw new RuntimeException("Exception while invoking the feature engine. Status :" + ref.getStatusCode());
		}
		return predResults;
	}

}
