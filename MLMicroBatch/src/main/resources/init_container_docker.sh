#-----------------------------------------------------------------------------------------------
#   Copyright 2019 Pivotal Software
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#----------------------------------------------------------------------------------------------

# Author: Sridhar Paladugu 
# Email: spaladugu@pivotal.io

#!/bin/sh

printf "Running MLMicrobatch Spring boot application .................."
echo "$1" > /opt/pivotal/rts-for-madlib/MLMicroBatch-input.log

java -jar /opt/pivotal/rts-for-madlib/MLMicroBatch-1.0.0-SNAPSHOT.jar \
			--spring.application.json="$1" > /opt/pivotal/rts-for-madlib/MLMicroBatch.log 2>&1

# java -jar -Dgemfire.deploy-working-dir=/tmp \
# 			-Dspring.profiles.active=gemfire \
# 			/opt/pivotal/madlibflow/MADlibFeaturesCacheLoader-1.0.0-SNAPSHOT.jar \
# 			--spring.application.json="$1" > /opt/pivotal/madlibflow/MADlibFeaturesCacheLoader.log 2>&1

# java -jar -Dspring.profiles.active=redis \
# 			/opt/pivotal/madlibflow/MADlibFeaturesCacheLoader-1.0.0-SNAPSHOT.jar \
# 			--spring.application.json="$1" > /opt/pivotal/madlibflow/MADlibFeaturesCacheLoader.log 2>&1
