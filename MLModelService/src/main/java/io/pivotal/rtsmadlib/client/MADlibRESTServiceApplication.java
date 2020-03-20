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
		log.debug("Finished importing madlib model artifacts to container!");
		log.debug("Container is ready for requests!");
	}
}
