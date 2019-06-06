/**********************************************************************************************
 MIT License

 Copyright (c) 2019 Pivotal

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
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
