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
Description: A deployment tool for MADlib ML models on Docker Engine.

"""
import subprocess
import shlex
import os
import logging

logging.basicConfig(format='%(message)s', level=logging.INFO)

class Docker:

    def deploy(
        self,
        moduleName,
        appName,
        inputJsoni,
        ):
        inputJson = inputJsoni.replace("\'", "'\\''")
        rtsMADlibHome = os.environ.get('RTSMADLIB_HOME')
        folder = rtsMADlibHome + '/docker/' + moduleName
        containerName = appName + '_' + moduleName + '_1'
        cmd1 = 'docker-compose -p ' + appName + ' -f ' + folder \
            + '/docker-compose.yml up -d --build --quiet-pull'
        logging.info(cmd1)
        subprocess.check_call(shlex.split(cmd1))

        logging.info('Running post container script in '
                     + containerName + ' ..........')
        cmd2 = 'docker exec -d ' + containerName \
            + " /bin/sh /docker-entrypoint-initdb.d/init_container_docker.sh \'" \
            + inputJson + "\'"
        subprocess.check_call(shlex.split(cmd2))
        logging.info('Finished the provisioning the ' + moduleName
                     + '  container! ')
        uris = self.getServiceUrls(moduleName, appName)
        logging.info('service end point => ')
        for url in uris:
            logging.info(url)
        return uris

    def undeploy(self, moduleName, appName):
        name = appName + '_' + moduleName + '_1'
        logging.info('un-deploying container ' + name)
        containerIDCmd = 'docker ps | grep ' + name \
            + " |awk '{print $1}'"
        containerID = (subprocess.Popen(containerIDCmd,
                stdout=subprocess.PIPE, shell=True).communicate()[0]).decode()
        if containerID and not containerID.isspace():
            stopCmd = 'docker stop ' + containerID
            rmCmd = 'docker rm ' + containerID
            subprocess.check_call(shlex.split(stopCmd))
            subprocess.check_call(shlex.split(rmCmd))
        else:
            logging.info('No deployment found. Exiting')

    def getServiceUrls(self, moduleName, appName):
        name = appName + '_' + moduleName + '_1'
        ipCmd = 'docker ps | grep ' + name \
            + " | awk '{print $1}' | xargs docker inspect --format '{{ .NetworkSettings.IPAddress }}'"
        ip = subprocess.Popen(ipCmd, stdout=subprocess.PIPE,
                              shell=True).communicate()[0]
        portCmd = 'docker ps | grep ' + name \
            + " | awk '{print $1}' | xargs docker port| grep 80"      
        portStr = (subprocess.Popen(portCmd, stdout=subprocess.PIPE,
                                   shell=True).communicate()[0]).decode()
        idx = portStr.index(':')
        length=len(portStr)
        port = portStr[idx+1:length]
        urlBase = 'http://127.0.0.1' + ':' + port.strip('\n')
        uri = []
        uri.append(urlBase + '/actuator/info')
        uri.append(urlBase + '/actuator/health')
        if moduleName == 'rts-for-madlib-model':
            uri.append(urlBase + '/predict')
        elif moduleName == 'rts-for-madlib-featuresengine':
            uri.append(urlBase + '/features/calculate')
        elif moduleName == 'rts-for-madlib-featurescachemanager':
            uri.append(urlBase + '/features/load')
        elif moduleName == 'rts-for-madlib-mlmodelflow':
            uri.append(urlBase + '/predict')
        elif moduleName == 'rts-for-madlib-microbatch':
            uri.append(urlBase + '/batch/run')    
        else:
            uri.append('')    
        return uri

    def getServiceUrl(self, moduleName, appName):
        name = appName + '_' + moduleName + '_1'
        ipCmd = 'docker ps | grep ' + name \
            + " | awk '{print $1}' | xargs docker inspect --format '{{ .NetworkSettings.IPAddress }}'"
        ip = (subprocess.Popen(ipCmd, stdout=subprocess.PIPE,
                              shell=True).communicate()[0]).decode()
        portCmd = 'docker ps | grep ' + name \
            + " | awk '{print $1}' | xargs docker port| grep 80"
        portStr = (subprocess.Popen(portCmd, stdout=subprocess.PIPE,
                                   shell=True).communicate()[0]).decode()
        idx = portStr.index('/')
        port = portStr[:idx]
        urlPrefix = 'http://' + ip.strip('\n') + ':'+port
        if moduleName == 'rts-for-madlib-model':
            uri = urlPrefix + '/predict'
            logging.info(" model uri => "+ uri)
        elif moduleName == 'rts-for-madlib-featuresengine':
            uri = urlPrefix + '/features/calculate'
            logging.info(" fe uri => "+ uri)
        elif moduleName == 'rts-for-madlib-featurescachemanager':
            uri = urlPrefix + '/features/load'
            logging.info(" fc uri => "+ uri)
        elif moduleName == 'rts-for-madlib-mlmodelflow':
            uri = urlPrefix + '/predict'
            logging.info(" mlf uri => "+ uri)
        elif moduleName == 'rts-for-madlib-microbatch':
            uri = urlPrefix + '/batch/run'
            logging.info(" mbatch uri => "+ uri)    
        else:
            uri=""
        return uri
