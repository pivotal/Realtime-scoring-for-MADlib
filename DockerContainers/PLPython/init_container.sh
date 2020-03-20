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

# This script require the client install program supply a text file mlpypackes.txt
# with each line with a pip package name.

exec > /opt/pivotal/plpy-model/init_container.out 2>&1

set -x

printf "\n Required Python packages for model as per mlpypackes.txt:\n"

pinput="pymodules_list.txt"
while IFS= read -r line
do
  printf "\n $line"
done < "$pinput"

printf "\nInstalling required packages .......\n"
input="pymodules_list.txt"
while IFS= read -r line
do
  printf "\nInstalling $line ............"
  sudo pip3 install $line
done < "$input"

printf "\nFinished with init_pyenv.sh script!.\n"