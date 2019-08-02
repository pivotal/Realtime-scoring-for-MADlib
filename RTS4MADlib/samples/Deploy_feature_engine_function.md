## Installing Feature generation function on Docker as REST service 
In this sample we will deploy a a feature engine function as rest services. The feature generation fucntion takes input p[ayload and enrich the payload for model input.

To get started we take a simeple function which takes a user record and categorize user based on the age in to a group. 

The feature function service has two flavors;
1. using a udf in a sql
2. using a udf as a function

In option 1 we do something like below to a user rord payload;

```
select name, age, gender, pseo.categorize_user(age) from pseo.message
```
in the above example a user defined function is used as inline sql.

in option 2 we call a user defined function directly. This option is usefule where we take input record and apply several transformations. This option requires the frame work define a user defined function which does the actual feature transformation work and a wrapper function which uses this function to create input and ouputs to/from from feature function. 

### usecase 1.

1. run the sql from file Realtime-scoring-for-MADlib/RTS4MADlib/samples/madlib_feature_engine_inline_function_demo/inline_feature_function_demo.sql

The file has below function defined.
```
create or replace function pseo.categorize_user(age int)
returns text as $$
    declare grp text;
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
end 
$$ LANGUAGE plpgsql;
```
2. Once the SQL is ran on your Greenplum server. We will deploy the feature engine on to docker. In this step we will use the manifest as below;
```
{
    "spring.profiles.active": "nocache",
    "modeldb-datasource.jdbc-url": "jdbc:postgresql://{DB_HOST}:{PORT}/{DATABASE}",
    "modeldb-datasource.userName": "{USER}",
    "modeldb-datasource.password": "{PASSWORD}",
    "feature-engine.featurename": "categorize_users",
    "feature-engine.featuredescription": "test feature function",
    "feature-engine.featuresschema": "pseo",
    "feature-engine.payloadtable": "message",
    "feature-engine.featurequery": "select name, age, gender, pseo.categorize_user(age) from pseo.message",
    "feature-engine.featureFunctions": [
        "categorize_user"
        ],
        "feature-engine.cacheenabled": "false"
}
```
The above manifest is in the file Realtime-scoring-for-MADlib/RTS4MADlib/samples/madlib_feature_engine_inline_function_demo/inline_feature_function_demo.json

Using the below command we will deploy this feature engine function;

```
rts4madlib --name credit_app_v1 --type feature-engine --target docker --action deploy --inputJson inline_feature_function_demo.json
```

Once the docker container is up we test this as below;

```
curl -v -H "Content-Type:application/json" http://localhost:8198/features/calculate -d '{"name":"oms","age":45,"gender":"m"}'
```

This produces the below output;
```
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8198 (#0)
> POST /features/calculate HTTP/1.1
> Host: localhost:8198
> User-Agent: curl/7.63.0
> Accept: */*
> Content-Type:application/json
> Content-Length: 36
>
* upload completely sent off: 36 out of 36 bytes
< HTTP/1.1 200
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Fri, 02 Aug 2019 02:19:38 GMT
<
* Connection #0 to host localhost left intact
[{"gender":"m","name":"oms","categorize_user":"G3","age":45}]
```


### usecase 2.

1. run the sql from file Realtime-scoring-for-MADlib/RTS4MADlib/samples/madlib_feature_engine_function_demo/feature_function_demo.sql
This script creates below functions;
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


create or replace function pseo.categorize_users_feature_driver
() 
returns void as $$
declare 
uname text;
uage int;
ugender text;
begin
select i.name, i.age, i.gender
into uname
, uage, ugender from pseo.message i;

create table pseo.out_message as
select *
from pseo.categorize_users(uname, uage, ugender) as rec(name text, age int, gender text, grp text);
end;
$$ language plpgsql;
```
As you see above the driver function is responsible to apply transformations to input. In this sample we do not see much value but this is the place where you can customize the input and putputs 

Also please make note about message table, this is the table that the incoming payload will be saved on rest client. Thsi name is paramterized on that deployment so this function and the payload table names should match.

2. Deploy using the below command;
    ```
   rts4madlib --name credit_app_v1 --type feature-engine --target docker --action deploy --inputJson feature_function_demo.json
   ```
   Once the deployment is finished we can test as;
   ```
   curl -v -H "Content-Type:application/json" http://localhost:8086/features/calculate -d '{"name":"oms","age":45,"gender":"m"}'
   ```
   This call will result in below output;
   
   ```
   {
    "gender": "m",
    "grp": "G3",
    "name": "oms",
    "age": 45
   }

   ```
   
