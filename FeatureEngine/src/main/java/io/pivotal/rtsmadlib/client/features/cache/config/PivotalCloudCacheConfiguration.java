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

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.pivotal.rtsmadlib.client.features.meta.ApplicationProperties;
import io.pivotal.rtsmadlib.client.features.meta.PivotalCloudCacheProperties;

/**
 * @author sridharpaladugu
 *
 */
@Component
@Configuration
@Profile("pcc")
public class PivotalCloudCacheConfiguration {

	private static final String SECURITY_CLIENT = "security-client-auth-init";
	private static final String SECURITY_USERNAME = "security-username";
	private static final String SECURITY_PASSWORD = "security-password";

	@Autowired
	PivotalCloudCacheProperties pccProperties;

	@Bean
	ClientCache getClientCache(ApplicationProperties applicationProperties) {
		try {
			String userName = pccProperties.getUsername();
			String password = pccProperties.getPassword();
			Properties gemfireProperties = new Properties();
			gemfireProperties.setProperty(SECURITY_CLIENT, UserAuthInitialize.class.getName() + ".create");
			gemfireProperties.setProperty(SECURITY_USERNAME, userName);
			gemfireProperties.setProperty(SECURITY_PASSWORD, password);
			ClientCacheFactory ccf = new ClientCacheFactory(gemfireProperties);
			ccf.setPdxSerializer(new ReflectionBasedAutoSerializer("io.pivotal.madlib.client.features.model.*"));
			ccf.setPdxReadSerialized(true);
			ccf.addPoolLocator(pccProperties.getLocators(), pccProperties.getPort());
			ClientCache clientCache = ccf.create();
			return clientCache;
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException(t);
		}
	}
}
