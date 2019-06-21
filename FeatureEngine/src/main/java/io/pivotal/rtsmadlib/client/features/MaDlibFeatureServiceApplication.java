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

package io.pivotal.rtsmadlib.client.features;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * @author sridhar paladugu
 */
import io.pivotal.rtsmadlib.client.features.meta.ApplicationProperties;
import io.pivotal.rtsmadlib.client.features.service.FunctionImporterService;
import io.pivotal.rtsmadlib.client.features.service.MADlibFeaturesService;

@SpringBootApplication(scanBasePackages = "io.pivotal")
public class MaDlibFeatureServiceApplication implements CommandLineRunner {
	static final Log log = LogFactory.getLog(MaDlibFeatureServiceApplication.class.getName());
	@Autowired
	ApplicationProperties appProps;
	@Autowired
	FunctionImporterService functionImporterService;

	public static void main(String[] args) {
		SpringApplication.run(MaDlibFeatureServiceApplication.class, args);
	}

	@Autowired
	MADlibFeaturesService madlibFeaturesService;

	@Override
	public void run(String... args) throws Exception {
//		String json = "{\"transaction_id\": \"cct0009187\", \"rlb_location_key\": \"1\", \"account_number\": \"123456789\", \"card_type\": \"VISA\", \"merchant_city\": \"Alpharetta\", \"merchant_id\": 1, \"merchant_name\": \"Walmart\", \"merchant_state\": \"GA\", \"sic_code\": \"sc12345\", \"transaction_amount\": 23.75, \"posting_date\": \"11-06-2018 10:30:22\", \"transaction_date\": \"11-06-2018 10:30:22\"}";
//		ObjectMapper om = new ObjectMapper();
//		Map<String, Object> payload = new HashMap<String, Object>();
//		payload = om.readValue(json, new TypeReference<Map<String, Object>>() {});
//		madlibFeaturesService.calculateFeatures(payload);
		functionImporterService.importFunctions();
	}

}
