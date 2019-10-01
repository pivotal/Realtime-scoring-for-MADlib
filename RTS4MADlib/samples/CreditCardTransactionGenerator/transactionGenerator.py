#!/usr/bin/env python
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
    Name: transactionGnerator.py
    Author: Jarrod Vawdrey
    Description: 

"""
import time
import json
import random
import numpy as np
import pandas as pd
import csv
import math
import sys
import logging
from datetime import timedelta,datetime
from kafka import KafkaProducer, KafkaClient
import geopy.distance
import enlighten
import redis

import fraudSignatures as fs

class myDataFiles:

    def __init__(self):

        # import locations data
        with open('locations.json') as data_file:
            locations = json.load(data_file)

        # prep locations data in with state for key
        uniqueStatesList = []
        for loc in locations:
            if(loc['merchant_state'] not in uniqueStatesList):
                 uniqueStatesList.append(loc['merchant_state'])
        uniqueStates = {}
        for state in uniqueStatesList:
            uniqueStates[state] = [loc for loc in locations if loc['merchant_state'] == state]

        # import accounts data
        with open('accounts.json') as data_file:
            accounts = json.load(data_file)

        self.locations = locations
        self.uniqueStatesList = uniqueStatesList
        self.uniqueStates = uniqueStates
        self.accounts = accounts

        # Setup progress bar
        manager = enlighten.get_manager()
        N = len(self.accounts)

        msg = "Mapping {} locations to {} accounts".format(len(self.locations), N)
        logging.info(msg)
        pbar = manager.counter(total=N, desc='Progress', unit='account')

        # build list of locations within "distance" of account holders home address
        self.accounts_location = {}
        for a in self.accounts:
            self.accounts_location[a['account_id']] = []
            for l in self.locations:
                dist = geopy.distance.vincenty((a['lat'], a['long']), (l['merchant_lat'],l['merchant_long'])).miles
                if (dist < a['transaction_radius']):
                    l['merchant_distance'] = dist
                    self.accounts_location[a['account_id']].append(l)
            pbar.update()

        manager.stop()

class myTimestamp():

    def __init__(self, minOpen=2, maxClose=23):
        self.timestamp = datetime.now()
        self.minOpen = minOpen
        self.maxClose = maxClose

    # To Do: Make this location specific
    def newTimestamp(self, loc):

        rnd = int(random.random() * 10)
        self.timestamp = self.timestamp + timedelta(seconds=rnd)

        dow = self.timestamp.weekday()
        days = ['mon','tue','wed','thu','fri','sat','sun']

        open = loc['{}{}'.format(days[dow],'_open')]
        close = loc['{}{}'.format(days[dow],'_close')]

        if self.timestamp.hour < self.minOpen:
            self.timestamp.replace(hour=self.minOpen)

        if self.timestamp.hour > self.maxClose:
            N = 24 - self.timestamp.hour + self.minOpen
            self.timestamp = self.timestamp.replace(minute=0, second=0) + timedelta(hours=N)

        if open == -1 or close == -1 or self.timestamp.hour < open or self.timestamp.hour > close:
            rnd = int(random.random() * 10)
            self.timestamp = self.timestamp + timedelta(minutes=rnd)
            return False

        return time.mktime(self.timestamp.timetuple())

def output_file(filename, records, type='json'):
    try:
        if type == 'csv':
            df = pd.read_json(json.dumps(records), orient='list')
            df.to_csv(filename,index=False,quoting=csv.QUOTE_NONNUMERIC)
        elif type == 'json':
            f = open(filename,'w')
            for rec in records:
                f.write(json.dumps(rec) + '\n')
            f.close()
    except Exception as e:
        logging.error(e)

def iterate_transaction_id(datafiles, transaction_id):
    # iterate transaction_id
    for i in range(0,len(datafiles.locations)):
        if datafiles.locations[i]['transaction_id'] == transaction_id:
            datafiles.locations[i]['transaction_id'] += 1

def random_location(datafiles, acct):

    # build list of locations within "distance" of account holders home address
    close_locations = []
    close_locations = datafiles.accounts_location[acct['account_id']]

    #close_locations = []
    #for l in datafiles.locations:
    #    dist = geopy.distance.vincenty((lat, long), (l['merchant_lat'],l['merchant_long'])).miles
    #    if (dist < distance):
    #        l['merchant_distance'] = dist
    #        close_locations.append(l)

    msg = "{} total location found within {} miles".format(len(close_locations),acct['transaction_radius'])
    logging.debug(msg)

    if (close_locations != []):
        loc = random.choice(close_locations)

    # no locations found - looks within state
    elif (acct['state'] in datafiles.uniqueStatesList):
        msg = "No merchant found within {} miles - choosing location within state".format(acct['transaction_radius'])
        logging.debug(msg)
        loc = random.choice(datafiles.uniqueStates[acct['state']])

    # final option - pick location at random
    else:
        msg = "No merchant found within {} miles or state {} - choosing random location".format(acct['transaction_radius'], acct['state'])
        logging.debug(msg)
        loc = random.choice(datafiles.locations)

    return loc

def random_account(datafiles):
    return random.choice(datafiles.accounts)

def generate_transaction(datafiles, ts, fraud, storeFraudFlag, target):

    # transaction date

    # Grab random account
    acct = random_account(datafiles)

    # Grab random merchant location
    loc = random_location(datafiles, acct)
    iterate_transaction_id(datafiles, loc['transaction_id'])

    trxnTS = ts.newTimestamp(loc)
    if trxnTS == False:
        return False

    # Create transaction (account dependent amount) - 20%
    if (np.random.rand() < 0.2):
        trxn_amount = str(round(np.random.normal(acct['trxn_mean'], acct['trxn_std']), 2))

    # Create transaction (merchant dependent amount) - 80%
    else:
        trxn_amount = str(round(np.random.normal(float(loc['merchant_trxn_mean']), float(loc['merchant_trxn_std'])), 2))

    if (target == "file"):
      trxn = {
        'rlb_location_key': loc['rlb_location_key']
        ,'account_id': acct['account_id']
        ,'account_number': acct['account_number']
        ,'account_lat': acct['lat']
        ,'account_long': acct['long']
        ,'card_type': acct['card_type']
        ,'location_id': loc['location_id']
        ,'merchant_city': loc['merchant_city']
        ,'merchant_city_alias': loc['merchant_city_alias']
        ,'merchant_name': loc['merchant_name']
        ,'merchant_state': loc['merchant_state']
        ,'merchant_long': loc['merchant_long']
        ,'merchant_lat': loc['merchant_lat']
        ,'posting_date': time.time()
        ,'transaction_amount': trxn_amount
        ,'transaction_date': trxnTS
        ,'transaction_id': loc['transaction_id']
      }
    elif (target == "kafka"):
      trxn = {
        'rlb_location_key': loc['rlb_location_key']
        ,'account_id': acct['account_id']
        ,'account_number': acct['account_number']
        ,'account_lat': acct['lat']
        ,'account_long': acct['long']
        ,'card_type': acct['card_type']
        ,'location_id': loc['location_id']
        ,'merchant_city': loc['merchant_city']
        ,'merchant_city_alias': loc['merchant_city_alias']
        ,'merchant_name': loc['merchant_name']
        ,'merchant_state': loc['merchant_state']
        ,'merchant_long': loc['merchant_long']
        ,'merchant_lat': loc['merchant_lat']
        ,'posting_date': time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time()))
        ,'transaction_amount': trxn_amount
        ,'transaction_date': time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(trxnTS))
        ,'transaction_id': loc['transaction_id']
      }    

    if (storeFraudFlag == True):
        trxn['fraud_flag'] = False

    # Update transaction if fraud case
    if (fraud == True):

        msg = "***** Generating fraud transaction *****"
        logging.debug(msg)
        trxn = fs.transform(trxn, acct, loc)

    return trxn


    if (storeFraudFlag == True):
        trxn['fraud_flag'] = False

    # Update transaction if fraud case
    if (fraud == True):

        msg = "***** Generating fraud transaction *****"
        logging.debug(msg)
        trxn = fs.transform(trxn, acct, loc)

    return trxn
    
def generate_kafka_data(myConfigs):
    redis_host = myConfigs['redis']['host']
    redis_port = myConfigs['redis']['port']
    redis_pwd = myConfigs['redis']['passwd']
    redis_db= redis.StrictRedis(host=redis_host, port=redis_port, password=redis_pwd)    
    transactionNumber = myConfigs['generator']['transactionNumber']
    everyNFraud = myConfigs['generator']['FraudEveryNTransactions']
    sleepBetweenIterations = myConfigs['generator']['sleepBetweenIterations']
    storeFraudFlag = myConfigs['generator']['storeFraudFlag']
    datafiles = myDataFiles()
    ts = myTimestamp()
    logging.debug("Transaction Generator: Applying fraud signature every {} transactions".format(everyNFraud))
    iter_counter = 0
    results = []
    bootstrapServers = myConfigs['target']['kafka']
    topic = myConfigs['target']['topic']
    batch=100
    producer = KafkaProducer(bootstrap_servers=bootstrapServers)
    for i in range(0,transactionNumber):
        iter_counter += 1
        # MOD
        fraud = False
        if (iter_counter % everyNFraud == 0):
            logging.debug("***** Generating fraud record *****")
            fraud = True

        msg = generate_transaction(datafiles, ts, fraud, storeFraudFlag, 'kafka')
        if msg == False:
            iter_counter -= 1
        else:
	    logging.debug(msg)
	    logging.debug(json.dumps(msg).encode('utf-8'))
            producer.flush()
            producer.send(topic, json.dumps(msg).encode('utf-8'))
	    redis_db.incr("TransactionsCount")
        if(iter_counter % batch ==0):
            time.sleep(sleepBetweenIterations)

def generate_file_data(myConfigs):
    
    transactionNumber = myConfigs['generator']['transactionNumber']
    everyNFraud = myConfigs['generator']['FraudEveryNTransactions']
    transactionPerFile = myConfigs['target']['transactionPerFile']
    storeFraudFlag = myConfigs['generator']['storeFraudFlag']


    datafiles = myDataFiles()
    ts = myTimestamp()
    
    iter_counter = 0
    batch_counter = 0
    results = []
    logging.info("start generating transactions .............")
    logging.info("Applying fraud signature every {} transactions ........".format(everyNFraud))
    # Setup progress bar
    manager = enlighten.get_manager()
    pbar = manager.counter(total=transactionNumber, desc='Progress', unit='transaction')
    for i in range(0,transactionNumber):
        iter_counter += 1

        # MOD
        fraud = False
        if ((iter_counter % everyNFraud) == 0):
            fraud = True

        msg = generate_transaction(datafiles, ts, fraud, storeFraudFlag, 'file')
        if msg == False:
            iter_counter -= 1
        else:

            results.append(msg)

            if (iter_counter == transactionPerFile or i == transactionNumber-1):

                filename = 'transactions_{}.{}'.format((str(time.time())).replace('.', ''),myConfigs['target']['type'])
                locationFilename = '{}{}'.format(myConfigs['target']['transactionsFileLoctation'],filename)
                output_file(locationFilename, results, myConfigs['target']['type'])
                iter_counter = 0
                results = []
                batch_counter += 1
                time.sleep(myConfigs['generator']['sleepBetweenFiles'])

            time.sleep(myConfigs['generator']['sleepBetweenIterations'])
        pbar.update()
if __name__ == '__main__':
    ts = myTimestamp()
