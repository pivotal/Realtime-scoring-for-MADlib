## Installing a ML Micro-batch 
In some scenarios where we have to run a delayed batch operations to scrore model and update the scoring result to client applications via a cache. This particular sample demonstrate that feature.
We are going to use the the sample represents the example from apache madlib help topic[Logistic Regression]( http://madlib.apache.org/docs/latest/group__grp__logreg.html)

### PART 1: Model Development On Greenplum cluster
- In order to run the demo locally, we need a docker environment. If you do not have docker running locally, please setup docker by following instructions on [Docker Getting started](href="https://www.docker.com/get-started")
- Next step is to have a running Greenplum environment.Please run the below script to setup a Greenplum instance on docker; ` $RTSMADLIB_HOME/bin/setup_greenplum_docker`. This script will download the image and starts the container, depending on your network speed the download take few minutes. <br>
- Verify Greenplum cluster by running the below command; when prompted use pivotal for password. 
```
psql -h 127.0.0.1 -p 9432 -d gpadmin -U gpadmin -c "\dn"

List of schemas
Name      |  Owner
------------+---------
gp_toolkit | gpadmin
madlib     | gpadmin
public     | gpadmin
(3 rows)
```
- Once we verify the greenplum and MADlib, we are ready to install sample data and develop a model on greenplum. Please run below from command line to create a sample data and train a model; <br>
```
psql -h 127.0.0.1 -p 9432 -d gpadmin -U gpadmin -f $RTSMADLIB_HOME/samples/mlbatch_demo/sql/logistic_regression.sql</i></b>
```
Verify that you have the model created in Greenplum; 
```
psql -h 127.0.0.1 -p 9432 -d gpadmin -U gpadmin -c "select * from madlib_demo.patients_logregr"
```
The output you see above is from the model logistic regression model table.

### PART 2: A batch trigger application on SpringBoot Docker container.</h2>

Here we create a server side function on Greenplum to score the model for records in a table and return results via a table. Below is the function which does this operation. This function is coded as plpgsql but can be coded in PL/Python or PL/R.

```
CREATE OR REPLACE FUNCTION madlib_demo.patient_prediction_batch() RETURNS integer    
AS $$ 
begin

drop table if exists madlib_demo.patient_ha_predict;

create table madlib_demo.patient_ha_predict as
SELECT p.id as patient_id, 
madlib.logregr_predict(coef, ARRAY[1, treatment, trait_anxiety]) prediction,
p.second_attack::BOOLEAN
FROM madlib_demo.patients p, madlib_demo.patients_logregr m
ORDER BY p.id;

drop table if exists madlib_demo.patient_ha_predict_prob;

create table madlib_demo.patient_ha_predict_prob as
SELECT p.id as patient_id, 
madlib.logregr_predict_prob(coef, ARRAY[1, treatment, trait_anxiety]) probablity,
p.second_attack::BOOLEAN
FROM madlib_demo.patients p, madlib_demo.patients_logregr m
ORDER BY p.id;
return 1;    
exception
when others then
return -1;
end
$$
LANGUAGE plpgsql;
```

In the above function we are scoring model and producing two result tables "madlib_demo.patient_ha_predict" and "madlib_demo.patient_ha_predict_prob". We follow some what the scemantics of MADlib; read input from a table and produce output to a table so that we can standardize the api contract. Also client application is stateless and loosely coupled with serverside logic.
On the Batch depoloyment side we manifest the spring boot application with below;
```
{
"spring.profiles.active": "redis",
"datasource.jdbc-url": "jdbc:postgresql://172.17.0.2:5432/gpadmin",
"datasource.userName": "gpadmin",
"datasource.password": "secret",
"redis.clustertype": "standalone",
"redis.hostname": "localhost",
"redis.port": 6379,
"redis.password": "secret",
"mlbatch.name": "test mico batch",
"mlbatch.schema": "madlib_demo",
"mlbatch.batchFunction": "patient_prediction_batch",
"mlbatch.hydrateCache": true,
"mlbatch.loadBatchSize": 500,
"mlbatch.cacheSourceTables":  {
    "patient_ha_predict" : "patient_id",
    "patient_ha_predict_prob" : "patient_id"
}
}
```

In the above manifest we are specifying the Greenplum connection information and the redis cache that need to be updated after the batch run.
Also we specify the batchFunction that need to be invoked on Greenplum. In this instance the pgpsql function "patient_prediction_batch" is invoked and update the cache after execution. The cache is sourced from the tables "cacheSourceTables" attribute. This attribute can take a map of table name and key values so.

Before deploy this sample, please make sure you have redis running and have the credentials in handy. Please update the above manifest as per your setup. The template file this sample is in 
the folder ```$RTSMADLIB_HOME/samples/mlbatch_demo/input/mlbatch.json".

Now let us deploy this application on to docker environment.

```
rts4madlib --name patients --type batch --action deploy --target docker --inputJson $RTSMADLIB_HOME/samples/mlbatch_demo/input/mlbatch.json
```

once we have the container up we are going run the information url on the container.

```
curl -v -H "Accept:application/json" http://127.0.0.1:8400/actuator/info
*   Trying 127.0.0.1...
* TCP_NODELAY set
* Connected to 127.0.0.1 (127.0.0.1) port 8400 (#0)
> GET /actuator/info HTTP/1.1
> Host: 127.0.0.1:8400
> User-Agent: curl/7.63.0
> Accept:application/json
>
< HTTP/1.1 200
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Tue, 06 Aug 2019 14:01:57 GMT
<
* Connection #0 to host 127.0.0.1 left intact
{"Feature Cache - ":"test mico batch","Batch Function - ":"patient_prediction_batch","source Schema - ":"madlib_demo","Scores source table - ":"patient_ha_predict :: patient_id","Scores keys size - ":0}
```

Now we see in the results are empty in the cache. So let us run the batch operation and verify the catch.

```
curl -v -H "Accept:application/json" http://127.0.0.1:8401/batch/run
*   Trying 127.0.0.1...
* TCP_NODELAY set
* Connected to 127.0.0.1 (127.0.0.1) port 8401 (#0)
> GET /batch/run HTTP/1.1
> Host: 127.0.0.1:8401
> User-Agent: curl/7.63.0
> Accept:application/json
>
< HTTP/1.1 200
< Content-Length: 0
< Date: Tue, 06 Aug 2019 14:12:59 GMT
<
* Connection #0 to host 127.0.0.1 left intact
```

after the batch run invokde the info endpoint to see the cache counts;

```

curl -v -H "Accept:application/json" http://127.0.0.1:8401/actuator/info
*   Trying 127.0.0.1...
* TCP_NODELAY set
* Connected to 127.0.0.1 (127.0.0.1) port 8401 (#0)
> GET /actuator/info HTTP/1.1
> Host: 127.0.0.1:8401
> User-Agent: curl/7.63.0
> Accept:application/json
>
< HTTP/1.1 200
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Tue, 06 Aug 2019 14:13:45 GMT
<
* Connection #0 to host 127.0.0.1 left intact

{"Feature Cache - ":"test mico batch","Batch Function - ":"patient_prediction_batch","source Schema - ":"madlib_demo","Scores source table - ":"patient_ha_predict :: patient_id","Scores keys size - ":40}
```
Notice that the scores were updated.

This is the basic framework for micro-batch the model scoring. The scenario can be extended to several usecases via control delegated to model functions on Greenplum.
Further this can be automated with some streaming event or spring boot cron style invocation, etc.

