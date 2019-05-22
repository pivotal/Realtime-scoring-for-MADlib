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

