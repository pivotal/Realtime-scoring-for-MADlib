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
