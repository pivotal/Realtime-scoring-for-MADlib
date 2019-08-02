/**
 * 
 */
package io.pivotal.rtsmadlib.batch.mlmodel.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.rtsmadlib.batch.mlmodel.meta.ApplicationProperties;
import io.pivotal.rtsmadlib.batch.mlmodel.service.MLBatchService;
/**
 * @author sridhar paladugu
 *
 */
@RestController
public class MicrobatchController {
	
	static final Log log = LogFactory.getLog(MicrobatchController.class.getName());
	@Autowired
	MLBatchService mlBatchService;

	@Autowired
	ApplicationProperties props;

	@RequestMapping("/batch/run")
	public ResponseEntity<HttpStatus> runBatch() {
		try {
			if (null != props.getBatchFunction() ) {
				log.debug("Running the batch.........");
				mlBatchService.runBatchFunction();
				log.debug("Finished executing features generation function(s).");
			}
		} catch (Exception e) {
			log.error("Error while running batch function on Greenplum", e);
			return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		try {
			if(props.getHydrateCache()) {
				log.debug("hydrating cache ....");	
				mlBatchService.hydrateCache();
			}			
		} catch (Exception e) {
			log.error("Exception while storing data to Cache!", e);
			return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<HttpStatus>(HttpStatus.OK);
	}
}
