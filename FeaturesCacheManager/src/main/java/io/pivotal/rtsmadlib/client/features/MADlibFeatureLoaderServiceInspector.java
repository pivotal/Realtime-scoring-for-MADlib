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
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import io.pivotal.rtsmadlib.client.features.cache.CacheLoader;



/**
 * @author Sridhar Paladugu
 *
 */
@Component
public class MADlibFeatureLoaderServiceInspector implements HealthIndicator {
	static final Log log = LogFactory.getLog(MADlibFeatureLoaderServiceInspector.class.getName());

	@Autowired
	CacheLoader cacheLoader;
	
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
    		   Long keys = -1l;
    			try {
    			 keys = cacheLoader.keyCount();
    			}catch (Exception e) {
    				log.error(e);
    			}
    		   if( keys != -1)
    			   return 0;
    	   }catch(Exception e) {
    		   errorMsg = e.getMessage();
    		   return -2;
    	   }
        return 1;
    }
}
