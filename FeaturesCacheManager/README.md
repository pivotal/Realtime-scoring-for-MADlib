** FeaturesCacheManager**

FeaturesCacheManager is a containerized deployment of a MADlib feature loader.  This module is responsible for loading the features from the Greenplum/Postgres to lookup Cache during feature engineering. This loads the cache from the a database table and refreshes the cache using parallel operation. This module also exposes a REST endpoint to refresh the cache whenever there is a need for updating the feature set. This is most important part in close loop analytics where features need to regenerated as and when the need lands in the database.

The Spring boot application should be manifested with JSON as command line attributes;
```
 {
  "spring.profiles.active": "{redis|gemfire|pcc}",
  "redis": {
    "clustertype": "standlone",
    "hostname": "192.168.1.66",
    "port": 6379
  },
  "modeldb-datasource": {
    "jdbc-url": "jdbc:postgresql://HOST:PORT/DATABASE",
    "username": "LOGIN",
    "password": "PASSWORD"
  },
  "app": {
    "featurename": FEATURECACHE_NAME",
    "featuresourceschema": "SOURCE_SCHEMA_NAME",
    "featurefunctions": [
      "PgPsql_FUNCTION1",
      .......
      "PgPsql_FUNCTIONn"
    ],
    "featuresourcetables": {
      "SOURCE_TABLE_NAME": "UNIQUE_ID_COLUMN",
      .........
    }
  }
```
The above run time configuration instrumenting the spring boot application that it need to lookup cache and join cache for runtime evaluation. Also telling that the cache is of redis standalone configuration. The below configuration is format
of Redis Sentinel

```
  redis:
    clustertype: ha
      master: "masternode"
  	  servers:
  		  - host:port
        - host:PORT
          ........
```          
If we use gemfire then the configuration is;
```
  gemfire:
    locator:
    port: 1
    username:
    password:
```
If we choose "Pivotal Cloud Cache" then ;
```
  pcc:
    locators:
    port: 1
    username:
    password:  
```

The spring boot application on start loads the cache with specified data and also exposes a REST end point for loading features on demand "/features/load". This uses the "featurefunctions" mentioned in the application yaml. The application
invokes those functions and pull data from tables after the execution in single transaction. This end point can be configured to run periodically at scheduled
intervals in closed loop analytics usecases.


### Building the code

The application is provided as bundled and available in docker registry. The Spring boot application can be build using maven.
So to build this project please run 

``` $mvn clean install package ```
