## Installing Apache MADlib model on Docker as REST service 
In this sample we will deploy a MADlib model with single model table representing a Logistic Regression Model as REST service.
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
	psql -h 127.0.0.1 -p 9432 -d gpadmin -U gpadmin -f $RTSMADLIB_HOME/samples/madlib_model_demo/sql/logistic_regression.sql</i></b>
	```
	Verify that you have the model created in Greenplum; 
	```
	psql -h 127.0.0.1 -p 9432 -d gpadmin -U gpadmin -c "select * from madlib_demo.patients_logregr"
	```
	The output you see above is from the model logistic regression model table.

### PART 2: Model Deployment On SpringBoot Docker container.</h2>
In this section we deploy the model developed on Greenplum to a Docker container as REST service.
 - Edit the file $RTSMADLIB_HOME/samples/madlib_model_demo/input/logistic_regression.json and change Greenplum connection information. <br>
 - Deploy the model to docker using below command; 
   ```
	rts4madlib --name patientslrm --action deploy --type model --target docker --inputJson $RTSMADLIB_HOME/samples/madlib_model_demo/input/logistic_regression.json</i></b>
    ```
    Notice the output of the deployment for port, This is dynamically generated in a range. We need this to invoke the service.
 - Once the command finished, you can verify the container created using `docker ps` command
 - Once the container is running run below to verify the REST end point. Please change the port as per the output from step 2. 
   ```
     curl -v -H "Accept:application/json" http://localhost:8091/actuator/info
   ```
   This will return the model deployed in the container. Once this return successful code we move on to further testing.
 - Now we are ready to do predict operations on this newly deployed model. Please change the port as per the output from step 
   ``` curl -v -H "Content-Type:application/json" -X POST http://localhost:8091/predict -d '{ "treatment": 1, "trait_anxiety": 70 }'
       
       Result: [ {"logregr_predict": true, "logregr_predict_prob": 0.7202230289415188}]
			
That's it, we successfully deployed MADlib model as REST end point.

### PART 3: Undeployment 
 To un-deploy the model please run below command;
  ```
	rts4madlib --name patientslrm --action undeploy --type model --target docker
  ```
  To verify the container is removed, please run the `docker ps` command and and verify there is no madlib model container in the output.
