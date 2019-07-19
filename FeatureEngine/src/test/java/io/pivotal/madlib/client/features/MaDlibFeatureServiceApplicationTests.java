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

package io.pivotal.madlib.client.features;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.pivotal.rtsmadlib.client.features.MaDlibFeatureServiceApplication;
/**
 * 
 * @author sridhar paladugu
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MaDlibFeatureServiceApplication.class)
@ActiveProfiles(profiles = "test")
public class MaDlibFeatureServiceApplicationTests {
	@Test
	public void contextLoads() {
	}

}
