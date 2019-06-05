# Data Simulator

```bash

# install dependencies
pip install -r REQUIREMENTS.txt

# Add config to myConfigs.yml
cp myConfigsExample.yml myConfigs.yml
python Generator.py

```

#### Results

```json

# Accounts
{
  "account_id": 0,
  "account_number": "2226719519354860",
  "expiration_date": "2/2020",
  "cvv": "317",
  "card_type": "MasterCard",
  "city": "Register",
  "city_alias": "Register",
  "state": "GA",
  "long": "-81.884004",
  "lat": "32.367124",
  "transaction_radius": 20,
  "trxn_mean": 30,
  "trxn_std": 7
}

# Location
{
  "location_id": 0,
  "rlb_location_key": 0,
  "merchant_name": "Microsoft Corp",
  "merchant_trxn_mean": 500.0,
  "merchant_trxn_std": 25.0,
  "merchant_city": "Wiley",
  "merchant_state": "GA",
  "merchant_long": "-83.416275",
  "merchant_lat": "34.782038",
  "merchant_city_alias": "Wiley",
  "transaction_id": 0
}

# Transaction
{
  "rlb_location_key": 38,
  "account_id": 0,
  "location_id": 0,
  "account_number": "2227412129683430",
  "card_type": "MasterCard",
  "merchant_city": "Guyton",
  "merchant_city_alias": "Pineora",
  "merchant_name": "Chick-fil-A",
  "merchant_state": "GA",
  "merchant_long": "-81.390096",
  "merchant_lat": "32.285888",
  "posting_date": 1543703486.429989,
  "transaction_amount": "9.25",
  "transaction_date": 1543703486.429989,
  "transaction_id": 1,
  "fraud_flag": false
}

```

### Notice on Data

Data found in this repo has been simulated or completely made up. The pricing information
does not accurately reflect actual business sales.


### Kafka Testing

```bash
# this will install java 1.8, zookeeper, and kafka
brew install kafka

# this will run ZK and kafka as services
zkserver start
brew services start kafka

/usr/local/Cellar/kafka/2.0.0/bin/kafka-server-start /usr/local/etc/kafka/server.properties

/usr/local/Cellar/kafka/2.0.0/bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test-topic

/usr/local/Cellar/kafka/2.0.0/bin/kafka-topics --list --zookeeper localhost:2181

/usr/local/Cellar/kafka/2.0.0/bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic --from-beginning

brew services stop kafka
brew services stop zookeeper

```


docker cp transactions_1543711919078363.json gpdb-ds:/home/gpadmin/transactions_1543711919078363.json
docker cp transactions_1543712579775396.json gpdb-ds:/home/gpadmin/transactions_1543712579775396.json
docker cp transactions_1543713236771273.json gpdb-ds:/home/gpadmin/transactions_1543713236771273.json
docker cp transactions_1543713893938922.json gpdb-ds:/home/gpadmin/transactions_1543713893938922.json

docker cp accounts.json gpdb-ds:/home/gpadmin/accounts.json
docker cp locations.json gpdb-ds:/home/gpadmin/locations.json
