<html>
<head>
<title>Deploy Random Forest Model</title>
</head>
<body>
	<h1>Installing Apache MADlib model on Docker as REST service</h1>
	In this sample let us explore how to deploy model involved with
	multiple base table and customizable return results.
	<div>
		<h2>PART 1: Model Development On Greenplum cluster</h2>

		<ol>
			<li>In order to run the demo locally, we need a docker
				environment. If you do not have docker running locally, please setup
				docker by following instructions on <a
				href="https://www.docker.com/get-started">Docker Getting started</a>
				<br>
			</li>
			<li>Next step is to have a running Greenplum environment.Please
				run the below script to setup a Greenplum instance on docker; <br>
				<b><i>$RTSMADLIB_HOME/bin/setup_greenplum_docker</i></b> <br>This
				script will download the image and starts the container, depending
				on your network speed the download take few minutes. <br>
			</li>
			<li>Verify Greenplum cluster by running the below command; when
				prompted use pivotal for password. <br> <b><i>psql -h
						127.0.0.1 -p 9432 -d gpadmin -U gpadmin -c "\dn"</i></b> <br>This
				should print list of schemas. <br>
			</li>

			<li>Once we verify the greenplum and MADlib, we are ready to
				install sample data and develop a model on greenplum. Please run
				below from command line to create a sample data and train a model; <br>
				<b><i>psql -h 127.0.0.1 -p 9432 -d gpadmin -U gpadmin -f
						$RTSMADLIB_HOME/samples/madlib_model_demo/sql/random_forest.sql</i></b> <br>
				Verify that you have the model created in Greenplum; <br> <b><i>psql
						-h 127.0.0.1 -p 9432 -d gpadmin -U gpadmin -c "select * from
						madlib_demo.rf_train_output_summary"</i></b> <br>The output you see
				above is from the model table.
			</li>
		</ol>
	</div>
	<div>
		<h2>PART 2: Model Deployment On SpringBoot Docker container.</h2>
		In this section we deploy the model developed on Greenplum to a Docker
		container as REST service.
		<ol>
			<li>Edit the file <b><i>$RTSMADLIB_HOME/samples/madlib_model_demo/input/random_forest.json</i></b>
				and change Greenplum connection information. <br>
			</li>
			<li>Deploy the model to docker using below command; <br> <b><i>rts4madlib
						--name golfrfm --action deploy --type model --target docker
						--inputJson
						$RTSMADLIB_HOME/samples/madlib_model_demo/input/random_forest.json</i></b>
				<br>Notice the output of the deployment for port, This is
				dynamically generated in a range. We need this to invoke the
				service.
			</li>
			<li>Once the command finished, verify by running <b><i>docker ps</i></b> 
			</li>
			<li>Once the container is running run below to verify the REST
				end point. Please change the port as per the output from step 2. <br>
				<b><i>curl -v -H "Accept:application/json"
						http://localhost:8095/actuator/info</i></b> 
				<br>This will return the model deployed in the container. 
			</li>
			<li>Now we are ready to do predict operations on this newly deployed model. Please change the port as per the output from step <br>
				<b><i>curl -v -H "Content-Type:application/json" -X POST http://localhost:8095/predict -d '{"id":2,"OUTLOOK":"overcast","temperature":64.00,"humidity":65.00,"windy":true,"class":"Play"}'</i></b> 
						<br> <i>Result: 
					[{"id":2,"class":"Play","estimated_prob_Don't Play":0.6,"estimated_prob_Play":0.4}] </i>
			</li>
		</ol>
		<br>That's it, we successfully deployed MADlib model as REST end
		point.
	</div>
</body>
</html>