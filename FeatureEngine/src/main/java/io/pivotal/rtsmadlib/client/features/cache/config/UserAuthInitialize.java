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

package io.pivotal.rtsmadlib.client.features.cache.config;

import java.util.Properties;

import org.apache.geode.LogWriter;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.security.AuthInitialize;
import org.apache.geode.security.AuthenticationFailedException;

@SuppressWarnings("unused")
public class UserAuthInitialize implements AuthInitialize {

    private static final String USERNAME = "security-username";
    private static final String PASSWORD = "security-password";

    private LogWriter securitylog;
    private LogWriter systemlog;

    public static AuthInitialize create() {
        return new UserAuthInitialize();
    }

    @Override
    public void init(LogWriter systemLogger, LogWriter securityLogger) throws AuthenticationFailedException {
        this.systemlog = systemLogger;
        this.securitylog = securityLogger;
    }

    @Override
    public Properties getCredentials(Properties props, DistributedMember server, boolean isPeer) throws AuthenticationFailedException {
    	    System.out.println("++++++++++++++++++++++++    getCredentials   start     ++++++++++++++++++++++++");
        String username = props.getProperty(USERNAME);
        if (username == null) {
            throw new AuthenticationFailedException("UserAuthInitialize: username not set.");
        }

        String password = props.getProperty(PASSWORD);
        if (password == null) {
            throw new AuthenticationFailedException("UserAuthInitialize: password not set.");
        }

        Properties properties = new Properties();
        properties.setProperty(USERNAME, username);
        properties.setProperty(PASSWORD, password);
      	System.out.println("++++++++++++++++++++++++    getCredentials  end       ++++++++++++++++++++++++");
        return properties;
    }

    @Override
    public void close() {
    }
}
