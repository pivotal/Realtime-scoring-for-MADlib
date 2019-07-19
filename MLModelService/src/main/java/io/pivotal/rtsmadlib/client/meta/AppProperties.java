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

package  io.pivotal.rtsmadlib.client.meta;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Sridhar Paladugu
 *
 */
@Component
@ConfigurationProperties(prefix = "madlibrest")
public class AppProperties {

	String modelName;
	
	String modelDescription;
	
	List<String> modeltables;

	String modelSchema;
	
	String modelInputTable;
	
	String modelQuery;
	
	String postgresHome;
	
	String workdir;
	
	String resultsTable;
	
	String resultsQuery;

	
	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getModelDescription() {
		return modelDescription;
	}

	public void setModelDescription(String modelDescription) {
		this.modelDescription = modelDescription;
	}
	
	public List<String> getModeltables() {
		return modeltables;
	}

	public void setModeltables(List<String> modeltables) {
		this.modeltables = modeltables;
	}

	public String getModelSchema() {
		return modelSchema;
	}

	public void setModelSchema(String modelSchema) {
		this.modelSchema = modelSchema;
	}

	public String getModelInputTable() {
		return modelInputTable;
	}

	public void setModelInputTable(String actorTableName) {
		this.modelInputTable = actorTableName;
	}

	public String getPostgresHome() {
		return postgresHome;
	}

	public void setPostgresHome(String postgresHome) {
		this.postgresHome = postgresHome;
	}

	public String getWorkdir() {
		return workdir;
	}

	public void setWorkdir(String workdir) {
		this.workdir = workdir;
	}

	public String getModelQuery() {
		return modelQuery;
	}

	public void setModelQuery(String actionQuery) {
		this.modelQuery = actionQuery;
	}

	public String getResultsTable() {
		return resultsTable;
	}

	public void setResultsTable(String resultsTable) {
		this.resultsTable = resultsTable;
	}

	public String getResultsQuery() {
		return resultsQuery;
	}

	public void setResultsQuery(String resultsQuery) {
		this.resultsQuery = resultsQuery;
	}

}

