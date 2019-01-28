FROM ubuntu:16.04

LABEL version="1.1.0"
LABEL maintainer="andreas.hoek@bio.uni-giessen.de"
LABEL maintainer="oliver.schwengers@computational.bio.uni-giessen.de"

RUN apt-get -y update && apt-get -y install \
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

RUN pip3 install \
    biopython \
    networkx \
    numpy \
    filetype \
    pytest \
    mock \
    pandas \
    matplotlib \
    seaborn \
    pyfaidx \
    pyahocorasick

RUN mkdir /var/scratch/

ENV ASAP_HOME=/asap

CMD [ "java", "-jar", "/asap/asap.jar", "--dir", "/data/", "--local" ]
