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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import io.pivotal.rtsmadlib.client.db.repo.ContainerDbRepository;



/**
 * @author Sridhar Paladugu
 *
 */
@Component
public class MADlibRESTServiceInspector implements HealthIndicator {
  
	@Autowired
	ContainerDbRepository containerDbRepository;
	
	String errorMsg;
    @Override
    public Health health() {
        int errorCode = check(); // perform some specific health check
        if (errorCode != 0) {
            return Health.down()
              .withDetail("Error: ", errorCode +" : "+errorMsg).build();
        }
        return Health.up().build();
    }
     
    public int check() {
    	int returnCode=1;
    	   try {
    		   Map<String, Map<String, Object>> m = containerDbRepository.fetchModel();
    		   if( null != m && m.size()>0)
    			   returnCode= 0;
    	   }catch(Exception e) {
    		   errorMsg = e.getMessage();
    		   returnCode = -2;
    	   }
      return returnCode;
    }
}
