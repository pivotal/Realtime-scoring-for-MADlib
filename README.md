# Realtime-scoring-for-MADlib
Operationalize AI/ML models built using Apache MADlib and Postgres PL/Python.

RTS-For-MADlib enables data scientists to deploy machine learning workflows built using Apache MADlib on Greenplum(postgres) as REST service. 
RTS-For-MADlib provides a mechanism to deploy models seemlessley to scalable container management systems like Pivotal Container Services (PKS), Google Kubernetes Engine (GKE) and similar container based systems.

RTS-For-MADlib enables the deployment of a AI/ML model as a workflow with components;

- light weight feature engine, which transsforms the incoming payload to model input
- Apache MADlib or PL/python model component
- An optional cache component for feature lookup
- An orchestrator component.


# License

https://opensource.org/licenses/MIT
