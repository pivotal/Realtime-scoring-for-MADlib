#!/bin/sh
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

# Description: Build Feature Engine Service Container and uploads to docker registry


NONE='\033[00m'
BCyan='\033[1;36m'
uline='\033[4m'
boldgreen='\033[1;32m'

# ------------------------------------------------------------------------------
# This script perform below stpes based on input parameters. "                          
# 1) Run maven build FeatureEngine.jar"
# 2) Build FeatureEngine docker image by using Dockerfile1"
# 3) Publish FeatureEngine image to docker repository if indicated"
# The below command will perform all steps"
# $ ./build_featureengine.sh -repo pivotaldata -tag 1.0 -push yes"
# ------------------------------------------------------------------------------"

set -e

usage()
{
	echo "$BCyan $uline usage: $NONE"
    echo "$boldgreen $0 -R docker_repo -T release_tag -P yes/no $NONE"
   	echo "\t-R -- Name of the docker repository to upload image and download the base images"
   	echo "\t-T -- version number for tag; for example 1.0"
   	echo "\t-P -- indicate whether upload image or not (yes/no)" 
   	exit 1 # Exit script after printing usage	
} 
	
while getopts "R:T:P:" opt
do
	case "$opt" in
		R ) docker_repo="$OPTARG" ;;
		T ) image_tag="$OPTARG" ;;
		P ) push_image="$OPTARG" ;;
		? ) usage ;;
	esac
done	
echo "${BCyan}Building the FeatureEngine with below parameters .......${NONE}"
echo " repo = $docker_repo"	
echo " tag = $image_tag"
echo " push = $push_image"	

if [ -z "$docker_repo" ] 
then
	echo "Please provide valid docker repo name";
	usage
fi

if [ -z "$image_tag" ] 
then
	echo "Please provide valid image tag number, for example 1.0";
	usage
fi	

if [ -z "$push_image" ] 
then
	echo "Please provide indicate whether upload image or not";
	usage
fi	

echo "${BCyan}Building the FeatureEngine jar.......${NONE}"

mkdir -p ../dist && cd ../FeatureEngine \
  && mvn clean install && \
  cp ./target/FeatureEngine-1.0.0-SNAPSHOT.jar ../dist && \
  cd ../build

# build container and push to docker registry if registry specified.
#empty string is passed if no registry is specified.
echo "${BCyan}Building Docker image FeatureEngine ........${NONE}"
source_docker_repo=pivotaldata
cd ../FeatureEngine && \
  docker build --build-arg docker_registry=$source_docker_repo  --build-arg base_image_tag=$image_tag --rm --tag rts4madlib-featuresengine:$image_tag . && \
  cd ../build

if [ "$push_image" == "yes" ]
then
	docker tag rts4madlib-featuresengine:$image_tag $docker_repo/rts4madlib-featuresengine:$image_tag
	echo "${BCyan}Pushing image to docker registry $docker_repo ......${NONE}"
	docker push $docker_repo/rts4madlib-featuresengine:$image_tag
	echo "${BCyan}Cleaning up build time images ..... ${NONE}"
	docker rmi -f rts4madlib-featuresengine:$image_tag $docker_repo/rts4madlib-featuresengine:$image_tag
fi

echo "${BCyan}Finished FeatureEngine Build Task!${NONE}" 
