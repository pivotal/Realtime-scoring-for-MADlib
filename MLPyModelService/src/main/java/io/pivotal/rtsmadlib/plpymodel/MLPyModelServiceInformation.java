/**
 * 
 */
package io.pivotal.rtsmadlib.plpymodel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.stereotype.Component;
import org.springframework.boot.actuate.info.InfoContributor;
import io.pivotal.rtsmadlib.plpymodel.meta.AppProperties;

/**
 * @author sridharpaladugu
 *
 */
@Component
public class MLPyModelServiceInformation implements InfoContributor {

	@Autowired
	AppProperties props;
	
	public void contribute(Builder builder) {
		builder.withDetail("Model name", props.getModelName());
		builder.withDetail("Model version", props.getModelVersion());
		builder.withDetail("Description", props.getModelDescription());
		builder.withDetail("Model Repo Schema", props.getModelSchema());
		builder.withDetail("Model driver function", props.getModelDriverFunction());				
		builder.withDetail("Model Input Table", props.getPayloadTable());
		builder.withDetail("Model Output Table", props.getPayloadTable());
		builder.withDetail("Model run operation", props.getModelQuery());
	}
}
