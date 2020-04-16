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
package io.pivotal.rtsmadlib.plpymodel.service;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.rtsmadlib.plpymodel.db.repo.ContainerDbRepository;
import io.pivotal.rtsmadlib.plpymodel.db.repo.DriverFunctionReturnType;
import io.pivotal.rtsmadlib.plpymodel.db.repo.ModelDbRepository;
import io.pivotal.rtsmadlib.plpymodel.db.repo.TableDefinition;
import io.pivotal.rtsmadlib.plpymodel.meta.AppProperties;

/**
 * @author sridharpaladugu
 *
 */
@Service
public class ModelArtifactsImporterService {
	static final Log log = LogFactory.getLog(ModelArtifactsImporterService.class.getName());

	@Autowired
	ModelDbRepository modelDbRepository;

	@Autowired
	ContainerDbRepository containerDbRepo;

	@Autowired
	AppProperties appProps;

	Map<String, String> functionDefs;

	byte[] model;
	DriverFunctionReturnType functionReturnType;
	public void importArtifacts() {

		try {
			containerDbRepo.createPLPythonExtension();
		} catch (Exception e) {
			if (e.getCause()
					.equals("org.postgresql.util.PSQLException: ERROR: extension \"plpythonu\" already exists")) {
				// skip and proceed not an error case;
			}
		}
		log.debug("Extracting model artifacts from source database .....");
		model = modelDbRepository.fetchModel();
		functionDefs = modelDbRepository.getFunctionDefinitions();
		log.debug("Creating model artifacts in the ontainer database....");
		
		if(appProps.getModelDriverFunctionReturnType() != null && appProps.getModelDriverFunctionReturnType().trim().length() > 0 ) {
			log.debug("Import Model Driver function return type definition ....");
			functionReturnType =  modelDbRepository.fetchUserTypeDefinition();
		}
		containerDbRepo.importModelAndDriverFunction(model, functionDefs, functionReturnType);
		log.debug("Import payload input table definition .....");
		TableDefinition payloadTblDef = modelDbRepository.fetchInputTableDefintion();
		log.debug("creating payload inout table in container .....");
		containerDbRepo.createPayloadTable(payloadTblDef);
		
		log.debug("Import result table definition .....");
		TableDefinition resultTableDef = modelDbRepository.fetchResultsTableDefintion();
		log.debug("creating result table in container .....");
		containerDbRepo.createResultTable(resultTableDef);

		log.debug("Finished creating model articats in container!");

	}

	public Map<String, String> getFunctionDefs() {
		return this.functionDefs;
	}
}
