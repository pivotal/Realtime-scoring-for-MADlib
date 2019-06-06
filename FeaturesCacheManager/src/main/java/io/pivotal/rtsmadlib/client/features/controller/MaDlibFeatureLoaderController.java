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
