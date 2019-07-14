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
