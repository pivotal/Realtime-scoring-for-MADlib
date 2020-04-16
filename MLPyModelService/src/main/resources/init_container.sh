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

exec > /opt/pivotal/plpy-model/init_container.out 2>&1

set -x

echo " Required Python packages for model->"
pydeps=$(echo $2 | tr "," "\n")
for dep in $pydeps
do
  echo "$dep"
done

echo "Installing python packages ......."
input="/opt/pivotal/plpy-model/pymodules_list.txt"
for dep in $pydeps
do
  echo "Installing $dep ............"
  sudo -H pip3 install $dep
done 

echo "Installed all required python packages ......."

echo "$1" > /opt/pivotal/plpy-model/MLPyModelService-input.log

echo "Running spring boot application.................."	

java -jar /opt/pivotal/plpy-model/MLPyModelService-1.0.0-SNAPSHOT.jar \
	--spring.application.json="$1" > /opt/pivotal/plpy-model/MLPyModelService.log 2>&1 &
	
echo "Finished with init_container.sh script!."

