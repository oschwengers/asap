FROM ubuntu:16.04

LABEL version="1.0.0"
LABEL maintainer="andreas.hoek@bio.uni-giessen.de"
LABEL maintainer="oliver.schwengers@computational.bio.uni-giessen.de"

RUN apt-get -y update && apt-get -y install \
    gnuplot-nox \
    less \
    libdatetime-perl libxml-simple-perl libdigest-md5-perl bioperl \
    libtbb2 \
    openjdk-8-jdk \
    python-pip \
    roary
RUN pip install biopython
RUN pip install networkx
RUN pip install numpy
RUN mkdir /var/scratch/

ENV ASAP_HOME=/asap/
ENV ASAP_DB=/asap/db/

CMD [ "java", "-jar", "/asap/asap.jar", "--dir", "/data/", "--local" ]

