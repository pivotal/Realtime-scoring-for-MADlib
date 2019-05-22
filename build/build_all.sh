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

reg='\033[00m'
boldgreen='\033[1;32m'
uline='\033[4m'
echo "---------------------------------------------------------------------------------"
echo ""
echo "${boldgreen}${uline}Running build for Real time Scoring for MADlib components${reg}"
echo "1. MLModelService"
echo "2. FeatureCacheManager"
echo "3. FeatureEngine"
echo "4. MLModelflow"
echo "5. RTS4MADLIB"
echo ""
echo "The tool will push the docker containers to a docker registry, if specified."
echo "For example the below command push all the containers to local docker registry"
echo " ${boldgreen}./build_all.sh localhost:5000${reg}" 
echo "---------------------------------------------------------------------------------"


docker_repo=$1
set -e
#docker system prune -f

if [ -z "$docker_repo" ]
then	
    ./build_mlmodelservice.sh
    ./build_featurescachemanager.sh
    ./build_featuresengine.sh
    ./build_mlmodelflow.sh

else
    ./build_mlmodelservice.sh $docker_repo
    ./build_featurescachemanager.sh $docker_repo
    ./build_featuresengine.sh $docker_repo
    ./build_mlmodelflow.sh $docker_repo	
fi

echo "${boldgreen}Finished building Real time Scoring for MADlib container components${reg}"

echo "${boldgreen}Building the RTS4MADLIB tool.....${reg}"

mkdir -p ../dist

cd ../. && tar -zcvf RTS4MADLIB.tar.gz RTS4MADLIB && chmod 755 RTS4MADLIB.tar.gz && mv RTS4MADLIB.tar.gz dist/

echo "${boldgreen}The build process is finished.${reg}"
