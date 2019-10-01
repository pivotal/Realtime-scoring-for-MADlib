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

USER root

RUN apt-get -qq update

RUN apt install -qq -y vim openssh-server

RUN wget https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz -O /tmp/openjdk-11.0.2_linux-x64_bin.tar.gz
RUN mkdir -p /usr/lib/jvm
RUN tar xfvz /tmp/openjdk-11.0.2_linux-x64_bin.tar.gz --directory /usr/lib/jvm
RUN ls -l /usr/lib/jvm
RUN rm -f /tmp/openjdk-11.0.2_linux-x64_bin.tar.gz
RUN sh -c 'for bin in /usr/lib/jvm/jdk-11*/bin/*; do update-alternatives --install /usr/bin/$(basename $bin) $(basename $bin) $bin 1102; done'
RUN sh -c 'for bin in /usr/lib/jvm/jdk-11*/bin/*; do update-alternatives --set $(basename $bin) $bin; done'

RUN mkdir -p /run/sshd

# Set the default command to run when starting the container
CMD ["/usr/sbin/sshd", "-D"]
