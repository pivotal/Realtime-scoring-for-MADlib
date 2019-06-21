/**
 * 
 */
package io.pivotal.rtsmadlib.client.features.model;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import lombok.Getter;
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
public class FunctionDefinition implements Serializable {
	private static final long serialVersionUID = 3069517157811448983L;
	private String schema;
	private String name;
	private String inputArgs;
	private String outputArgs;
	private String language;
	private String src;
	
}
