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
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import io.pivotal.rtsmadlib.client.features.meta.ApplicationProperties;
import io.pivotal.rtsmadlib.client.features.service.MADlibFeaturesLoaderService;
/**
 * @author sridhar paladugu
 *
 */
@SpringBootApplication
@Profile("!test")
public class MaDlibFeatureLoaderServiceApplication implements CommandLineRunner{
	static final Log log = LogFactory.getLog(MaDlibFeatureLoaderServiceApplication.class.getName());
	@Autowired
	ApplicationProperties appProps;
	
	@Autowired
	MADlibFeaturesLoaderService featuresLoaderService;
	
	@Autowired
	Environment env;
	
	public static void main(String[] args) {
		SpringApplication.run(MaDlibFeatureLoaderServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Running post start hooks.........");
		featuresLoaderService.loadFeatures();
		log.info("Finished bootstrapping cache .......");
		log.info("Container is ready for requests!");
	}
}
