/**********************************************************************************************
MIT License

 Copyright (c) 2019 Pivotal

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
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
