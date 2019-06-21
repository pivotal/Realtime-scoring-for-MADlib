/**
 * 
 */
package io.pivotal.rtsmadlib.client.features.service;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.rtsmadlib.client.features.db.repo.ContainerDbRepository;
import io.pivotal.rtsmadlib.client.features.db.repo.ModelDbRepository;
import io.pivotal.rtsmadlib.client.features.meta.ApplicationProperties;
import io.pivotal.rtsmadlib.client.features.meta.ModelDatabaseProperties;



/**
 * @author sridharpaladugu
 *
 */
@Service
public class FunctionImporterService {
	static final Log log = LogFactory.getLog(FunctionImporterService.class.getName());
	@Autowired
	ModelDbRepository modelrepo;
	@Autowired
	ContainerDbRepository containerDbRepo;
	@Autowired
	ApplicationProperties appProps;
	@Autowired
	ModelDatabaseProperties gpProps;
	
	Map<String, String> functionDefs;
	public void importFunctions() {
		if (appProps.featureFunctions != null && appProps.featureFunctions.size() > 0) {
			try {
			containerDbRepo.createPLPythonExtension();
			}catch (Exception e) {
				if (e.getCause().equals("org.postgresql.util.PSQLException: ERROR: extension \"plpythonu\" already exists")) {
					//skip and proceed not an error case;
				}
			}
			log.debug("Found function definitions in manifest.Extracting functions from source database ...");
			functionDefs = modelrepo.getFunctionDefinitions();
			log.debug("Creating function definitions on container database....");
			containerDbRepo.crateSchema(appProps.getFeaturesSchema());
			containerDbRepo.crateFunctions(functionDefs);
			log.debug("Finished creating functions in container database!");
		}
	}
	
	public Map<String, String> getFunctionDefs() {
		return this.functionDefs;
	}
}
