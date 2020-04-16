/**
 * 
 */
package io.pivotal.rtsmadlib.plpymodel.db.repo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * @author sridharpaladugu
 *
 */
@Getter
@Setter
public class DriverFunctionReturnType  {
	
	String schemaName;
	String typeName;
	List<TableColumn> columns;
	//this maintain the java to sql mapping to validate the incoming payload
	Map<String, String> java2SqlMap; 
	public DriverFunctionReturnType(String schema, String name) {
		this.schemaName = schema;
		this.typeName = name;
	}
	public void addColumn(TableColumn tc) {
		if(this.columns == null) {
			this.columns = new LinkedList<TableColumn>();
		}
		this.columns.add(tc);
	}
	/**
	 * Get the column in the order that was defined in source database
	 * This is must for Type definitions where return types for functions
	 * depends on.
	 * @return {@link TableColumn}
	 */
	public TableColumn getColumn(int index) {
		TableColumn tc = null;
		for(TableColumn tc1 : columns) {
			if(tc1.getColumnNumber() == index) {
				tc = tc1;
				break;
			}
		}
		return tc;
	}
}
