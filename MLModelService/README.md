 <img src="https://github.com/Pivotal-Data-Engineering/MADlibFlow/blob/master/images/madlibrest.png" alt="drawing" width="80" />     **MADlib REST** - Containerized deployment of [Apache MADlib](https://madlib.apache.org/) Big Data Machine Learning model for low latency event driven scoring.

Benefits

* Easy to deploy
* Pre configured
* Light weight
* Low latency scoring/predictions
* No model conversion to Java/scala/python for streaming/low-latency usecases.
* Develop model and deploy same model in Batch and Realtime engines.

### Implementation Details

The current solution contains two primary components packaged into a Docker container:

1. Optimized Postgres instance. The postgres instance is stateless and not designed for recovery.
2. MADlib 1.5.1 with python 2.7. --
3. OpenJDK 1.8
4. Custom Spring boot application(Spring boot 2.0.6.RELEASE)

##Future:
  1. support multiple versions of MADlib and Python
  2. bundle plpython or plr.

The spring boot application reads the below startup parameters from configuration.
1. Greenplum Connection information -- source Greenplum connection information
2. GreenplumModel Schema name -- schema name where the model(s) will be downloaded
3. Greenplum model table(s) name -- list of model table name(s)
4. Greenplum data table name. -- model input table name.
5. model operation query -- The MADLib qeury to run to do prediction
6. Result Table -- If specified return data from this table.
7. Results Query -- If specified return data from this query.


When the container is launched there things happen behind the scene;
1. install madlib package(pre packaged in the container)
2. import schema and data for model table(s).
3. import schema only for the data table.

Upon successful launch of container the REST end point is enabled to run prediction.


### Building the code

The application is provided as bundled and available in docker registry. The Spring boot application can be build using maven.
So to build this project please run ``` $mvn clean install package ```
