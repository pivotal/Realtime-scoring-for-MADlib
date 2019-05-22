#-----------------------------------------------------------------------------------------------
# MIT License

# Copyright (c) 2019 Pivotal

# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:

# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#----------------------------------------------------------------------------------------------

# Author: Sridhar Paladugu 
# Email: spaladugu@pivotal.io

ARG docker_registry=

FROM $docker_registry/rts4madlib-jdk:1.0

RUN mkdir -p /opt/pivotal/rts-for-madlib
RUN chmod ugo+rwx /opt/pivotal/rts-for-madlib/
ADD ./target/MLModelflow-1.0.0-SNAPSHOT.jar /opt/pivotal/rts-for-madlib/MLModelflow-1.0.0-SNAPSHOT.jar
RUN chmod ugo+rx /opt/pivotal/rts-for-madlib/MLModelflow-1.0.0-SNAPSHOT.jar

EXPOSE 8089

RUN mkdir /docker-entrypoint-initdb.d
ADD ./src/main/resources/init_container.sh /docker-entrypoint-initdb.d/
ADD ./src/main/resources/init_container_docker.sh /docker-entrypoint-initdb.d/
RUN chmod 755 /docker-entrypoint-initdb.d/init_container.sh
RUN chmod 755 /docker-entrypoint-initdb.d/init_container_docker.sh

WORKDIR /opt/pivotal/rts-for-madlib

# Set the default command to run when starting the container
CMD ["/usr/sbin/sshd", "-D"]
