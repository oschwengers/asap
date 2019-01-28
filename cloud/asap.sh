#!/bin/bash

### Variables for gateway_instance_id, path to project directory & path to openstack.rc file ###

while getopts ":o:i:p:" opt; do
  case $opt in
    o)
      OPENSTACK_RC_FILE=${OPTARG}
      ;;
    i)
      INSTANCE_ID=${OPTARG}
      ;;
    p)
      PROJECT_DIR=${OPTARG}
      ;;
    \?)
      echo "Invalid option: -$OPTARG" #>&2
      ;;
  esac
done


### 1) ASAP cluster logic script & create .bibigrid.properties file ###

# Create .bibigrid.properties file with the asap-cloud-setup script from Oliver Schwengers. This script NEEDS the asap.properties file in the same dir (~/asap-cloud/)
java -jar ~/asap-cloud/asap-cloud-setup-1.0.0.jar -p $PROJECT_DIR

# Extract ID of data volume from asap.properties file
VOLUME_ID=$(cat ~/asap-cloud/asap.properties | grep "volume.data*" | cut -d= -f2)
echo "#############################################"
echo "VOLUME_ID = $VOLUME_ID"
echo "#############################################"

### 2) source openstack.rc file ###

# Source the environment variables for the Openstack CLI tool
source $OPENSTACK_RC_FILE


### 3) detach volume(s) in openstack ###

# Unmount & detach the data volume & project dir
sudo umount /mnt/data/
nova volume-detach $INSTANCE_ID $VOLUME_ID


### 4) create new ssh-key for bibigrid ###

# Create new ssh keypair with Openstack CLI tool & write the key into a local file.
openstack keypair create asap-cluster > ~/asap-cloud/asap.cluster.key

# Change filemod
chmod 600 ~/asap-cloud/asap.cluster.key


### 5) start bibigrid cluster ###

echo "#############################################"
echo "Starting bibigrid cluster..."
echo "#############################################"
sleep 1

# Start of BiBiGrid SGE cluster
java -jar ~/asap-cloud/BiBiGrid-1.1.jar -c -o ~/asap-cloud/.bibigrid.properties | tee ~/asap-cloud/bibigrid-specs

# Extract ID & IP address of the newly created cluster
BIBIGRID_IP=$(cat ~/asap-cloud/bibigrid-specs | grep -E 'BIBIGRID_MASTER=[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}' | grep -Eo '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')
BIBIGRID_ID=$(cat ~/asap-cloud/bibigrid-specs | grep -E 'The cluster id of your started cluster is :' | grep -Eo '[A-Za-z0-9]{15}')

echo "#############################################"
echo "BIBIGRID_IP = $BIBIGRID_IP"
echo "BIBIGRID_ID = $BIBIGRID_ID"
echo "#############################################"

# Remove temporary file containing BiBiGrid output
rm ~/asap-cloud/bibigrid-specs


### 6) login to master node & start ASA3P run on the cluster ###

# Disable StrictHostKeyChecking (Login via ssh without manual confirmation)
echo "#############################################"
echo "Disabling StrictHostKeyChecking"
echo "#############################################"
sudo sed -i "s/#   StrictHostKeyChecking ask/   StrictHostKeyChecking no/g" /etc/ssh/ssh_config

# Add ssh key
eval `ssh-agent -s`
ssh-add ~/asap-cloud/asap.cluster.key

# Remove known_hosts file to prevent login problems, as the Floating IP might be used several different VMs
ssh-keygen -f "/home/ubuntu/.ssh/known_hosts" -R $BIBIGRID_IP

# Start ASAP at the cluster
echo "#############################################"
echo "Starting ASAP"
echo "#############################################"
ssh -l ubuntu $BIBIGRID_IP "export ASAP_HOME=/mnt/asap/ && java -jar /mnt/asap/asap.jar -d $PROJECT_DIR/"

# Enable StrictHostKeyChecking
echo "#############################################"
echo "Enabling StrictHostKeyChecking"
echo "#############################################"
sudo sed -i "s/   StrictHostKeyChecking no/#   StrictHostKeyChecking ask/g" /etc/ssh/ssh_config


### 7) terminate bibigrid cluster ###

java -jar ~/asap-cloud/BiBiGrid-1.1.jar -o ~/asap-cloud/.bibigrid.properties -t $BIBIGRID_ID


### 8) Delete ssh keypair

# Delete key on local machine
rm ~/asap-cloud/asap.cluster.key

# Delete key in Openstack
openstack keypair delete asap-cluster


### 9) attach free volume with results from analysis ###

# Attach & mount the data volume & project dir
echo "#############################################"
echo "Attaching volume"
echo "#############################################"
nova volume-attach $INSTANCE_ID $VOLUME_ID
echo "#############################################"
echo "Mounting volume"
echo "#############################################"
sudo mount $(ls -t /dev/vd* | head -n 1 | grep -o "/dev/vd[a-z]") /mnt/data/
