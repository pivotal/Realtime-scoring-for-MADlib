FROM ubuntu:16.04

RUN apt-get -qq update

### Get necessary libraries to add postgresql apt repository
RUN apt install -qq -y wget vim lsb-core software-properties-common gdebi-core gnupg

### necessary tools for rest apps
RUN apt-get -qq update && apt-get install -y apt-utils apt-transport-https ca-certificates

### Install python 3.7
RUN add-apt-repository ppa:deadsnakes/ppa
RUN apt update && apt install -qq -y python3.7
RUN rm /usr/bin/python3 && ln -s /usr/bin/python3.7 /usr/bin/python3
RUN apt-get install -qq -y python3-pip python3-setuptools
RUN apt-get install -y python3.7-dev

### Add postgresql apt repository
RUN add-apt-repository "deb http://apt.postgresql.org/pub/repos/apt/ $(lsb_release -sc)-pgdg main" && wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -

### Have to update after getting new repository
RUN apt-get update

### Get postgres10 and postgres specific add-ons
RUN apt-get install -y libpq-dev \
                       build-essential \
                       openssl \
                       libssl-dev \
                       libboost-all-dev \
                       m4 \
                       vim \
                       pgxnclient \
                       flex \
                       bison \
                       graphviz \
                       apt_pkg \
                           sudo

### Install the postgres10.10 from sources with python3.7
RUN wget https://ftp.postgresql.org/pub/source/v10.10/postgresql-10.10.tar.bz2 -O /tmp/postgresql.tar.bz2

RUN mkdir -p /usr/src/postgresql \
   && tar --extract --file /tmp/postgresql.tar.bz2 --directory /usr/src/postgresql --strip-components 1 \
   && rm /tmp/postgresql.tar.bz2

RUN cd /usr/src/postgresql &&./configure PYTHON=/usr/bin/python --with-python \
        && make shared_libpython=yes \
        && make shared_libpython=yes install

RUN adduser --disabled-password --gecos "" postgres

RUN echo "export LD_LIBRARY_PATH=/usr/local/pgsql/lib:$LD_LIBRARY_PATH" >> /home/postgres/.bashrc

RUN echo "export PATH=$PATH:/usr/local/pgsql/bin" >> /home/postgres/.bashrc

RUN chown -R postgres:postgres /usr/local/pgsql

### CONFIGURE POSTGRES

USER postgres

RUN /usr/local/pgsql/bin/initdb -D /usr/local/pgsql/data

RUN echo "host all  all    0.0.0.0/0  md5" >> /usr/local/pgsql/data/pg_hba.conf

RUN echo "listen_addresses='*'" >> /usr/local/pgsql/data/postgresql.conf &&\
        echo "fsync=off" >> /usr/local/pgsql/data/postgresql.conf &&\
        echo "synchronous_commit=off" >> /usr/local/pgsql/data/postgresql.conf &&\
        echo "full_page_writes=off" >> /usr/local/pgsql/data/postgresql.conf &&\
        echo "bgwriter_lru_maxpages=0" >> /usr/local/pgsql/data/postgresql.conf

EXPOSE 5432

RUN /usr/local/pgsql/bin/pg_ctl -D /usr/local/pgsql/data start && \
    /usr/local/pgsql/bin/psql --command "CREATE USER madlibuser WITH SUPERUSER PASSWORD 'justworks!';" &&\
    /usr/local/pgsql/bin/createdb -O madlibuser madlibdb

### REBUILD postgres with python3.7
RUN cd /usr/src/postgresql && make clean && make distclean

RUN cd /usr/src/postgresql && ./configure PYTHON=/usr/bin/python3 --with-python \
        && make shared_libpython=yes \
        && make shared_libpython=yes install

### create a working dir and copy python package setup tooling

USER root
RUN mkdir -p /opt/pivotal/plpy-model
RUN chmod ugo+rwx /opt/pivotal/plpy-model/
RUN usermod -a -G sudo postgres
RUN echo "postgres      ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers
ADD init_container.sh /opt/pivotal/rts-for-madlib/init_container.sh
RUN chown postgres:postgres /opt/pivotal/plpy-model/init_container.sh
ADD init_container_docker.sh /opt/pivotal/plpy-model/init_container_docker.sh
RUN chown postgres:postgres /opt/pivotal/plpy-model/init_container_docker.sh


#Open JDK11
RUN wget https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz -O /tmp/openjdk-11.0.2_linux-x64_bin.tar.gz
RUN mkdir -p /usr/lib/jvm
RUN tar xfvz /tmp/openjdk-11.0.2_linux-x64_bin.tar.gz --directory /usr/lib/jvm
RUN rm -f /tmp/openjdk-11.0.2_linux-x64_bin.tar.gz
RUN sh -c 'for bin in /usr/lib/jvm/jdk-11*/bin/*; do update-alternatives --install /usr/bin/$(basename $bin) $(basename $bin) $bin 1102; done'
RUN sh -c 'for bin in /usr/lib/jvm/jdk-11*/bin/*; do update-alternatives --set $(basename $bin) $bin; done'


WORKDIR /opt/pivotal/plpy-model

USER postgres
# Set the default command to run when starting the container
CMD ["/usr/local/pgsql/bin/postgres", "-D", "/usr/local/pgsql/data", "-c", "config_file=/usr/local/pgsql/data/postgresql.conf"]
