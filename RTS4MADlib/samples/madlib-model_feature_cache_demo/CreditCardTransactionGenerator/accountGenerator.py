"""
    Name: accountGenerator.py
    Author: Jarrod Vawdrey
    Description: Generate accounts data
    
    Extended from -- https://github.com/eye9poob/python/blob/master/credit-card-numbers-generator.py
    Author -- crazyjunkie ::.. 2014
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

NUM_RECORDS = 10000


# ---------- CREDIT CARD NUMBER ----------
visaPrefixList = [
        ['4', '5', '3', '9'],
        ['4', '5', '5', '6'],
        ['4', '9', '1', '6'],
        ['4', '5', '3', '2'],
        ['4', '9', '2', '9'],
        ['4', '0', '2', '4', '0', '0', '7', '1'],
        ['4', '4', '8', '6'],
        ['4', '7', '1', '6'],
        ['4']]

mastercardPrefixList = [
        ['5', '1'],
        ['5', '2'],
        ['5', '3'],
        ['5', '4'],
        ['5', '5'],
        ['2', '2', '2', '1'],
        ['2', '2', '2', '2'],
        ['2', '2', '2', '3'],
        ['2', '2', '2', '4'],
        ['2', '2', '2', '5'],
        ['2', '2', '2', '6'],
        ['2', '2', '2', '7'],
        ['2', '2', '2', '8'],
        ['2', '2', '2', '9'],
        ['2', '2', '3'],
        ['2', '2', '4'],
        ['2', '2', '5'],
        ['2', '2', '6'],
        ['2', '2', '7'],
        ['2', '2', '8'],
        ['2', '2', '9'],
        ['2', '3'],
        ['2', '4'],
        ['2', '5'],
        ['2', '6'],
        ['2', '7', '0'],
        ['2', '7', '1'],
        ['2', '7', '2', '0']]

amexPrefixList = [['3', '4'], ['3', '7']]

discoverPrefixList = [['6', '0', '1', '1']]

dinersPrefixList = [
        ['3', '0', '0'],
        ['3', '0', '1'],
        ['3', '0', '2'],
        ['3', '0', '3'],
        ['3', '6'],
        ['3', '8']]

enRoutePrefixList = [['2', '0', '1', '4'], ['2', '1', '4', '9']]

jcbPrefixList = [['3', '5']]

voyagerPrefixList = [['8', '6', '9', '9']]


def completed_number(generator, prefix, length):
    '''
    'prefix' is the start of the CC number as a string, any number of digits.
    'length' is the length of the CC number to generate. Typically 13 or 16
    '''

    ccnumber = prefix
    # generate digits

    while len(ccnumber) < (length - 1):
        digit = str(generator.choice(range(0, 10)))
        ccnumber.append(digit)

    # Calculate sum

    sum = 0
    pos = 0

    reversedCCnumber = []
    reversedCCnumber.extend(ccnumber)
    reversedCCnumber.reverse()

    while pos < length - 1:

        odd = int(reversedCCnumber[pos]) * 2
        if odd > 9:
            odd -= 9

        sum += odd

        if pos != (length - 2):

            sum += int(reversedCCnumber[pos + 1])

        pos += 2

    # Calculate check digit

    checkdigit = ((sum / 10 + 1) * 10 - sum) % 10

    ccnumber.append(str(checkdigit))

    return str(int(float(''.join(ccnumber))))


def credit_card_number(rnd, prefixList, length):

    result = []

    ccnumber = copy.copy(rnd.choice(prefixList))

    return completed_number(rnd, ccnumber, length)

# ---------- CREDIT CARD EXPIRATION DATE ----------
def month_year(rnd):
    year = rnd.randint(2019, 2024)
    month = rnd.randint(1, 12)
    return '{}/{}'.format(month,year)


# ---------- CREDIT CARD CVV CODE ----------
def cvv(rnd):
    min = 0
    max = 999
    digit = str(rnd.randint(min, max))
    return (len(str(max))-len(digit))*'0'+digit

# ---------- UNIQUE LONG/LAT ----------
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

# ---------- ACOUNT CITY, STATE ----------
def account_city_state(rnd, cities_states_df, restrictToStates):
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

    long, lat = random_long_lat(long, lat)

    return (city, state, long, lat, alias)


# ---------- TRXN MEAN AND STDDEV ----------
def transaction_mean_std(myConfigs, rnd):
    mean = rnd.randint(myConfigs['generator']['minTransactionRadiusTarget'], myConfigs['generator']['maxTransactionRadiusTarget'])
    stddev = rnd.randint(1, math.ceil(mean / 2.0))
    return (mean, stddev)


def create_card_info(myConfigs, rnd, cities_states_df, restrictToStates):

    typeList = [
     ['MasterCard', mastercardPrefixList, 16],
     ['Visa', visaPrefixList, 16],
     ['Amex', amexPrefixList, 15],
     ['Discover', discoverPrefixList, 16],
     ['Diners', dinersPrefixList, 14]
    ]

    t = rnd.randint(0, 4)

    cardInfo = {}

    cardInfo['account_id'] =  0
    cardInfo['account_number'] = credit_card_number(rnd, typeList[t][1], typeList[t][2])
    cardInfo['expiration_date'] = month_year(rnd)
    cardInfo['cvv'] = cvv(rnd)
    cardInfo['card_type'] = typeList[t][0]
    city, state, long, lat, alias = account_city_state(rnd, cities_states_df, restrictToStates)
    cardInfo['city'] = city
    cardInfo['city_alias'] = alias
    cardInfo['state'] = state


    cardInfo['long'] = long
    cardInfo['lat'] = lat
    cardInfo['transaction_radius'] = rnd.randint(myConfigs['generator']['minTransactionRadiusTarget'], myConfigs['generator']['maxTransactionRadiusTarget'])
    mean, stddev = transaction_mean_std(myConfigs, rnd)
    cardInfo['trxn_mean'] = mean
    cardInfo['trxn_std'] = stddev

    return cardInfo

def accountGenerator(myConfigs, restrictToStates=[]):

    numberOfRecords = myConfigs['generator']['accountsNumber']
    dataFiles = myConfigs['data']

    generator = Random()
    generator.seed()

    cities_states_df = pandas.read_csv(dataFiles['locations'], sep='|')

    results = []

    for i in range(0,numberOfRecords):

        cardInfo = create_card_info(myConfigs, generator, cities_states_df, restrictToStates)
        cardInfo['account_id'] = i
        results.append(cardInfo)

    msg = 'Account Generator: {} total account records created'.format(len(results))
    logging.info(msg)

    with open('accounts.json', 'w') as accounts_file:
        json.dump(results, accounts_file)

    accounts_file.close()

if __name__ == '__main__':
    dataFiles = {'retailers': 'us_retailers.csv', 'locations': 'us_cities_states_counties_longlats.csv'}
    accountGenerator(NUM_RECORDS, dataFiles)
