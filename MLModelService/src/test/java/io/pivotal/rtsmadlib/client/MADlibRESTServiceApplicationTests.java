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

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.DockerComposeContainer;
/**
 * @author Sridhar Paladugu
 *
 */



@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=MADlibRESTServiceApplication.class)
@ActiveProfiles("test")
public class MADlibRESTServiceApplicationTests{
	

	@ClassRule
    public static DockerComposeContainer madlibContainer = 
      new DockerComposeContainer(
        new File("src/test/resources/docker-compose.yml"))
      ;

	@Test
	public void contextLoads() {
	}
	
	
	@Before
	public void before() {
		System.out.println("Here");

    }

	@After
	public void teardown() {
		System.out.println("Here");
	}
}
