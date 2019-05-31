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
