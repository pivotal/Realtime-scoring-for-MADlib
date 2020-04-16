/**
 * 
 */
package io.pivotal.rtsmadlib.plpymodel.db.repo;

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
public class TableColumn {
		Integer columnNumber;
		String columnName;
		String columnDataType;
		String nullCondition;
}
