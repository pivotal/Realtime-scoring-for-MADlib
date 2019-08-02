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

package io.pivotal.rtsmadlib.batch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

import io.pivotal.rtsmadlib.batch.mlmodel.service.MLBatchService;

/**
 * 
 * @author Sridhar Paladugu
 * Pivotal Software Inc.
 *
 */
@SpringBootApplication
@Profile("!test")
public class MLMicroBatchApplication implements CommandLineRunner{
	@Autowired
	MLBatchService svc;
	public static void main(String[] args) {
		SpringApplication.run(MLMicroBatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		svc.runBatch();
	}

}
