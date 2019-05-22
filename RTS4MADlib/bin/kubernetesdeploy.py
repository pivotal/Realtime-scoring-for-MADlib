#!/usr/bin/env python
#-----------------------------------------------------------------------------------------------
# Copyright 2019 Sridhar Paladugu

# Permission is hereby granted, free of charge, to any person obtaining a copy of this 
# software and associated documentation files (the "Software"), to deal in the Software 
# without restriction, including without limitation the rights to use, copy, modify, 
# merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
# permit persons to whom the Software is furnished to do so, subject to the following 
# conditions:

# The above copyright notice and this permission notice shall be included in all copies 
# or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
# INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
# PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
# FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
# OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
# OTHER DEALINGS IN THE SOFTWARE.
#----------------------------------------------------------------------------------------------
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
        madlibflowHome = os.environ.get('MADLIBFLOW_HOME')
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
        madlibflowDockerReg = os.environ.get('MADLIBFLOW_DOCKER_REG')
        newTemplateFileData2 = newTemplateFileData1.replace('$MADLIBFLOW_DOCKER_REG', madlibflowDockerReg)
        if moduleName == "madlibrest" :
            newTemplateFileData = newTemplateFileData2.replace('$REPLICAS', "2")
        elif moduleName == "madlibfeaturesengine" :
            newTemplateFileData = newTemplateFileData2.replace('$REPLICAS', "2")
        else :
            newTemplateFileData = newTemplateFileData2.replace('$REPLICAS', "1")

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
