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

package io.pivotal.rtsmadlib.client.features.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.rtsmadlib.client.features.meta.ApplicationProperties;
import io.pivotal.rtsmadlib.client.features.service.MADlibFeaturesLoaderService;

/**
 * @author Sridhar Paladugu
 *
 */
@RestController
public class MaDlibFeatureLoaderController {

	static final Log log = LogFactory.getLog(MaDlibFeatureLoaderController.class.getName());
	@Autowired
	MADlibFeaturesLoaderService featuresLoaderService;

	@Autowired
	ApplicationProperties props;

	@RequestMapping("/features/load")
	public ResponseEntity<HttpStatus> getFeatures() {
		try {
			if (null != props.getFeatureFunctions() && props.getFeatureFunctions().size() > 0) {
				log.debug("Running features generation function(s).........");
				featuresLoaderService.generateFeatures();
				log.debug("Finished executing features generation function(s).");
			}
		} catch (Exception e) {
			log.error("Error while running features function on Greenplum", e);
			return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		try {
			log.info("Running features load.........");
			featuresLoaderService.loadFeatures();
			log.info("Finished loading featues cache.");
		} catch (Exception e) {
			log.error("Exception while storing data to Cache!", e);
			return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<HttpStatus>(HttpStatus.OK);
	}
}
