# Author: Sridhar Paladugu 
# Email: spaladugu@pivotal.io

ARG docker_registry=

FROM $docker_registry/rts4madlib-pgjava:1.0

USER root
RUN wget -qq https://archive.apache.org/dist/madlib/1.15.1/apache-madlib-1.15.1-bin-Linux.deb -O apache-madlib-1.15.1-bin-Linux.deb
RUN gdebi --n apache-madlib-1.15.1-bin-Linux.deb
RUN rm -f apache-madlib-1.15.1-bin-Linux.deb

RUN mkdir -p /opt/pivotal/rts-for-madlib
RUN chmod ugo+rwx /opt/pivotal/rts-for-madlib/
ADD ./target/FeatureEngine-1.0.0-SNAPSHOT.jar /opt/pivotal/rts-for-madlib/FeatureEngine-1.0.0-SNAPSHOT.jar
RUN chmod ugo+rx /opt/pivotal/rts-for-madlib/FeatureEngine-1.0.0-SNAPSHOT.jar
RUN mkdir /docker-entrypoint-initdb.d
ADD ./src/main/resources/init_container.sh /docker-entrypoint-initdb.d/
ADD ./src/main/resources/init_container_docker.sh /docker-entrypoint-initdb.d/

WORKDIR /opt/pivotal/rts-for-madlib

EXPOSE  8086

USER postgres

# Set the default command to run when starting the container
CMD ["/usr/lib/postgresql/9.6/bin/postgres", "-D", "/var/lib/postgresql/9.6/main", "-c", "config_file=/etc/postgresql/9.6/main/postgresql.conf"]
