#!/usr/bin/env python
"""
Copyright 2019 Pivotal Software

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Author: Sridhar Paladugu 
Email: spaladugu@pivotal.io
Description: A deployment tool for MADlib ML models on Kubernetes.

"""
import subprocess
import shlex
import time
import os
import logging

logging.basicConfig(format='%(message)s', level=logging.DEBUG)

class Kubernetes:

    '''
........Deploys the specified module on kubernetes
........moduleName: name of the module to deploy
........appName: name of the application
........inputJson: Input manifest for the module
....'''

    def deploy(
        self,
        moduleName,
        appName,
        inputJson,
        ):
        podName = appName + '-' + moduleName
        logging.info('Deploying ' + podName
                     + ' container to kubernetes ..........')
        self.cleanupConfig(podName)
        self.createPodConfigMap(podName, inputJson)
        self.createPodSpec(appName, moduleName, podName)
        self.provisionPOD(podName)
        self.pollForPodStart(podName)
        self.getPODInformation(podName)
        logging.info('Provisioning is finished.')

    def undeploy(self, moduleName, appName):
        podName = appName + '-' + moduleName
        logging.info('undeploying ' + podName + ' .........')
        mkcommand1 = 'kubectl delete deployment ' + podName
        mkcommand2 = 'kubectl delete service ' + podName
        mkcommand3 = 'kubectl delete configmaps ' + podName + '-config'
        try:
            subprocess.check_call(shlex.split(mkcommand1))
        except (subprocess.CalledProcessError, e1):
            logging.info(e1.output)
        try:
            subprocess.check_call(shlex.split(mkcommand2))
        except (subprocess.CalledProcessError, e2):
            logging.info(e2.output)
        try:
            subprocess.check_call(shlex.split(mkcommand3))
        except (subprocess.CalledProcessError, e3):
            logging.info(e3.output)

    def cleanupConfig(self, podName):
        deleteConfigCmd = 'kubectl delete configmaps ' + podName \
            + '-config'
        try:
            logging.info('deleting any old service configurations.....')
            subprocess.check_call(shlex.split(deleteConfigCmd))
        except (subprocess.CalledProcessError):
            logging.info('No existing configuration found. Continuing....'
                         )

    def createPodConfigMap(self, podName, inputJson):
        configInput = inputJson.replace('"', '"').replace("\'", "'\\''")
        createConfigCmd = 'kubectl create configmap ' + podName \
            + "-config --from-literal=podname='" + podName \
            + "' --from-literal=springjson=\'" + configInput + "\'"
        logging.info('creating new service configuration.....')
        subprocess.check_call(shlex.split(createConfigCmd))

    def getKubeFolderName(self):
        madlibflowHome = os.environ.get('RTSMADLIB_HOME')
        folder = madlibflowHome + '/kubernetes'
        return folder

    def createPodSpec(
        self,
        appName,
        moduleName,
        podName,
        ):
        templateFileName = moduleName + '-app.yaml'
        folder = self.getKubeFolderName()
        with open(folder + '/' + templateFileName, 'r') as templateFile:
            templateFileData = templateFile.read()
        newTemplateFileData1 = templateFileData.replace('$APP_NAME', appName)
        madlibflowDockerReg = os.environ.get('RTSMADLIB_DOCKER_REG')
        newTemplateFileData2 = newTemplateFileData1.replace('$RTSMADLIB_DOCKER_REG', madlibflowDockerReg)
        madlibflowDockerTag = os.environ.get('RTSMADLIB_IMG_TAG')
        newTemplateFileData3 = newTemplateFileData2.replace('$RTSMADLIB_IMG_TAG', madlibflowDockerTag)
        if moduleName == "madlibrest" :
            newTemplateFileData = newTemplateFileData3.replace('$REPLICAS', "2")
        elif moduleName == "madlibfeaturesengine" :
            newTemplateFileData = newTemplateFileData3.replace('$REPLICAS', "2")
        else :
            newTemplateFileData = newTemplateFileData3.replace('$REPLICAS', "1")

          # Write the file out again

        with open(folder + '/pods/' + podName + '-app.yaml', 'w') as \
            file:
            file.write(newTemplateFileData)

    def provisionPOD(self, podName):
        folder = self.getKubeFolderName() + '/pods/'
        createDeploymentCmd = 'kubectl create -f ' + folder + podName \
            + '-app.yaml'

        logging.info('creating the deployment .....')
        subprocess.check_call(shlex.split(createDeploymentCmd))

    def pollForPodStart(self, podName):
        logging.info("Polling for pod <"+podName+">'s running status ........")
        statusCmd = 'kubectl get pods | grep ' + podName \
            + " | awk '{print $3}'"

        status = subprocess.Popen(statusCmd, stdout=subprocess.PIPE,
                                  shell=True).communicate()[0].decode()
        logging.info('Status => ' + status)
        while status.strip() != 'Running':
            statuses = (subprocess.Popen(statusCmd,
                    stdout=subprocess.PIPE, shell=True).communicate()[0]).decode()
            #logging.info(statuses)
            if 'Running' in statuses:
               status = 'Running'
            else:
               status = statuses

        logging.info('Status => ' + status)

    def getPODInformation(self, podName):
        podCmd = 'kubectl get pods | grep ' + podName \
            + " | awk \'{print $1}\'"
        podname = (subprocess.Popen(podCmd, stdout=subprocess.PIPE,
                                   shell=True).communicate()[0]).decode()
        logging.info('POD NAME => ' + podname)
