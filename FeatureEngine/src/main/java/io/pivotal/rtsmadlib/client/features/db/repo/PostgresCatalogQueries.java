/**
 * 
 */
package io.pivotal.rtsmadlib.client.features.db.repo;

/**
 * @author sridharpaladugu
 *
 */
public interface PostgresCatalogQueries {
	//(employment_outcomes_1)
	//pseo
	public static String functionMetaExtractQuery = "SELECT "
			+ " n.nspname as \"Schema\", p.proname as \"Name\",l.lanname as \"Language\","
			+ " pg_catalog.pg_get_function_result(p.oid) as \"Result data type\","  
			+ " pg_catalog.pg_get_function_arguments(p.oid) as \"Argument data types\","  
			+ " p.prosrc as \"Source code\" "
			+ " FROM pg_catalog.pg_proc p\n" 
			+ " LEFT JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace\n"
			+ " LEFT JOIN pg_catalog.pg_language l ON l.oid = p.prolang\n" 
			+ " WHERE  n.nspname ~ '<SCHEMA_NAME>'"
			+ " and p.proname ~ '^<FUNC_NAME>$'";
	
	public static String functionFullMetaExtractQuery = "SELECT n.nspname as \"Schema\"," + 
			"  p.proname as \"Name\"," + 
			"  pg_catalog.pg_get_function_result(p.oid) as \"Result data type\"," + 
			"  pg_catalog.pg_get_function_arguments(p.oid) as \"Argument data types\"," + 
			"  CASE" + 
			"  WHEN p.proisagg THEN 'agg'" + 
			"  WHEN p.proiswin THEN 'window'" + 
			"  WHEN p.prorettype = 'pg_catalog.trigger'::pg_catalog.regtype THEN 'trigger'" + 
			"  ELSE 'normal'" + 
			" END as \"Type\", " + 
			" CASE " + 
			"  WHEN p.prodataaccess = 'n' THEN 'no sql'" + 
			"  WHEN p.prodataaccess = 'c' THEN 'contains sql'" + 
			"  WHEN p.prodataaccess = 'r' THEN 'reads sql data'" + 
			"  WHEN p.prodataaccess = 'm' THEN 'modifies sql data'" + 
			" END as \"Data access\"," + 
			" CASE" + 
			"  WHEN p.provolatile = 'i' THEN 'immutable'" + 
			"  WHEN p.provolatile = 's' THEN 'stable'" + 
			"  WHEN p.provolatile = 'v' THEN 'volatile'" + 
			" END as \"Volatility\"," + 
			"  pg_catalog.pg_get_userbyid(p.proowner) as \"Owner\"," + 
			"  l.lanname as \"Language\"," + 
			"  p.prosrc as \"Source code\"," + 
			"  pg_catalog.obj_description(p.oid, 'pg_proc') as \"Description\"" + 
			" FROM pg_catalog.pg_proc p" + 
			"     LEFT JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace" + 
			"     LEFT JOIN pg_catalog.pg_language l ON l.oid = p.prolang" + 
			" WHERE NOT p.proisagg" + 
			"      AND p.prorettype <> 'pg_catalog.trigger'::pg_catalog.regtype" + 
			"  AND p.proname ~ '^(<FUNC_NAME>)$'" + 
			"  AND n.nspname ~ '<SCHEMA_NAME>'" +
			"  AND pg_catalog.pg_function_is_visible(p.oid)" + 
			" ORDER BY 1, 2, 4";

}
