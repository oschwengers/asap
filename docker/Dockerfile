FROM ubuntu:16.04

LABEL version="1.3.0"
LABEL maintainer="andreas.hoek@bio.uni-giessen.de"
LABEL maintainer="oliver.schwengers@computational.bio.uni-giessen.de"

RUN apt-get -y update && apt-get -y install \
    wget \
    gnuplot-nox \
    less \
    libdatetime-perl \
    libxml-simple-perl \
    libdigest-md5-perl \
    bioperl \
    libtbb2 \
    openjdk-8-jdk \
    python3 \
    python3-setuptools \
    python3-pip \
    roary

RUN wget -O- http://neuro.debian.net/lists/xenial.de-md.libre | tee /etc/apt/sources.list.d/neurodebian.sources.list
RUN apt-key adv --recv-keys --keyserver hkp://pool.sks-keyservers.net:80 0xA5D32F012649A5A9
RUN apt-get -y update && apt-get -y install singularity-container

RUN pip3 install \
    biopython \
    networkx \
    numpy

RUN apt-get -y remove wget \
    python3-setuptools
RUN apt-get -y autoremove && apt-get -y clean
RUN rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN mkdir /var/scratch/
RUN chmod 777 /var/scratch/
RUN mkdir /data/

WORKDIR /data
ENV ASAP_HOME=/asap

ENTRYPOINT [ "java", "-jar", "/asap/asap.jar", "--project-dir", "/data", "--local" ]
