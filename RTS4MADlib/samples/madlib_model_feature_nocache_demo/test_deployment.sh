#!/bin/sh
# Deploying the Credit Application model to docker 

rts4madlib --name credit_app_v1 --type flow --target docker --action deploy --inputJson $RTSMADLIB_HOME/samples/madlib_model_feature_nocache/credit_approval_model_feature.json

# Test Deployment with below url. Please change the port as per your output."

curl -v -H "Content-Type:application/json" -X POST http://localhost:8494/predict -d '{"a1":"a","a2":58.67,"a3":4.46,"a4":"u","a5":"g","a6":"q","a7":"h","a8":3.04,"a9":"t","a10":"t","a11":6.0,"a12":"f","a13":"g","a14":43.0,"a15":560.0,"a16":"+"}'

# undeploy

rts4madlib --name credit_app_v1 --type flow --target docker --action undeploy