#!/usr/bin/env python

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
