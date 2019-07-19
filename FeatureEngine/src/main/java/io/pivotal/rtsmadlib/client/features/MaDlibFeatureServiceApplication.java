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

package io.pivotal.rtsmadlib.client.features;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * @author sridhar paladugu
 */
import io.pivotal.rtsmadlib.client.features.meta.ApplicationProperties;
import io.pivotal.rtsmadlib.client.features.service.FunctionImporterService;
import io.pivotal.rtsmadlib.client.features.service.MADlibFeaturesService;

@SpringBootApplication(scanBasePackages = "io.pivotal")
public class MaDlibFeatureServiceApplication implements CommandLineRunner {
	static final Log log = LogFactory.getLog(MaDlibFeatureServiceApplication.class.getName());
	@Autowired
	ApplicationProperties appProps;
	@Autowired
	FunctionImporterService functionImporterService;

	public static void main(String[] args) {
		SpringApplication.run(MaDlibFeatureServiceApplication.class, args);
	}

	@Autowired
	MADlibFeaturesService madlibFeaturesService;

	@Override
	public void run(String... args) throws Exception {
		functionImporterService.importFunctions();
	}

}
