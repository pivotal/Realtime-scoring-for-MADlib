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
