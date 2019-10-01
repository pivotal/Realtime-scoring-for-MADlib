#!/usr/bin/python
#-------------------------------------------------------------------------------
# Copyright 2019 Pivotal Software Inc
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
"""
    Name: Generator.py
    Author: Jarrod Vawdrey
    Description: 

"""
import yaml
import os
import json
import logging
import sys

import accountGenerator as ag
import merchantGenerator as mg
import transactionGenerator as tg

YAML_FILE="myConfigs.yml"
LOGGING_LEVEL=logging.INFO

def readYaml(filename):
    cwd = os.getcwd()
    print(cwd)
    with open(filename, 'r') as stream:
        try:
            myConfigs = yaml.load(stream)
            return myConfigs
        except yaml.YAMLError as exc:
            logging.critical(exc)
            sys.exit()

def checkAndDropFile(filename):
    if os.path.exists(filename):
        os.remove(filename)
        return True
    else:
        return False

if __name__ == '__main__':

    # logging configurations
    logging.basicConfig(format='%(asctime)s - %(message)s',level=LOGGING_LEVEL)

    # read configs
    myConfigs = readYaml(YAML_FILE)

    try:
        restrictToStates = myConfigs['constraints']['states']
    except Exception as e:
        logging.error(e)
        restrictToStates = []

    if myConfigs['configs']['createAccountsJson'] == True:
        # clean up files if already exist
        checkAndDropFile('accounts.json')
        # create accounts.json file
        ag.accountGenerator(myConfigs, restrictToStates)

    if myConfigs['configs']['createLocationsJson'] == True:
        # clean up files if already exist
        checkAndDropFile('locations.json')
        # create locations.json file
        mg.merchantGenerator(myConfigs['generator']['merchantsNumber'], myConfigs['data'], restrictToStates)

    # simulate transactions
    if (myConfigs['target']['type'] == 'kafka'):
      tg.generate_kafka_data(myConfigs)

    elif (myConfigs['target']['type'] in ['json','csv']):
        tg.generate_file_data(myConfigs)
    logging.info("!")
    logging.info("Finished generating data!")  
