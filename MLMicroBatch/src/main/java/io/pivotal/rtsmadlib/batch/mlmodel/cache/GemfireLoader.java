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

package io.pivotal.rtsmadlib.batch.mlmodel.cache;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.pivotal.rtsmadlib.batch.mlmodel.cache.repo.ModelScoresGemfireRepository;
import io.pivotal.rtsmadlib.batch.mlmodel.model.ModelScore;


/**
 * @author Sridhar Paladugu
 *
 */
@Component
@Profile("gemfire")
public class GemfireLoader implements CacheLoader {
	@Autowired
	ModelScoresGemfireRepository modelScoresGemfireRepository;	
	@Override
	public void saveModelScore(ModelScore score){
		modelScoresGemfireRepository.save(score);
	}
	@Override
	public void saveModelScores(List<ModelScore> scores) {
		modelScoresGemfireRepository.saveAll(scores);		
	}
	
	@Override
	public Long keyCount() {
		return modelScoresGemfireRepository.count();		
	}
}
