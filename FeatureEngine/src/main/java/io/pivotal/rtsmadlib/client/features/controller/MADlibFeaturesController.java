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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.rtsmadlib.client.features.service.MADlibFeaturesService;

/**
 * @author sridhar paladugu
 *
 */
@RestController
public class MADlibFeaturesController {


	@Autowired 
	MADlibFeaturesService madlibFeaturesService;
	
	@RequestMapping("/features/calculate")
	public List<Map<String, Object>> getFeatures(@RequestBody Map<String, Object> payload) {
		
		return madlibFeaturesService.calculateFeatures(payload);
		
	}

	
}
