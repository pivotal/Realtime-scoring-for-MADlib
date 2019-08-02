#!/usr/bin/env python

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

# Description: Build Base docker images for RTS4MADlib

import os
import sys
import argparse
import shlex
import subprocess

"""
    Name: buildbaaseimages
    Description: Build JDK11 and Postgres with plpython docker images
    usage: 
"""
def buildJava11(registry):
	print "Building UBUNTU image  with JDK11........."
	buildImgCommad='docker build -t  rts4madlib-jdk:1.0 '+os.getcwd()+'/java'
	subprocess.call(shlex.split(buildImgCommad))
	if registry is None:
		print "No Docker registry is specified. Skipping the push operation!"
	else:
		print "pushing the image to docker registry "+ registry
		tagCommand='docker tag rts4madlib-jdk:1.0 '+ registry+'/rts4madlib-jdk:1.0'
		subprocess.call(shlex.split(tagCommand))
		pushCommand='docker push '+ registry +'/rts4madlib-jdk:1.0'
		subprocess.call(shlex.split(pushCommand))
		cleanCommand='docker rmi -f rts4madlib-jdk:1.0 '+ registry+'/rts4madlib-jdk:1.0'
		subprocess.call(shlex.split(cleanCommand))

def buildMadlibbase(registry):
	print "Building UBUNTU image with Postgres with plPython and JDK11........."
	buildImgCommad='docker build -t rts4madlib-pgjava:1.0 '+os.getcwd()+'/MADlib'
	subprocess.call(shlex.split(buildImgCommad))
	if registry is None:
		print "No Docker registry is specified. Skipping the push operation!"
	else:
		print "pushing the image to docker registry"+ registry
		tagCommand='docker tag rts4madlib-pgjava:1.0 '+ registry+'/rts4madlib-pgjava:1.0'
		subprocess.call(shlex.split(tagCommand))
		pushCommand='docker push '+ registry +'/rts4madlib-pgjava:1.0'
		subprocess.call(shlex.split(pushCommand))
		cleanCommand='docker rmi -f rts4madlib-pgjava:1.0 '+ registry+'/rts4madlib-pgjava:1.0'
		subprocess.call(shlex.split(cleanCommand))

def main():
	parser = argparse.ArgumentParser()
	parser.add_argument("--registry", nargs="?", help="docker registry for pushing images")
	arguments = parser.parse_args()
	buildJava11(arguments.registry)
	buildMadlibbase(arguments.registry)
	print "Finished building base docker images!"

if __name__ == "__main__":
	main()
