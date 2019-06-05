## Installing a  model with Result Table and multiple mode tables on Docker environment
In this sample let us explore how to deploy model involved with multiple base table and customizable return results.
### PART 1: Model Development On Greenplum cluster
 - In order to run the demo locally, we need a docker environment. If you do not have docker running locally, please setup  docker by following instructions on [Docker Getting started](https://www.docker.com/get-started)
 - Next step is to have a running Greenplum environment.Please run the below script to setup a Greenplum instance on docker 
```
 $RTSMADLIB_HOME/bin/setup_greenplum_docker
```
This script will download the image and starts the container, depending on your network speed the download take few minutes. -- Verify Greenplum cluster by running the below command; when prompted use pivotal for password. 
```
psql -h 127.0.0.1 -p 9432 -d gpadmin -U gpadmin -c "\dn"
```
This should print list of schemas
- Once we verify the greenplum and MADlib, we are ready to install sample data and develop a model on greenplum. Please run below from command line to create a sample data and train a model; 
```
 psql -h 127.0.0.1 -p 9432 -d gpadmin -U gpadmin -f $RTSMADLIB_HOME/samples/madlib_model_demo/sql/random_forest.sql
```
- Verify that you have the model created in Greenplum; 
```
psql -h 127.0.0.1 -p 9432 -d gpadmin -U gpadmin -c "select * from madlib_demo.rf_train_output_summary"
```
The output you see above is from the model table.

### PART 2: Model Deployment On SpringBoot Docker container
		
In this section we deploy the model developed on Greenplum to a Docker container as REST service.
- Edit the file `$RTSMADLIB_HOME/samples/madlib_model_demo/input/random_forest.json` and change Greenplum connection information. 
- Deploy the model to docker using below command; 
``` 
rts4madlib --name golfrfm --action deploy --type model --target docker --inputJson $RTSMADLIB_HOME/samples/madlib_model_demo/input/random_forest.json
```
Notice the output of the deployment for port, This is dynamically generated in a range. We need this to invoke the service.
- Once the command finished, verify by running `docker ps`
- Once the container is running run below to verify the REST end point. Please change the port as per the output from step 2. 

```
curl -v -H "Accept:application/json" http://localhost:8095/actuator/info
```
This will return the model deployed in the container. 

- Now we are ready to do predict operations on this newly deployed model. Please change the port as per the output from step 

```
curl -v -H "Content-Type:application/json" -X POST http://localhost:8095/predict -d '{"id":2,"OUTLOOK":"overcast","temperature":64.00,"humidity":65.00,"windy":true,"class":"Play"}'

Result: [{"id":2,"class":"Play","estimated_prob_Don't Play":0.6,"estimated_prob_Play":0.4}] 
```
That's it, we successfully deployed MADlib model as REST end point.
