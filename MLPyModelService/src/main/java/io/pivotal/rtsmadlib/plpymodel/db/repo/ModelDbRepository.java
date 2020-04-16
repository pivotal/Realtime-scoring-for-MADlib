package io.pivotal.rtsmadlib.plpymodel.db.repo;

import static io.pivotal.rtsmadlib.plpymodel.db.repo.PostgresCatalogQueries.functionMetaExtractQuery;
import static io.pivotal.rtsmadlib.plpymodel.db.repo.PostgresCatalogQueries.tableMetaExtractQuery;
import static io.pivotal.rtsmadlib.plpymodel.db.repo.PostgresCatalogQueries.userDefinedTypeExtractQuery;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.pivotal.rtsmadlib.plpymodel.meta.AppProperties;

/**
 * @author Sridhar Paladugu
 *
 */
@Repository
public class ModelDbRepository {

	static final Log log = LogFactory.getLog(ModelDbRepository.class.getName());

	@Autowired
	AppProperties appProps;

	@Autowired
	@Qualifier("modeldbJdbcTemplate")
	JdbcTemplate modeldbJdbcTemplate;
	

	@Autowired
	@Qualifier("modeldbNPJdbcTemplate")
	NamedParameterJdbcTemplate modeldbNPJdbcTemplate;
	/*
	 * This is not 100% java to sql data type maps; used for only payload JSON
	 * validation
	 */
	static final Map<String, String> typeMaps = new HashMap<String, String>();
	static {

		typeMaps.put("int4", "java.lang.Integer");
		typeMaps.put("int8", "java.lang.Long");
		typeMaps.put("text", "java.lang.String");
		typeMaps.put("float8", "java.lang.Double");
		typeMaps.put("double precision", "java.lang.Double");
		typeMaps.put("bool", "java.lang.Boolean");
		typeMaps.put("character", "java.lang.String");
		typeMaps.put("varchar", "java.lang.String");
		typeMaps.put("longvarchar", "java.lang.String");
		typeMaps.put("numeric", "java.math.BigDecimal");
		typeMaps.put("decimal", "java.math.BigDecimal");
		typeMaps.put("bit", "java.lang.Boolean");
		typeMaps.put("tinyint", "java.lang.Integer");
		typeMaps.put("smallint", "java.lang.Integer");
		typeMaps.put("integer", "java.lang.Integer");
		typeMaps.put("bigint", "java.lang.Long");
		typeMaps.put("real", "java.lang.Float");
		typeMaps.put("float", "java.lang.Double");
		typeMaps.put("double precesion", "java.lang.Double");
		typeMaps.put("binary", "java.lang.Byte[]");
		typeMaps.put("varbinary", "java.lang.Byte[]");
		typeMaps.put("longvarbinary", "java.lang.Byte[]");
		typeMaps.put("date", "java.lang.String");
		typeMaps.put("timestamp", "java.lang.String");
		typeMaps.put("time", "java.lang.Long");
	}

	
	public byte[] fetchModel() {
		Map<String, Object> results = new HashMap<String, Object>();
		byte model[] = null;
		String modelName = appProps.getModelName();
		int modelVersion = appProps.getModelVersion();

		String modelQry = "SELECT model FROM " 
				+ appProps.getModelRepoSchema() 
				+ "."
				+ appProps.getModelRepoTable()
				+ " WHERE model_name = :modelName "
				+ "	and model_version = :modelVersion";
		
		Map<String, Object> qryParams = new HashMap<String, Object>();
		qryParams.put("modelName", modelName);
		qryParams.put("modelVersion", modelVersion);
		modeldbNPJdbcTemplate.query(modelQry, qryParams, (ResultSet rs) -> {
			byte[] modelBytes = rs.getBytes(1);
			results.put(modelName, modelBytes);
		});
		if (results.containsKey(modelName))
			model = (byte[]) results.get(modelName);
		return model;
	}
	/**
	 * Extract the function name, language type, input params and output params, and
	 * source
	 * 
	 * @return List of FunctionDefinition class.
	 */
	public List<FunctionDefinition> fetchFunctionDefinitions() {
		List<FunctionDefinition> functions = new ArrayList<FunctionDefinition>();
		log.debug("querying source database funrtion definitions....");
		List<String> functionsToImport = new LinkedList<String>();
		functionsToImport.add(appProps.getModelDriverFunction());
		functionsToImport.addAll(appProps.getAdditionalFunctions());
		for (String functionName : functionsToImport) {
			String sqlQry = (functionMetaExtractQuery.replace("<FUNC_NAME>", functionName)).replace("<SCHEMA_NAME>",
					appProps.getModelSchema());
			modeldbJdbcTemplate.query(sqlQry, (ResultSet rs) -> {
				FunctionDefinition fd = new FunctionDefinition();
				fd.setSchema(rs.getString("Schema"));
				fd.setName(rs.getString("Name"));
				fd.setInputArgs(rs.getString("Argument data types"));
				fd.setOutputArgs(rs.getString("Result data type"));
				// hadcode to plpython3u because plcontainer is not in general postgres yet
//				fd.setLanguage(rs.getString("Language"));
				if(rs.getString("Language").equalsIgnoreCase("plcontainer")) {
					fd.setLanguage("plpython3u");
				}
				fd.setSrc(rs.getString("Source code"));
				functions.add(fd);
			});
		}
		return functions;
	}
	
	/**
	 * Fetch model input table metadata from source database 
	 * @return {@link TableDefinition}
	 */
	public TableDefinition fetchInputTableDefintion() {		
		String payloadTable = appProps.getPayloadTable();
		TableDefinition td = fetchTableDefintion(payloadTable);
		return td;		
	}
	
	/**
	 * Fetch model input table metadata from source database 
	 * @return {@link TableDefinition}
	 */
	public TableDefinition fetchResultsTableDefintion() {		
		String resultsTable = appProps.getResultsTable();
		TableDefinition td = fetchTableDefintion(resultsTable);
		return td;		
	}
	/**
	 * Fetch table metadata from source database 
	 * @return {@link TableDefinition}
	 */
	public TableDefinition fetchTableDefintion(String tableName) {		
		Map<String, String> columnsMap = new HashMap<String, String>();
		String schemaName = appProps.getModelSchema();
		TableDefinition td = new TableDefinition(schemaName, tableName);
		String sqlQeury = tableMetaExtractQuery
				.replace("<PAYLOAD_TABLE_NAME>", tableName)
				.replace("<SCHEMA_NAME>", schemaName);
		modeldbJdbcTemplate.query(sqlQeury, (ResultSet rs)-> {
			TableColumn tc = new TableColumn();
			tc.setColumnName(rs.getString("attname"));
			tc.setColumnDataType(rs.getString("format_type"));
			columnsMap.put(tc.columnName, typeMaps.get(tc.columnDataType));
			Boolean nullable = rs.getBoolean("attnotnull");
			if(!nullable) {
				tc.setNullCondition(" not null ");
			}else if(nullable) {
				tc.setNullCondition(" null ");
			}
			td.addColumn(tc);
		});	
		td.setJava2SqlMap(columnsMap);
		return td;		
	}
	
	/**
	 * extract the user defined structured data type from database
	 * 
	 * @return {@link DriverFunctionReturnType}
	 */
	public DriverFunctionReturnType fetchUserTypeDefinition() {
		if(appProps.getModelDriverFunctionReturnType() == null) return null;
		Map<String, String> columnsMap = new HashMap<String, String>();
		String typeName = appProps.getModelDriverFunctionReturnType();
		String schemaName = appProps.getModelSchema();
		DriverFunctionReturnType dfrt = new DriverFunctionReturnType(schemaName, typeName);
		String sqlQeury = userDefinedTypeExtractQuery
					.replace("<USER_TYPE_NAME>", typeName)
					.replace("<SCHEMA_NAME>", schemaName);
		modeldbJdbcTemplate.query(sqlQeury, (ResultSet rs)-> {
			TableColumn tc = new TableColumn();
			tc.setColumnName(rs.getString("attname"));
			tc.setColumnDataType(rs.getString("format_type"));
			columnsMap.put(tc.columnName, typeMaps.get(tc.columnDataType));
			Boolean nullable = rs.getBoolean("attnotnull");
			if(!nullable) {
				tc.setNullCondition(" not null ");
			}else if(nullable) {
				tc.setNullCondition(" null ");
			}
			int columnNumber = rs.getInt("attnum");
			tc.setColumnNumber(columnNumber);	
			dfrt.addColumn(tc);
		});	
		dfrt.setJava2SqlMap(columnsMap);
		return dfrt;
	}
	
	/**
	 * Extract function definitions from source database catalog and
	 * reassemble the code
	 * @return map of function source
	 */
	public Map<String, String> getFunctionDefinitions() {
		Map<String, String> functions = new HashMap<String, String>();
		List<FunctionDefinition> defs = fetchFunctionDefinitions();
		defs.forEach(fd -> {
			functions.put(fd.getSchema()+"."+fd.getName(), constructFunctionDefinition(fd));
		});
		return functions;
	}
	public String constructFunctionDefinition(FunctionDefinition fdMeta) {
		if (fdMeta == null) {
			throw new RuntimeException("Function metadata is null.");
		}
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE OR REPLACE FUNCTION ");
		sb.append(fdMeta.getSchema()).append(".").append(fdMeta.getName());
		sb.append(" ( ").append(fdMeta.getInputArgs()).append(" ) ");
		if (null != fdMeta.getOutputArgs()) {
			sb.append(" RETURNS ");
			sb.append(fdMeta.getOutputArgs());
		}
		sb.append(" AS $$ ");
		sb.append(fdMeta.getSrc());
		sb.append(" $$ LANGUAGE ");
		sb.append((fdMeta.getLanguage().replace("{", "")).replace("}", ""));
		return sb.toString();
	}
}
