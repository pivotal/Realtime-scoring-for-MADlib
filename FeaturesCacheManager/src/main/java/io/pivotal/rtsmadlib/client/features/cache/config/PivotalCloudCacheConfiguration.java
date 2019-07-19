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
			ccf.setPdxSerializer(new ReflectionBasedAutoSerializer("io.pivotal.poc.duke.workorders.cache.model.*"));
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
