## Installing a  model and feature engine on minikube
In this sample let us explore how to deploy model and feature engine containers on minikube.
### PART 1: Model Development On Greenplum cluster
- In order to run the demo locally, we need a minikube environment. If you do not have minikube running locally, please setup minikube by following instructions on [Install minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/)
Once you have minikube installed as per above link start minikube as below;
 - ```minikube start --cpus 4 --memory 8192``` -- start minikube
  - ```eval $(minikube docker-env)``` -- This step is needed only if you already had docker on your machine.
  - ```minikube dashboard``` -- open minikube dashboard by running
  - ```minikue ip``` -- take a nopy of the minikube ip address, we need this for subsequent stpes.  

- Next step is to have a running Greenplum environment.Please run the below script to setup a Greenplum instance on minikube's docker.
```
$RTSMADLIB_HOME/bin/setup_greenplum_docker
```
This script will download the image and starts the container, depending on your network speed the download take few minutes. -- Verify Greenplum cluster by running the below command; when prompted use pivotal for password. Please use minikube ip address noted in above step.
```
psql -h <MINIKUBE_IP> -p 9432 -d gpadmin -U gpadmin -c "\dn"
```
This should print list of schemas
- Once we verify the greenplum and MADlib, we are ready to install sample data and develop a model on greenplum. Please run below from command line to create a sample data and train a model; 
```
psql -h 127.0.0.1 -p 9432 -d gpadmin -U gpadmin -f $RTSMADLIB_HOME/samples/madlib_model_feature_nocache/credit_application_schema.sql
```

### PART2: Model Deployment

In this section we deploy the model developed on Greenplum to a minikube container as REST service.

- Edit the file `$RTSMADLIB_HOME/samples/madlib_model_feature_nocache/credit_approval_model_feature.json` and change Greenplum connection information. 

- Deploy the model to docker using below command; 
```
rts4madlib --name credit-app-v1 --type flow --target kubernetes --action deploy --inputJson $RTSMADLIB_HOME/samples/madlib_model_feature_nocache/credit_approval_model_feature.json
```
Once the command finished please run; then you should see 3 contianers model,feature engine, flow orchestrator as below;
```kubectl get all
NAME                                                               READY     STATUS    RESTARTS   AGE
pod/credit-app-v1-rts-for-madlib-featuresengine-79d95686ff-q6tm2   1/1       Running   0          3m
pod/credit-app-v1-rts-for-madlib-mlmodelflow-5fd55fdb47-tm77c      1/1       Running   0          3m
pod/credit-app-v1-rts-for-madlib-model-856876668-dchnr             1/1       Running   0          3m

NAME                                                  TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)                         AGE
service/credit-app-v1-rts-for-madlib-featuresengine   LoadBalancer   10.111.67.66     <pending>     8086:32243/TCP                  3m
service/credit-app-v1-rts-for-madlib-mlmodelflow      LoadBalancer   10.106.123.232   <pending>     8089:31224/TCP                  3m
service/credit-app-v1-rts-for-madlib-model            LoadBalancer   10.100.123.246   <pending>     8085:31168/TCP,5432:30794/TCP   3m
service/kubernetes                                    ClusterIP      10.96.0.1        <none>        443/TCP                         190d

NAME                                                          DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/credit-app-v1-rts-for-madlib-featuresengine   1         1         1            1           3m
deployment.apps/credit-app-v1-rts-for-madlib-mlmodelflow      1         1         1            1           3m
deployment.apps/credit-app-v1-rts-for-madlib-model            1         1         1            1           3m

NAME                                                                     DESIRED   CURRENT   READY     AGE
replicaset.apps/credit-app-v1-rts-for-madlib-featuresengine-79d95686ff   1         1         1         3m
replicaset.apps/credit-app-v1-rts-for-madlib-mlmodelflow-5fd55fdb47      1         1         1         3m
replicaset.apps/credit-app-v1-rts-for-madlib-model-856876668             1         1         1         3m
```
Please note above we have 3 services exposed and their ip addresses. Since this is minikue environment and no load balancer we do not have any external ip addresses allocated. we get to these services using minikube ip that we collected in step 1.

Now to test the service

```curl -v -H "Content-Type:application/json" -X POST http://192.168.99.100:31224/predict -d '{"a1":"a","a2":58.67,"a3":4.46,"a4":"u","a5":"g","a6":"q","a7":"h","a8":3.04,"a9":"t","a10":"t","a11":6,"a12":"f","a13":"g","a14":43,"a15":560,"a16":"+"}'
```
We should get below result;

```
[
{
"estimated_prob_1": 0.9,
"estimated_prob_0": 0.1
}

```
