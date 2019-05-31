#!/bin/sh
#-----------------------------------------------------------------------------------------------
# MIT License
# Copyright (c) 2019 Pivotal
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#----------------------------------------------------------------------------------------------

# Author: Sridhar Paladugu 
# Email: spaladugu@pivotal.io

NONE='\033[00m'
BCyan='\033[1;36m'

echo "------------------------------------------------------------------------------"
echo "${BCyan}MADlibWorkflow${NONE}" 
echo "This script perform below steps based on input parameters. "                          
echo "1) Run maven build MLModelflow.jar"
echo "2) Build MLModelflow docker image by using Dockerfile"
echo "3) Publish MLModelflow image to docker repository if registry specified"
echo ""
echo "The below command will perform al 3 steps ; #"
echo " ./build_mlmodelflow.sh localhost:5000 "
echo "------------------------------------------------------------------------------"

docker_repo=$1

echo "${BCyan}Building the MLModelflow jar.......${NONE}"
cd ../MLModelflow && mvn clean install && \
 cp ./target/MLModelFlow-1.0.0-SNAPSHOT.jar ../dist && \
 cd ../build

# build container and push to docker registry; if registry specified
echo "${BCyan}Building Docker image for mlModelflow ........${NONE}"
mkdir -p ../dist
cd ../MLModelflow && \
	docker build --build-arg docker_registry=$docker_repo --rm --tag rts4madlib-modelflow:1.0 . && \
	cp ./target/MLModelflow-1.0.0-SNAPSHOT.jar ../dist && \
	cd ../build
if [ -n "$docker_repo" ]
then
  	docker tag rts4madlib-modelflow:1.0 $docker_repo/rts4madlib-modelflow:1.0
	echo "${BCyan}Pushing image to docker registry $docker_repo ......${NONE}"
  	docker push $docker_repo/rts4madlib-modelflow:1.0
	echo "${BCyan}Cleaning up build time images ..... ${NONE}"
  	docker rmi -f rts4madlib-modelflow:1.0 $docker_repo/rts4madlib-modelflow:1.0
fi

echo "${BCyan}Finished MLModelflow Build Task!${NONE}" 
