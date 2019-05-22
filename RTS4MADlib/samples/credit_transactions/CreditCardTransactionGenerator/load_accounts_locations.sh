#!/bin/sh

psql -h 35.185.82.155 -U gpadmin -d gpadmin -c "INSERT INTO credit_trans.accounts (SELECT raw_accounts->>'account_number' AS account_number  ,raw_accounts->>'expiration_date' AS expiration_date  ,raw_accounts->>'cvv' AS cvv ,raw_accounts->>'card_type' AS card_type ,raw_accounts->>'city' AS account_city ,raw_accounts->>'city_alias' AS account_city_alias ,(raw_accounts->>'state')::varchar(2) AS account_state  ,(raw_accounts->>'long')::float AS account_long  ,(raw_accounts->>'lat')::float AS account_lat ,(raw_accounts->>'transaction_radius')::integer AS account_transaction_radius ,(raw_accounts->>'trxn_mean')::float AS trxn_mean,(raw_accounts->>'trxn_std')::float AS trxn_std,(raw_accounts->>'account_id')::integer AS account_id FROM ( SELECT json_array_elements(raw_accounts)AS raw_accounts FROM credit_trans.raw_accounts ) foo );"


psql -h 35.185.82.155 -U gpadmin -d gpadmin -c "INSERT INTO credit_trans.locations ( SELECT raw_locations->>'rlb_location_key' AS rlb_location_key,raw_locations->>'merchant_name' AS merchant_name,(raw_locations->>'merchant_trxn_mean')::float AS merchant_trxn_mean,(raw_locations->>'merchant_trxn_std')::float AS merchant_trxn_std,raw_locations->>'merchant_city' AS merchant_city,(raw_locations->>'merchant_state')::varchar(2) AS merchant_state,(raw_locations->>'merchant_long')::float AS merchant_long ,(raw_locations->>'merchant_lat')::float AS merchant_lat,raw_locations->>'merchant_city_alias' AS merchant_city_alias,(raw_locations->>'transaction_id')::integer AS transaction_id,(raw_locations->>'location_id')::integer AS location_id FROM ( SELECT json_array_elements(raw_locations) AS raw_locations FROM credit_trans.raw_locations ) foo );"


psql -h 35.185.82.155 -U gpadmin -d gpadmin -c "SELECT count(*) FROM credit_trans.accounts;"


psql -h 35.185.82.155 -U gpadmin -d gpadmin -c "SELECT count(*) FROM credit_trans.locations;"