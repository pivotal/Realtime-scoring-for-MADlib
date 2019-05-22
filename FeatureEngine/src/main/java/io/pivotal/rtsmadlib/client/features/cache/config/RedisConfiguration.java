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

package io.pivotal.rtsmadlib.client.features.cache.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import io.pivotal.rtsmadlib.client.features.meta.RedisProperties;
import io.pivotal.rtsmadlib.client.features.model.MADlibFeature;
import io.pivotal.rtsmadlib.client.features.model.MADlibFeatureKey;

/**
 * @author sridharpaladugu
 *
 */
@Configuration
@Profile("redis")
public class RedisConfiguration {

	@Autowired
	RedisProperties redisProperties;

	@Bean
	RedisConnectionFactory connectionFactory() {
		if (redisProperties.getClustertype().equals("ha")) {
			RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
			sentinelConfig.master(redisProperties.getMaster());
			addServers(sentinelConfig, redisProperties.getServers());
			return new JedisConnectionFactory(sentinelConfig);

		}
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisProperties.getHostname(),
				redisProperties.getPort());
		config.setPassword(redisProperties.getPassword());
		return new JedisConnectionFactory(config);
	}

	@Bean
	public RedisTemplate<MADlibFeatureKey, MADlibFeature> redisTemplate() {
		RedisTemplate<MADlibFeatureKey, MADlibFeature> template = new RedisTemplate<MADlibFeatureKey, MADlibFeature>();
		template.setConnectionFactory(connectionFactory());
		return template;
	}

	/**
	 * parser input server:port list of values and add to redis config
	 * 
	 * @param sentinelConfig
	 * @param serversList
	 */
	private void addServers(RedisSentinelConfiguration sentinelConfig, List<String> serversList) {
		serversList.forEach(serverPort -> {
			int index = serverPort.indexOf(":");
			String server = serverPort.substring(0, index);
			int port = Integer.parseInt((serverPort.substring(index)));
			sentinelConfig.sentinel(server, port);
		});
	}
}
