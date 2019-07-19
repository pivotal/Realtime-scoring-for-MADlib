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


FROM ubuntu

RUN apt-get -qq update 

RUN apt install -qq -y wget vim gdebi-core gnupg 

RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys B97B0AFCAA1A47F044F244A07FCC7D46ACCC4CF8

RUN echo "deb http://apt.postgresql.org/pub/repos/apt/ precise-pgdg main" > /etc/apt/sources.list.d/pgdg.list

RUN apt-get -qq update && apt-get install -y apt-utils apt-transport-https ca-certificates

RUN apt-get -y -qq install software-properties-common &&\
		apt-get -y -qq install postgresql-9.6 postgresql-client-9.6 postgresql-contrib-9.6 postgresql-plpython-9.6

USER postgres

RUN    /etc/init.d/postgresql start &&\
    psql --command "CREATE USER madlibuser WITH SUPERUSER PASSWORD 'justworks!';" &&\
    createdb -O madlibuser madlibdb
    
RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/9.6/main/pg_hba.conf

RUN echo "listen_addresses='*'" >> /etc/postgresql/9.6/main/postgresql.conf &&\
	echo "fsync=off" >> /etc/postgresql/9.6/main/postgresql.conf &&\
	echo "synchronous_commit=off" >> /etc/postgresql/9.6/main/postgresql.conf &&\
	echo "full_page_writes=off" >> /etc/postgresql/9.6/main/postgresql.conf &&\
	echo "bgwriter_lru_maxpages=0" >> /etc/postgresql/9.6/main/postgresql.conf
	
EXPOSE 5432

VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

USER root

#python2.7
RUN apt-get update
RUN apt install -y software-properties-common
RUN add-apt-repository ppa:deadsnakes/ppa
RUN apt-get install -qq -y python3.7 python-pip 
RUN apt-get install  -qq -y m4

#Open JDK11
RUN wget https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz -O /tmp/openjdk-11.0.2_linux-x64_bin.tar.gz
RUN mkdir -p /usr/lib/jvm
RUN tar xfvz /tmp/openjdk-11.0.2_linux-x64_bin.tar.gz --directory /usr/lib/jvm
RUN rm -f /tmp/openjdk-11.0.2_linux-x64_bin.tar.gz
RUN sh -c 'for bin in /usr/lib/jvm/jdk-11*/bin/*; do update-alternatives --install /usr/bin/$(basename $bin) $(basename $bin) $bin 1102; done'
RUN sh -c 'for bin in /usr/lib/jvm/jdk-11*/bin/*; do update-alternatives --set $(basename $bin) $bin; done'
