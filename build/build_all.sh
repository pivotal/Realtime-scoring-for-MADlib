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

# Description: Build all the components for RTS4MADlib uploads to docker registry

NONE='\033[00m'
BCyan='\033[1;36m'
uline='\033[4m'
boldgreen='\033[1;32m'

# ---------------------------------------------------------------------------------"
# "
# ${boldgreen}${uline}Running build for Real time Scoring for MADlib components${reg}"
# 1. MLModelService"
# 2. FeatureCacheManager"
# 3. FeatureEngine"
# 4. MLModelflow"
# 5. RTS4MADLIB"
# The tool will push the docker containers to a docker registry, if specified."
# For example the below command push all the containers to local docker registry"
#  ${boldgreen}./build_all.sh -R pivotaldata -T 1.0 -P yes" 
# ---------------------------------------------------------------------------------"

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
echo "${BCyan}Building the MLModelFlow with below parameters .......${NONE}"
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





#docker system prune -f

./build_mlmodelservice.sh -R $docker_repo -T $image_tag -P $push_image
./build_featurescachemanager.sh -R $docker_repo -T $image_tag -P $push_image
./build_featuresengine.sh -R $docker_repo -T $image_tag -P $push_image
./build_mlmodelflow.sh -R $docker_repo -T $image_tag -P $push_image


echo "${boldgreen}Finished building Real time Scoring for MADlib container components${reg}"

echo "${boldgreen}Building the RTS4MADLIB tool.....${reg}"

mkdir -p ../dist

cd ../. && tar -zcvf RTS4MADLIB.tar.gz RTS4MADLIB && chmod 755 RTS4MADLIB.tar.gz && mv RTS4MADLIB.tar.gz dist/

echo "${boldgreen}The build process is finished.${reg}"
