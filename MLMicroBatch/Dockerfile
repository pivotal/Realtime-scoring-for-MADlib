#-------------------------------------------------------------------------------
# Copyright 2019 Pivotal Software Inc
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
ARG docker_registry=
ARG base_image_tag=
FROM $docker_registry/rts4madlib-jdk:$base_image_tag

RUN mkdir -p /opt/pivotal/rts-for-madlib
RUN chmod ugo+rwx /opt/pivotal/rts-for-madlib/

ADD ./target/MLMicroBatch-1.0.0-SNAPSHOT.jar /opt/pivotal/rts-for-madlib/MLMicroBatch-1.0.0-SNAPSHOT.jar
RUN chmod ugo+rx /opt/pivotal/rts-for-madlib/MLMicroBatch-1.0.0-SNAPSHOT.jar

RUN mkdir /docker-entrypoint-initdb.d
ADD ./src/main/resources/init_container.sh /docker-entrypoint-initdb.d/
ADD ./src/main/resources/init_container_docker.sh /docker-entrypoint-initdb.d/

EXPOSE 8088

WORKDIR /opt/pivotal/rts-for-madlib

# Set the default command to run when starting the container
CMD ["/usr/sbin/sshd", "-D"]

