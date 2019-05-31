# Realtime-scoring-for-MADlib
Operationalize AI/ML models built using Apache MADlib and Postgres PL/Python.

RTS-For-MADlib enables data scientists to deploy machine learning workflows built using Apache MADlib on Greenplum(postgres) as REST service. 
RTS-For-MADlib provides a mechanism to deploy models seemlessley to scalable container management systems like Pivotal Container Services (PKS), Google Kubernetes Engine (GKE) and similar container based systems.

RTS-For-MADlib enables the deployment of a AI/ML model as a workflow with components;

- light weight feature engine, which transsforms the incoming payload to model input
- Apache MADlib or PL/python model component
- An optional cache component for feature lookup
- An orchestrator component.

# Build
RT4MADlib is based on docker containers. The package contain below components;
- MLModelservice - MADlibModel application
- FeatureEngine - Transformer for payload to featureset
- FeatureCacheManager - An optional feature cache for lookup 
- MLModelflow - An orchestrator component
- DockerContainers - Base Docker image(s) with java, python, MADlib, Postgres
- RTS4MADlib - A client application that deploys the ML pipeline to PKS or GKE, etc.

The project provides prebuilt containers to download from Pivotal Data Docker repositories. But if you wish to build and deploy containers
to your organization private docker repo, we provide the scripts to build containers in build folder.
In order to build and upload docker images to registry you need to run the below command and provide the credentials when prompted.
 ` docker login $registry_server ` 
After successfully login to registry we will start the build process.
###Steps:

 - Build base containers, the below build 2 containers, jdk 11 container and Postgres96 with MADlib and pl/python. The containers will be tagged and uploaded to the registry specified.
    ```
     $ cd DockerContainers
     $ ./buildbaseimages.sh --registry $docker_repo 
    ``` 

 - Build the rest of the project containers. This step build containers for MLModelflow, FeatureEngine, FeatureCacheManager and MLModelflow      Spring boot applications, and uploads them to specified docker registry. Apart from Docker containers this step also build the client deployment command line tool. All the jar files for Spring boot applications and a RT4MADlib client tool tar files will be copied on to $project_root/dist folder.
   ```
    $ cd $project_root/build 
    $ ./build all $docker_repo 
   ``` 

If you wish to build individual containers then there are scripts available in $project_root/build folder. For example;
    ```
      $ cd $project_root 
      $ ./build_mlmodelservice.sh $docker_repo
      $ ./build_featurescachemanager.sh $docker_repo
      $ ./build_featuresengine.sh $docker_repo
      $ ./build_mlmodelflow.sh $docker_repo 
    ```
# Deploy

# Usage
    
# License

https://opensource.org/licenses/MIT
