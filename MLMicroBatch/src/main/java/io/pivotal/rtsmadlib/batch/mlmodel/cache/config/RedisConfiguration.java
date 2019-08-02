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
package io.pivotal.rtsmadlib.batch.mlmodel.cache.config;

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

import io.pivotal.rtsmadlib.batch.mlmodel.meta.RedisProperties;
import io.pivotal.rtsmadlib.batch.mlmodel.model.ModelScore;
import io.pivotal.rtsmadlib.batch.mlmodel.model.ModelScoreKey;

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

//	@Bean
//	public RedisTemplate<Object, Object> redisTemplate() {
//		RedisTemplate<Object, Object> template = new RedisTemplate<Object, Object>();
//		template.setConnectionFactory(connectionFactory());
//		return template;
//	}
	@Bean
	public RedisTemplate<ModelScoreKey, ModelScore> redisTemplate() {
		RedisTemplate<ModelScoreKey, ModelScore> template = new RedisTemplate<ModelScoreKey, ModelScore>();
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
