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

package io.pivotal.rtsmadlib.client.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.pivotal.rtsmadlib.client.db.repo.ContainerDbRepository;
import io.pivotal.rtsmadlib.client.db.repo.ExportImplortRepository;
import io.pivotal.rtsmadlib.client.db.repo.ModelDbRepository;


/**
 * @author Sridhar Paladugu
 *
 */
@RestController
public class MADlibClientRestController {
	
	static final Log log = LogFactory.getLog(MADlibClientRestController.class.getName());
	
	@Autowired
	ModelDbRepository modelDbRepository;

	@Autowired
	ContainerDbRepository containerDbRepository;
	
	@Autowired
	ExportImplortRepository exportImplortRepository;
	
	@GetMapping("/getDeployedModel")
	public Map<String, Map<String, Object>> getDeployedModel() {
		Map<String, Map<String, Object>> m = containerDbRepository.fetchModel();
		return m;
	}

	@RequestMapping("/predict")
	public List<Map<String, Object>> predict(@RequestBody  Map<String, Object> payload) {
		
		Map<String, Object> validationMsgs = validatePayload(payload);
		if(validationMsgs.get("isValid").equals("False")) {
			log.error("ERROR ==> payload validation failed!");
			ObjectMapper om = new ObjectMapper();
			String errors = null;
			try {
				errors= om.writerWithDefaultPrettyPrinter().writeValueAsString(validationMsgs);
				ResponseStatusException rex = new ResponseStatusException(HttpStatus.BAD_REQUEST, errors, new Exception(errors));
				throw rex;
			} catch (JsonProcessingException e) {
				throw new ResponseStatusException(
				          HttpStatus.FORBIDDEN, "Processing Error", e);
			}
			
		}
		return containerDbRepository.runPrediction(payload);
		
	}
	
	private Map<String, Object> validatePayload(Map<String, Object> payload) {
		Boolean isValid = true;
		if(log.isDebugEnabled()) {
			log.debug(printablePayload(payload));
		}
		Map<String, Object> validationMsgs = new HashMap<String, Object>();
		Map<String, String> dbColumnsMap = containerDbRepository.getActorTableMetada();
		Set<String> dbcolumns = dbColumnsMap.keySet();
		Set<String> payloadColumns = payload.keySet();
		int count=1;
		for(String column: payloadColumns){
			if(!dbcolumns.contains(column)) {
				isValid = false;
				validationMsgs.put("Error_"+count, "unexpected Column: "+ column);
				count++;
			}else if(dbcolumns.contains(column)){
				String columnClass=payload.get(column).getClass().getName();
				if(!columnClass.equals(dbColumnsMap.get(column))) {
					isValid = false;
					validationMsgs.put("Error_"+count, column+ " type must be "+ dbColumnsMap.get(column));
					count++;
				}
			}
		}
		if(!isValid) {
			validationMsgs.put("isValid", "False");
		}else {
			validationMsgs.put("isValid", "True");
		}
		return validationMsgs;
	}
	
	private String printablePayload(Map<String, Object> payload) {
		StringBuffer sb = new StringBuffer();
		payload.keySet().forEach(key-> {
			sb.append(key).append(" = ").append(payload.get(key));
		});
		return sb.toString();
	}
	
}



