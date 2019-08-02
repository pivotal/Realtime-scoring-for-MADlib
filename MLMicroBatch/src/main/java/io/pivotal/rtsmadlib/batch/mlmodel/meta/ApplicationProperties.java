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
package io.pivotal.rtsmadlib.batch.mlmodel.meta;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author sridhar paladugu
 *
 */
@Component
@ConfigurationProperties(prefix = "ml-batch")
public class ApplicationProperties {
	private String name;
	private String schema;
	private String batchFunction;
	private boolean hydrateCache;
	private int loadBatchSize;
	private Map<String, String> cacheSourceTables;

	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	public String getSchema() {
		return schema;
	}


	public void setSchema(String schema) {
		this.schema = schema;
	}


	public String getBatchFunction() {
		return batchFunction;
	}


	public void setBatchFunction(String batchFunction) {
		this.batchFunction = batchFunction;
	}


	public boolean getHydrateCache() {
		return hydrateCache;
	}


	public void setHydrateCache(boolean hydrateCache) {
		this.hydrateCache = hydrateCache;
	}


	public int getLoadBatchSize() {
		return loadBatchSize;
	}


	public void setLoadBatchSize(int loadBatchSize) {
		this.loadBatchSize = loadBatchSize;
	}


	public Map<String, String> getCacheSourceTables() {
		return cacheSourceTables;
	}


	public void setCacheSourceTables(Map<String, String> cacheSourceTables) {
		this.cacheSourceTables = cacheSourceTables;
	}


	public void addCacheSourceTable(String table, String idColumn) {
		if (null == this.cacheSourceTables) {
			cacheSourceTables = new HashMap<String, String>();
		}
		cacheSourceTables.put(table, idColumn);
	}




	

}
