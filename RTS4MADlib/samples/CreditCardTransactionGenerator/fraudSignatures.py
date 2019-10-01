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
    Name: fraudSignatures.py
    Author: Jarrod Vawdrey
    Description: 

"""

import logging
import numpy as np

def transform(transaction, account, merchant):

    if (account == None):
        logging.error("Fraud Signatures: No account or default account values found")
        return {}

    if (merchant == None):
        logging.error("Fraud Signatures: No merchant or default merchant values found")
        return {}

    if 'fraud_flag' in transaction:
        transaction['fraud_flag'] = True

    decRand = np.random.rand()
    if (decRand < 0.4):

        # large transaction amount for account
        trxn_amount = str(round(np.random.normal(account['trxn_mean'], account['trxn_std']) * account['trxn_mean'] * 1000, 2))
        transaction['transaction_amount'] = trxn_amount

    elif (decRand < 0.8):

        # large transaction amount for merchant
        trxn_amount = str(round(np.random.normal(merchant['merchant_trxn_mean'], merchant['merchant_trxn_std']) * merchant['merchant_trxn_mean'] * 1000, 2))
        transaction['transaction_amount'] = trxn_amount

    else:

        # bad merchant
        transaction['merchant_city'] = 'Moscow'
        transaction['merchant_state'] = 'RS'
        transaction['merchant_name'] = 'ACME Hackers'

    return transaction
