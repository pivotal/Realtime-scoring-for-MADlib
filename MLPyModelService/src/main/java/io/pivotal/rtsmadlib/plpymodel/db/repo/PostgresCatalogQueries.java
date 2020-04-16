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
package io.pivotal.rtsmadlib.plpymodel.db.repo;

/**
 * @author sridharpaladugu
 *
 */
public interface PostgresCatalogQueries {

	public static String functionMetaExtractQuery = 
		"SELECT "
		+ " n.nspname as \"Schema\", p.proname as \"Name\",l.lanname as \"Language\","
		+ " pg_catalog.pg_get_function_result(p.oid) as \"Result data type\","  
		+ " pg_catalog.pg_get_function_arguments(p.oid) as \"Argument data types\","  
		+ " p.prosrc as \"Source code\" "
		+ " FROM pg_catalog.pg_proc p\n" 
		+ " LEFT JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace\n"
		+ " LEFT JOIN pg_catalog.pg_language l ON l.oid = p.prolang\n" 
		+ " WHERE  n.nspname ~ '(<SCHEMA_NAME>)$'"
		+ " and p.proname ~ '^(<FUNC_NAME>)$'";
	
	public static String functionFullMetaExtractQuery = 
		"SELECT n.nspname as \"Schema\"," + 
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
	
	public static String tableMetaExtractQuery = 
		" SELECT c.oid, n.nspname, c.relname, " + 
		" a.attname,pg_catalog.format_type(a.atttypid, a.atttypmod), a.attnotnull " + 
		" FROM pg_catalog.pg_class c" + 
		"	LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace" + 
		"	join pg_catalog.pg_attribute a on a.attrelid = c.oid AND a.attnum > 0 AND NOT a.attisdropped" + 
		" WHERE c.relname OPERATOR(pg_catalog.~) '^(<PAYLOAD_TABLE_NAME>)$'" + 
		"  AND n.nspname OPERATOR(pg_catalog.~) '^(<SCHEMA_NAME>)$'";

	public static String userDefinedTypeExtractQuery = 
		" SELECT a.attname, " 
		+ "  pg_catalog.format_type(a.atttypid, a.atttypmod), "  
		+ "  (SELECT substring(pg_catalog.pg_get_expr(d.adbin, d.adrelid) for 128) " 
		+ "   FROM pg_catalog.pg_attrdef d "
		+ "   WHERE d.adrelid = a.attrelid AND d.adnum = a.attnum AND a.atthasdef), " 
		+ "  a.attnotnull, "
		+ "  a.attnum, " 
		+ "  (SELECT c.collname FROM pg_catalog.pg_collation c, pg_catalog.pg_type t "  
		+ "   WHERE c.oid = a.attcollation "
		+ "		AND t.oid = a.atttypid AND a.attcollation <> t.typcollation) "
		+ "  AS attcollation, "  
		+ "  NULL AS indexdef, "  
		+ "  NULL AS attfdwoptions "  
		+ " FROM pg_catalog.pg_attribute a "  
		+ "  LEFT OUTER JOIN pg_catalog.pg_attribute_encoding e " 
		+ "    ON e.attrelid = a .attrelid AND e.attnum = a.attnum " 
		+ " WHERE a.attrelid = (SELECT c.oid FROM pg_catalog.pg_class c " 
		+ "     		LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " 
		+ "				WHERE c.relname OPERATOR(pg_catalog.~) '^(<USER_TYPE_NAME>)$' " 
		+ "  				AND n.nspname OPERATOR(pg_catalog.~) '^(<SCHEMA_NAME>)$') "  
		+ "	AND a.attnum > 0 AND NOT a.attisdropped " 
		+ " ORDER BY a.attnum; " ;
}
