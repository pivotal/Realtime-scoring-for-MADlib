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

package io.pivotal.rtsmadlib.client.db;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * @author Sridhar Paladugu
 *
 */
@Configuration
public class DataSourceConfig {

	@Bean
	@Qualifier("modeldb-datasource")
	@Primary
	@ConfigurationProperties(prefix="modeldb-datasource")
	DataSource modeldbDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean
	@Qualifier("containerdb-datasource")
	@ConfigurationProperties(prefix="containerdb-datasource")
	DataSource containerdbDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean
	@Qualifier("modeldbJdbcTemplate")
	JdbcTemplate modeldbJdbcTemplate(@Qualifier("modeldb-datasource")DataSource modeldbDataSource) {
		return new JdbcTemplate(modeldbDataSource);
	}
	
	@Bean
	@Qualifier("containerdbJdbcTemplate")
	JdbcTemplate containerdbJdbcTemplate(@Qualifier("containerdb-datasource")DataSource containerdbDataSource) {
		return new JdbcTemplate(containerdbDataSource);
	}
}
