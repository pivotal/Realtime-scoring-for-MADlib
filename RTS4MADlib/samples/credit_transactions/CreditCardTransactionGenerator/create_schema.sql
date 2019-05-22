
-- create working schema
DO $$
BEGIN

    IF NOT EXISTS(
        SELECT schema_name
          FROM information_schema.schemata
          WHERE schema_name = 'credit_trans'
      )
    THEN
      EXECUTE 'CREATE SCHEMA credit_trans';
    ELSE
      EXECUTE 'DROP SCHEMA credit_trans CASCADE';
      EXECUTE 'CREATE SCHEMA credit_trans';  
    END IF;

END
$$;

CREATE TABLE credit_trans.raw_accounts (
    raw_accounts json
);

DROP TABLE IF EXISTS credit_trans.accounts;

CREATE TABLE credit_trans.accounts (
    account_number text,
    expiration_date text,
    cvv text,
    card_type text,
    account_city text,
    account_city_alias text,
    account_state text,
    account_long float,
    account_lat float,
    account_transaction_radius integer,
    trxn_mean float,
    trxn_std float,
    account_id integer
);


CREATE TABLE credit_trans.raw_locations (
    raw_locations json
);

CREATE TABLE credit_trans.locations (
    rlb_location_key text,
    merchant_name text,
    merchant_trxn_mean float,
    merchant_trxn_std float,
    merchant_city text,
    merchant_state varchar(2),
    merchant_long float,
    merchant_lat float,
    merchant_city_alias text,
    transaction_id integer,
    location_id integer
);

CREATE TABLE credit_trans.transactions (
   account_id integer,
   account_number text,
   card_type text,
   fraud_flag boolean,
   location_id integer,
   merchant_city text,
   merchant_city_alias text,
   merchant_lat double precision,
   merchant_long double precision,
   merchant_name text,
   merchant_state varchar(2),
   posting_date timestamp,
   rlb_location_key text,
   transaction_amount double precision,
   transaction_date timestamp,
   transaction_id integer
)WITH (appendonly=true)
distributed randomly;

CREATE  TABLE credit_trans.transactions_flagged (
	account_id integer,
	account_number text ,
	card_type text ,
	fraud_flag bool ,
	location_id integer ,
	merchant_city text ,
	merchant_city_alias text ,
	merchant_lat double precision ,
	merchant_long double precision ,
	merchant_name text ,
	merchant_state text ,
	merchant_state_2 text ,
	estimated_prob_false integer,
	a_transaction_delta double precision,
	m_fraud_cases integer,
	m_transaction_delta double precision,
	posting_date text ,
	rlb_location_key text ,
	transaction_amount double precision ,
	transaction_date text ,
	transaction_id integer 
) WITH (appendonly=true)
distributed randomly;


create external TABLE credit_trans.gpfdist_transactions (
   account_id integer,
   account_number text,
   card_type text,
   fraud_flag boolean,
   location_id integer,
   merchant_city text,
   merchant_city_alias text,
   merchant_lat double precision,
   merchant_long double precision,
   merchant_name text,
   merchant_state varchar(3),
   posting_date text,
   rlb_location_key text,
   transaction_amount double precision,
   transaction_date text,
   transaction_id integer
)
LOCATION ('gpfdist://:8081/*.csv')
FORMAT  'CSV' (HEADER);

--(DELIMITER AS ',' NULL AS 'null');




