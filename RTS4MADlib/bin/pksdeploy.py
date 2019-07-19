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
import subprocess
import shlex
import logging

"""
    Name: pksdeploy.py
    <developers>
        <developer>
            <id>spaladugu</id>
            <name>Sridhar Paladugu</name>
            <email>spaladugu@pivotal.io</email>
            <organization>Pivotal Software, Inc.</organization>
            <organizationUrl>https://pivotal.io/pivotal-greenplum</organizationUrl>
            <roles>
                <role>Machine Learning Data Engineer </role>
                <role>RTS-For-MADlib Lead/Committer</role>
            </roles>
        </developer>
    </developers>
    Description: A deployment tool for MADlib ML models on Pivotal Container Service(PKS).

"""
logging.basicConfig(format='%(message)s', level=logging.DEBUG)

class Pks:

    def deploy(moduleName, inputJson):
        logging.info ("Not implemented Yet!")
        
    def undeployDocker(moduleName):
        logging.onfo ("Not implemented Yet!")
 
