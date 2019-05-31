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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

import io.pivotal.rtsmadlib.client.db.repo.ContainerDbRepository;
import io.pivotal.rtsmadlib.client.db.repo.ExportImplortRepository;
import io.pivotal.rtsmadlib.client.meta.AppProperties;

/**
 * @author Sridhar Paladugu
 *
 */
@SpringBootApplication(scanBasePackages = "io.pivotal")
@Profile("!test")
public class MADlibRESTServiceApplication implements CommandLineRunner {

	static final Log log = LogFactory.getLog(MADlibRESTServiceApplication.class.getName());

	public static void main(String[] args) {
		SpringApplication.run(MADlibRESTServiceApplication.class, args);
	}

	@Autowired
	ExportImplortRepository exportImplortRepository;

	@Autowired
	ContainerDbRepository containerDbRespository;
	
	@Autowired
	AppProperties props;

	@Override
	public void run(String... args) throws Exception {
		log.debug("Boot strapping the local postgres madlib model tables greenplum .......");
		exportImplortRepository.performExportImportModel();
		containerDbRespository.fetchActorTableMetada();
		log.debug("Finished local postgres madlib model tables .......");
		log.debug("Container is ready for requests........");
	}
}
