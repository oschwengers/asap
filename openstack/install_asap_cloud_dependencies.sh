#!/bin/bash

# Shell script to install all dependencies required by ASA³P on an Ubuntu 14.04 Cloud Image.
# Should be run as sudo
# Author: Andreas Hoek

Check() {
	VAR=$1
	APP=$2
	if [[ $VAR > 0 ]]
	then
		echo "Step: $APP failed"
		exit
	else
		echo "############################################################"
		echo "Step: $APP successful"
		echo "############################################################"
		sleep 3
	fi
}

sudo add-apt-repository ppa:openjdk-r/ppa
Check $? "Add OpenJDK 8 repo"

sudo apt-get -y update
Check $? "Update"

sudo apt-get -y upgrade
Check $? "Upgrade"

sudo apt-get -y install openjdk-8-jdk openjdk-8-demo openjdk-8-doc openjdk-8-jre-headless openjdk-8-source
Check $? "OpenJDK 8"

sudo apt-get -y install python-pip
Check $? "Pip"

sudo apt-get -y install language-pack-de
Check $? "German language pack"

sudo pip install -U pip setuptools
Check $? "Setuptools"

sudo apt-get install -y python-dev
Check $? "Python-dev"

sudo pip install biopython
Check $? "Biopython"

sudo pip install numpy
Check $? "numpy"

sudo pip install networkx
Check $? "networkx"

sudo apt-get -y install libtbb2
Check $? "libtbb2"

sudo mkdir /var/scratch/
Check $? "/var/scratch/ creation"

sudo chmod 777 /var/scratch
Check $? "chmod 777 /var/scratch/"

sudo apt-get -y install gnuplot-nox
Check $? "Gnuplot"

sudo apt-get -y install libdatetime-perl libxml-simple-perl libdigest-md5-perl bioperl
Check $? "Bioperl"

sudo apt-get -y install unzip
Check $? "Unzip"

sudo apt-get -y install bedtools cd-hit ncbi-blast+ mcl parallel cpanminus prank mafft fasttree
Check $? "Roary dependencies"

sudo cpanm -f Bio::Roary
Check $? "Roary"

echo -e "\n#ASAP \nexport ASAP_HOME=/mnt/asap" >> /home/ubuntu/.profile
Check $? "Write ASAP variables to .profile"

export ASAP_HOME=/mnt/asap/
Check $? "export variable ASAP_HOME"
