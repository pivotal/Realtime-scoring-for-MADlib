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
    Name: merchantGenerator.py
    Author: Jarrod Vawdrey
    Description: 

"""
from datetime import datetime
from random import Random
import copy
import pandas
import math
import csv
import json
import logging
import numpy as np

NUM_RECORDS = 2000

# ---------- MERCHANT NAME ----------
def merchant_name(rnd, merchant_names_df):
    rec = merchant_names_df[['Name','trxn_mean','trxn_std','sun_open','sun_close','mon_open','mon_close','tue_open','tue_close','wed_open','wed_close','thu_open','thu_close','fri_open','fri_close','sat_open','sat_close']].sample(1)
    recClean = {}
    recClean['merchant_name'] = rec['Name'].to_string(index=False)
    recClean['merchant_trxn_mean'] = float(rec['trxn_mean'].to_string(index=False))
    recClean['merchant_trxn_std'] = float(rec['trxn_std'].to_string(index=False))
    days = ['sun','mon','tue','wed','thu','fri','sat']
    for d in days:
        recClean['{}_open'.format(d)] = int(rec['{}_open'.format(d)].to_string(index=False))
        recClean['{}_close'.format(d)] = int(rec['{}_close'.format(d)].to_string(index=False))
    return recClean

# ---------- MERCHANT CITY, STATE ----------
def merchant_city_state(rnd, cities_states_df, restrictToStates):
    cols = ['City','State short','City alias','Longitude','Latitude']
    if (restrictToStates != []):
        rec = cities_states_df[cities_states_df['State short'].isin(restrictToStates)][cols].sample(1)
    else:
        rec = cities_states_df[cols].sample(1)
    city = rec['City'].to_string(index=False)
    alias = rec['City alias'].to_string(index=False)
    state = rec['State short'].to_string(index=False)
    long = float(rec['Longitude'].to_string(index=False))
    lat = float(rec['Latitude'].to_string(index=False))
    return (city, state, long, lat, alias)

def random_long_lat(long, lat):
    # Latitude
    northSouth = np.random.normal(0.005,0.0001)
    if (np.random.rand() < 0.5):
        northSouth = northSouth * -1.0

    # Longitude
    eastWest = np.random.normal(0.005,0.0001)
    if (np.random.rand() < 0.5):
        eastWest = eastWest * -1.0

    return (long + eastWest, lat + northSouth)


def create_merchant_info(rnd, cities_states_df, merchant_names_df, restrictToStates):

    merchantInfo = {}
    merchantInfo['location_id'] = 0
    merchantInfo['rlb_location_key'] = 1

    merchantRecord = merchant_name(rnd, merchant_names_df)

    merchantInfo.update(merchantRecord)

    city, state, long, lat, alias = merchant_city_state(rnd, cities_states_df, restrictToStates)
    long, lat = random_long_lat(long, lat)
    merchantInfo['merchant_city'] = city
    merchantInfo['merchant_state'] = state
    merchantInfo['merchant_long'] = long
    merchantInfo['merchant_lat'] = lat
    merchantInfo['merchant_city_alias'] = alias
    merchantInfo['transaction_id'] = 0

    return merchantInfo


def merchantGenerator(numberOfRecords, dataFiles, restrictToStates=[]):

    generator = Random()
    generator.seed()

    merchant_names_df = pandas.read_csv(dataFiles['retailers'], sep='|')
    cities_states_df = pandas.read_csv(dataFiles['locations'], sep='|')

    results = []

    for i in range(0,numberOfRecords):

        merchant = create_merchant_info(generator, cities_states_df, merchant_names_df, restrictToStates)

        merchant['location_id'] = i;

        # check if merchant + city + merchant_city_alias + state already exists
        # TO DO: Change this - very slow
        drop = False
        merchantCnt = 0
        for res in results:
            if res['merchant_name'] == merchant['merchant_name']:
                if res['merchant_city'] == merchant['merchant_city_alias'] and res['merchant_city_alias'] == merchant['merchant_city_alias'] and res['merchant_state'] == merchant['merchant_state']:
                    drop = True
                else:
                    merchantCnt += 1

        if drop == False:
            merchant['rlb_location_key'] = merchantCnt
            results.append(merchant)

    msg = 'Merchant Generator: {} total location records created'.format(len(results))
    logging.info(msg)

    with open('locations.json', 'w') as location_file:
        json.dump(results, location_file)

    location_file.close()

if __name__ == '__main__':
    dataFiles = {'retailers': 'us_retailers.csv', 'locations': 'us_cities_states_counties_longlats.csv'}
    merchantGenerator(NUM_RECORDS, dataFiles)
