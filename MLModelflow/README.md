**MADlibflow**

MADlibflow is a containerized deployment of a MADlib model workflow component.  
This is basically an orchestration module which invokes the features container
and invoke the model container by passing the output from feature engine. This
automates the process of feature engineering and model prediction in to single
operation for end client. This is deployed when we need feature engine and model
as part of single operation.

The application runtime JSON for building the flow is ;

app:
  feature-engine-endpoint:
  model-endpoint:  

Madlibflow client will inject these values runtime when we are deploying the flow.

### Building the code

The application is provided as bundled and available in docker registry.
The Spring boot application can be build using maven.
So to build this project please run ``` $mvn clean install package ```
 
