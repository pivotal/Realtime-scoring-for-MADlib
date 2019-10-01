#-----------------------------------------------------------------------------------------------
#   Copyright 2019 Pivotal Software
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#----------------------------------------------------------------------------------------------
# Author: Sridhar Paladugu 
# Email: spaladugu@pivotal.io


FROM ubuntu:16.04

RUN apt-get -qq update 

### Get necessary libraries to add postgresql apt repository
RUN apt install -qq -y wget vim lsb-core software-properties-common gdebi-core gnupg 

### necessary tools for rest apps
RUN apt-get -qq update && apt-get install -y apt-utils apt-transport-https ca-certificates

### Add postgresql apt repository
RUN add-apt-repository "deb http://apt.postgresql.org/pub/repos/apt/ $(lsb_release -sc)-pgdg main" && wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -

### Have to update after getting new repository
RUN apt-get update

### Get postgres10 and postgres specific add-ons
RUN apt-get install -y postgresql-10 \
                       postgresql-client-10 \
                       postgresql-plpython-10 \
                       postgresql-server-dev-10 \
                       libpq-dev \
                       build-essential \
                       openssl \
                       libssl-dev \
                       libboost-all-dev \
                       m4 \
                       vim \
                       pgxnclient \
                       flex \
                       bison \
                       graphviz
USER postgres

RUN    /etc/init.d/postgresql start &&\
    psql --command "CREATE USER madlibuser WITH SUPERUSER PASSWORD 'justworks!';" &&\
    createdb -O madlibuser madlibdb
    
RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/10/main/pg_hba.conf

RUN echo "listen_addresses='*'" >> /etc/postgresql/10/main/postgresql.conf &&\
	echo "fsync=off" >> /etc/postgresql/10/main/postgresql.conf &&\
	echo "synchronous_commit=off" >> /etc/postgresql/10/main/postgresql.conf &&\
	echo "full_page_writes=off" >> /etc/postgresql/10/main/postgresql.conf &&\
	echo "bgwriter_lru_maxpages=0" >> /etc/postgresql/10/main/postgresql.conf
	
EXPOSE 5432

VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

USER root

#python3.7
 RUN apt-get update
 RUN apt install -y software-properties-common
 RUN apt-get install -qq -y python3 python-pip
 RUN apt-get install  -qq -y m4

#Open JDK11
RUN wget https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz -O /tmp/openjdk-11.0.2_linux-x64_bin.tar.gz
RUN mkdir -p /usr/lib/jvm
RUN tar xfvz /tmp/openjdk-11.0.2_linux-x64_bin.tar.gz --directory /usr/lib/jvm
RUN rm -f /tmp/openjdk-11.0.2_linux-x64_bin.tar.gz
RUN sh -c 'for bin in /usr/lib/jvm/jdk-11*/bin/*; do update-alternatives --install /usr/bin/$(basename $bin) $(basename $bin) $bin 1102; done'
RUN sh -c 'for bin in /usr/lib/jvm/jdk-11*/bin/*; do update-alternatives --set $(basename $bin) $bin; done'
