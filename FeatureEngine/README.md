**MADlib FEATURES**

MADlibFearuteService is a containerized deployment of a MADlib feature engine.  If the model that is deployed using MADlibRESTService, and need to generate feature inputs dynamically then we need to deploy this container. This service is exposes the REST end point which passes a JSON document of feature creation request. The service also can leverage the Cache for features lookup.

The Spring boot application should be manifested with below attributes ;

```
{
	"spring.profiles.active": "redis",
	"redis": {
		"clustertype": "standlone",
		"hostname": "HOST",
		"port": PORT
	},
	"app.featurename": "FEATURE_ENGINE_NAME",
	"app.featuredescription": "FEATURE_ENGINE_DESCRIPTION",
	"app.featuresschema": "SOURCE_SCHEMA_NAME",
    "featureFunctions":
        - list of functions, line item for each function
	"app.featurequery": "FEATURE_ENGINE_QUERY",
	"app.cacheenabled": "true",
	"app.cacheentities": {
		"CACHE_ENTITY": "KEY",
		.........
	}
}
```
The above run time configuration instrumenting the spring boot application that it need to lookup cache and join cache for runtime evaluation. Also telling that the cache is of redis standalone configuration. The below configuration is format
re of Redis Sentinel;

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
			port:
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
If there is no need of cache and we just need to apply transformations to payload then we instrument that  "app.cacheenabled": "false".

### Building the code

The application is provided as bundled and available in docker registry. The Spring boot application can be build using maven.
So to build this project please run 

``` $mvn clean install package ```


### usage sample:
####  Feature generation using a pl/function. 
The below example shows a sample feature service which enrich the payload. This specific sample also demostrte the use of pl/function usage.

```
logging:
file: MADlibFeatureService.log
level:
org.springframework: ERROR
io.pivotal: DEBUG 

management:
endpoints:
web:
exposure:
include: "*"    
server:
port: 9196          

spring:
application:
name:  Madlib Features engine Application

#no cache lookup is needed for this feature service
profiles:
active:
- nocache

jpa:
properties:
hibernate:
temp:
use_jdbc_metadata_defaults: false
database-platform: org.hibernate.dialect.PostgreSQL9Dialect

# Database properties
modeldb-datasource:
jdbc-url: jdbc:postgresql://127.0.0.1:9432/test
username: gpadmin
password: pivotal
driverClassName: org.postgresql.Driver 

containerdb-datasource:
jdbc-url: jdbc:postgresql://127.0.0.1:7432/madlibdb?stringtype=unspecified
username: madlibuser
password: justworks!
driverClassName: org.postgresql.Driver
leak-detection-threshold: 3000    

---
feature-engine:
featurename: test
featuredescription: test description
featuresschema: pseo
payloadtable: message
featurequery: select pseo.categorize_users_feature_driver()
featureFunctions:
- categorize_users
- categorize_users_feature_driver
cacheenabled: false

```
In the above manifest file we specified a source database connection which is a model repository database and the container's db engine.
We also specified the pl functions that we are using in the feature transformation. The functions are specified as list under "featurefunctions".
Please take a note that we have two functions, one that does the actual processing and the other function ending with "driver" does the
input creation to the feature function and output creation from feature function. 

The below feature function takes a user data and categorize user in a specific group. I kept the function very simple to show the usage.
```
create or replace function pseo.categorize_users(name text, age int, gender text)
returns setof RECORD as $$
declare 
    rec RECORD;
BEGIN
    select name,  age ,  gender,
        case 
            when age > 50  then 'G4' 
            when age > 40  then 'G3' 
            when age > 30  then 'G3'
            when age > 18  then 'G2'
            when age > 0  then 'G1'
            end as grp into rec;
    return next rec;
end $$ LANGUAGE plpgsql;
```
The framework requires the feature function should have a driver function that should contain the logic to supply the feature input and create output.
Below is the sample. 
```
create or replace function pseo.categorize_users_feature_driver() 
returns void as $$
declare 
    uname text;
    uage int;
    ugender text;
begin
    select i.name, i.age, i.gender  into uname, uage, ugender from pseo.message i;
    
    create table pseo.out_message as 
        select * from pseo.categorize_users(uname, uage, ugender) as rec(name text, age int, gender text, grp text);      
end;
$$ language plpgsql;
```

There are two things happening here. First the we read the input data needed for feature transformation from a table called "pseo.message".
This the table that get created automatically on the container when a user request comes to feature engine. The feature engine store the user payload in this table.
Secondly, we create the "out_message" table based on the query results from the actual feature generation function.

This is the framework we follow for feature generation using pl/function. This function can be any pl function.

The "feature query" shows that we are telling the container to run this query to invoke the feature transformation function. 

To test and deploy the feature service save above manifest as user-group-features.json in current folder;
``` rts4madlib --name group-users-v1 --type featureengine --target docker --action deploy --inputJson user-group-features.json ```

once it is deployed test this by

``` curl http://localhost:8086/features/calculate -d '{"name":"oms","age":45,"gender":"m"}' ```

would return results as 
```
{
"gender": "m",
"grp": "G3",
"name": "oms",
"age": 45
}
```

####  Feature generation using a pl/function in line SQL. 
In this set of example we use custom PLPGSQL function in SQL. The below is the manifest file for the deployment
```
logging:
file: MADlibFeatureService.log
level:
org.springframework: ERROR
io.pivotal: DEBUG 

management:
endpoints:
web:
exposure:
include: "*"    
server:
port: 9196          

spring:
application:
name:  Madlib Features engine Application

#no cache lookup is needed for this feature service
profiles:
active:
- nocache

jpa:
properties:
hibernate:
temp:
use_jdbc_metadata_defaults: false
database-platform: org.hibernate.dialect.PostgreSQL9Dialect

# Database properties
modeldb-datasource:
jdbc-url: jdbc:postgresql://127.0.0.1:9432/test
username: gpadmin
password: pivotal
driverClassName: org.postgresql.Driver 

containerdb-datasource:
jdbc-url: jdbc:postgresql://127.0.0.1:7432/madlibdb?stringtype=unspecified
username: madlibuser
password: justworks!
driverClassName: org.postgresql.Driver
leak-detection-threshold: 3000    

---
feature-engine:
featurename: test
featuredescription: test description
featuresschema: pseo
payloadtable: message
featurequery: select name, age, gender, pseo.categorize_user(age) from pseo.message
featureFunctions:
- categorize_user
cacheenabled: false

```
In this sample we mention the function to import in to container and SQL. The function that is on source database is 
```
create or replace function pseo.categorize_user(age int)
returns text as $$
declare 
grp text;
BEGIN
select 
case 
when age > 50  then 'G4' 
when age > 40  then 'G3' 
when age > 30  then 'G3'
when age > 18  then 'G2'
when age > 0  then 'G1'
end into grp;
return grp;
end $$ LANGUAGE plpgsql;
```
To test and deploy the feature service save above manifest as user-group-features.json in current folder;
``` rts4madlib --name group-users-v1 --type featureengine --target docker --action deploy --inputJson user-group-features.json ```

once it is deployed test this by

``` curl http://localhost:8086/features/calculate -d '{"name":"oms","age":45,"gender":"m"}' ```

would return results as 
```
{
"gender": "m",
"grp": "G3",
"name": "oms",
"age": 45
}
```
    
####  Feature generation using just SQL.

In this case there is no function specified in the manifest, just only SQL.

####  Feature generation that involves Cache.
In this case we provide option to plug-in Cache for looking up feature data from external cache. This case we provide details about cache provider and what need to be looked up in cache.
For a detailed sample on this advance case, please refer to ```Realtime-scoring-for-MADlib/RTS4MADlib/samples/madlib-model_feature_cache_demo```


