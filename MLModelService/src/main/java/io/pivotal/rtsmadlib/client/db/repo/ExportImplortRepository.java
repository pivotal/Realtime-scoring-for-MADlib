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

package io.pivotal.rtsmadlib.client.db.repo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import io.pivotal.rtsmadlib.client.meta.AppProperties;
import io.pivotal.rtsmadlib.client.meta.ContainerDatabaseProperties;
import io.pivotal.rtsmadlib.client.meta.ModelDatabaseProperties;


/**
 * @author Sridhar Paladugu
 *
 */
@Repository
public class ExportImplortRepository {

	@Autowired
	@Qualifier("containerdbJdbcTemplate")
	JdbcTemplate containerdbJdbcTemplate;
	
	@Autowired
	ModelDatabaseProperties gpProps;
	
	@Autowired
	ContainerDatabaseProperties pgProps;
	
	@Autowired
	AppProperties appProps;
	
	 @Autowired
	   Environment env;
	 
	static final Log log = LogFactory.getLog(ExportImplortRepository.class.getName());
	
	public void performExportImportModel() {
		if(Arrays.asList(env.getActiveProfiles()).contains("test"))
			return;
		runPgDumpForSchemaOnly() ;
		runPgDumpForDataOnly();
		containerdbJdbcTemplate.execute("drop schema if exists "+ appProps.getModelSchema()+ " cascade");
		containerdbJdbcTemplate.execute("create schema IF NOT EXISTS "+ appProps.getModelSchema());
		runRestore();
		try {
			Files.deleteIfExists(Paths.get(appProps.getWorkdir()+"/"+appProps.getModelSchema()+"_schema.dump"));
			Files.deleteIfExists(Paths.get(appProps.getWorkdir()+"/ModelTables_Data.dump"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * perform schema only dump to pwd. 
	 * refer to ../resources/application.yml
	 * @return java.lang.String representing stdout
	 * @throws a RuntimeException if and errors
	 */
	private String runPgDumpForSchemaOnly()  {
		List<String> cmds = getPgDumpCmdPrefix();
		cmds.add("-O");
		cmds.add("-x");
		cmds.add("--no-security-labels");
		cmds.add("-s");
		for(String tableName: appProps.getModeltables()) {
			cmds.add("-t" );
			cmds.add( appProps.getModelSchema()+"."+tableName);
		}	
		cmds.add("-t" );
		cmds.add( appProps.getModelSchema()+"."+appProps.getModelInputTable());
		cmds.add("-f");
		cmds.add(appProps.getWorkdir()+"/"+appProps.getModelSchema()+"_schema.dump");

		ProcessBuilder pb = null;
		Process process = null;
		Map<String, String> env = null;
		try {
			pb = new ProcessBuilder(cmds);
			env = pb.environment();
			env.put("PGPASSWORD", gpProps.getPassword());
			process = pb.start();
			process.waitFor(); //blocking call
			if (process.exitValue() != 0) {
				throw new RuntimeException(getErrorMessage(process));
			}else {
				String stdout = extractOutput(process);
				log.debug("PG_DUMP schema export finished!");  
				return stdout;
			} 
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}finally {
			if( null != env && !env.isEmpty()) {
				env.remove("PGPASSWORD");
			}
			if(null != process) {
				process.destroy();
			}
		}
		
	}
	
	/**
	 * perform data only dump to pwd. 
	 * refer to ../resources/application.yml
	 * @return java.lang.String representing stdout
	 * @throws a RuntimeException if and errors
	 */
	private String runPgDumpForDataOnly()  {
		List<String> cmds = getPgDumpCmdPrefix();
		cmds.add("-a");
		for(String tableName: appProps.getModeltables()) {
			cmds.add("-t" );
			cmds.add( appProps.getModelSchema() + "." + tableName);
		}
		cmds.add("-f");
		//cmds.add(appProps.getWorkdir()+"/"+appProps.getModelTableName()+"_Data.dump");
		cmds.add(appProps.getWorkdir()+"/ModelTables_Data.dump");
		ProcessBuilder pb = null;
		Process process = null;
		Map<String, String> env = null;
		try {
			pb = new ProcessBuilder(cmds);
			env = pb.environment();
			env.put("PGPASSWORD", gpProps.getPassword());
			process = pb.start();
			process.waitFor(); //blocking call
			if (process.exitValue() != 0) {
				throw new RuntimeException(getErrorMessage(process));
			}else {
				String stdout = extractOutput(process);
				log.debug("PG_DUMP data export finished!");  
				return stdout;
			} 
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}finally {
			if( null != env && !env.isEmpty()) {
				env.remove("PGPASSWORD");
			}
			if(null != process) {
				process.destroy();
			}
		}
	}

	/**
	 * Prepare common command for pg_dump
	 * @return
	 */
	private List<String> getPgDumpCmdPrefix() {
		List<String> cmds = new ArrayList<String>();
		cmds.add(appProps.getPostgresHome() + "/pg_dump");
		cmds.add("-h");
		Map<String, String> m = parseUrl(gpProps.getJdbcUrl());
		cmds.add(m.get("host"));
		cmds.add("-p");
		cmds.add(m.get("port"));
		cmds.add("-d");
		cmds.add(m.get("dbname"));
		cmds.add("-U");
		cmds.add(gpProps.getUsername());
		cmds.add("-n" );
		cmds.add(appProps.getModelSchema());
		return cmds;
	}
	
	
	private Map<String, String> parseUrl(String url) {
		Map<String, String> m = new HashMap<String, String>();
		String host = url.substring(url.indexOf("//")+2, url.lastIndexOf(":"));
		String port = url.substring(url.lastIndexOf(":")+1, url.lastIndexOf("/"));
		String dbname = null;
		if(url.lastIndexOf("?") != -1) {
			dbname = url.substring(url.lastIndexOf("/")+1, url.lastIndexOf("?"));
		}else {
			dbname = url.substring(url.lastIndexOf("/")+1);
		}
		m.put("host", host);m.put("port", port);m.put("dbname", dbname);
		return m;

	}
	/**
	 * Perform restore using the schema dump and data dump files.
	 * @return java.lang.String representing stdout
	 * @throws a RuntimeException if and errors
	 */
	private void runRestore()  {
		log.debug("installing schema ........"); 
		runRestore(appProps.getWorkdir()+"/"+appProps.getModelSchema()+"_schema.dump");
		log.debug("copying model table ........"); 
		runRestore(appProps.getWorkdir()+"/ModelTables_Data.dump");
	}
	
	/**
	 * Perform restore using the sql source file.
	 * @return java.lang.String representing stdout
	 * @throws a RuntimeException if and errors
	 */
	private String runRestore(String srcFile)  {
		Map<String, String> m = parseUrl(pgProps.getJdbcUrl());
		List<String> cmds = getRestoreCmdPrefix(m);
		cmds.add(srcFile);
		ProcessBuilder pb = null;
		Process process =null;
		Map<String, String> env = null;
		try {
			pb = new ProcessBuilder(cmds);
			env = pb.environment();
			env.put("PGPASSWORD", pgProps.getPassword());
			process = pb.start();
			process.waitFor(); //blocking call
			if (process.exitValue() != 0) {
				throw new RuntimeException(getErrorMessage(process));
			}else {
				String stdout = extractOutput(process);
				log.debug("done!");  
				return stdout;
			} 
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}finally {
			if( null != env && !env.isEmpty()) {
				env.remove("PGPASSWORD");
			}
			if(null != process) {
				process.destroy();
			}
		}
	}
	
	/**
	 * Prepare base command for running psql
	 * @param cmds
	 */
	private List<String> getRestoreCmdPrefix(Map<String, String> m) {
		List<String> cmds = new ArrayList<String>();
		cmds.add(appProps.getPostgresHome() + "/psql");
		cmds.add("-h");
		cmds.add(m.get("host"));
		cmds.add("-p");
		cmds.add(m.get("port"));
		cmds.add("-U");
		cmds.add(pgProps.getUsername());
		cmds.add("-d");
		cmds.add(m.get("dbname"));
		cmds.add("-f");
		return cmds;
	}
	
	/**
	 * parse and return error message from stdout.
	 * @param process
	 * @return java.lang.String
	 * @throws IOException
	 */
	private String extractOutput(Process process) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));  
		StringBuffer sb = new StringBuffer();  
		String line = "";
		while ((line = br.readLine()) != null) {  
				sb.append(line);  
		}
		return sb.toString();
	}

	/**
	 * parse and return error message from stdout.
	 * @param process
	 * @return java.lang.String
	 * @throws IOException
	 */
	private String getErrorMessage(Process process) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));  
		StringBuffer sb = new StringBuffer();  
		String errline = "";
		while ((errline = br.readLine()) != null) {  
				sb.append(errline);  
		}
		return sb.toString();
	}
	
}
