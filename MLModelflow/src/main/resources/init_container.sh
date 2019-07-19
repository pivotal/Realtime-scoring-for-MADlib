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

#!/bin/bash

echo "$1"
printf "Running MLModelflow Spring boot application .................."
echo "$1" > /opt/pivotal/rts-for-madlib/MLModelflow-input.log
nohup java -jar /opt/pivotal/rts-for-madlib/MLModelflow-1.0.0-SNAPSHOT.jar \
	 --spring.application.json="$1" > /opt/pivotal/rts-for-madlib/MLModelflow.log 2>&1 &
