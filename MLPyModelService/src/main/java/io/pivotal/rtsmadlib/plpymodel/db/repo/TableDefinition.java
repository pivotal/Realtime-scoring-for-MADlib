/**
 * 
 */
package io.pivotal.rtsmadlib.plpymodel.db.repo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author sridharpaladugu
 *
 */
@Component
@Getter
@Setter
@ToString
@NoArgsConstructor
public class TableDefinition {
	String schemaName;
	String tableName;
	List<TableColumn> columns;
	//this maintain the java to sql mapping to validate the incoming payload
	Map<String, String> java2SqlMap; 
	public TableDefinition(String schema, String name) {
		this.schemaName = schema;
		this.tableName = name;
	}
	public void addColumn(TableColumn tc) {
		if(this.columns == null) {
			this.columns = new LinkedList<TableColumn>();
		}
		this.columns.add(tc);
	}
}
