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

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;


/**
 * @author Sridhar Paladugu
 *
 */
@Component
public class MADlibFeaturesServiceHealthInspector implements HealthIndicator {
  
	
	
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
    	   try {
    		   return 0;
    	   }catch(Exception e) {
    		   errorMsg = e.getMessage();
    		   return -2;
    	   }
    }
}
