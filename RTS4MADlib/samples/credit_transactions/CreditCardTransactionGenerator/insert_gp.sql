INSERT INTO credit_trans.transactions
(account_id, account_number, card_type, fraud_flag, location_id, merchant_city, merchant_city_alias, merchant_lat, 
merchant_long, merchant_name, merchant_state, posting_date, rlb_location_key, transaction_amount, transaction_date, 
transaction_id)
select account_id, account_number, card_type, fraud_flag, location_id, merchant_city, merchant_city_alias, merchant_lat, 
merchant_long, merchant_name, merchant_state,to_timestamp(posting_date::float), rlb_location_key, transaction_amount, 
to_timestamp(transaction_date::float), transaction_id
from credit_trans.gpfdist_transactions
