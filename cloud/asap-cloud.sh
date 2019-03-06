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


### 1) Check arguments and make paths absolute

OPENSTACK_RC_FILE=$(realpath $OPENSTACK_RC_FILE)
if [ ! -f $OPENSTACK_RC_FILE ]; then
    echo "Wrong path to OpenStack RC file!"
    exit 1
fi

PROJECT_DIR=$(realpath $PROJECT_DIR)
if [ ! -d $PROJECT_DIR ]; then
    echo "Wrong path to project directory!"
    exit 1
fi

SCRIPT_PATH=$(realpath $0)
ASAP_CLOUD=$(dirname $SCRIPT_PATH)


### 2) ASAP cluster logic script & create bibigrid.yml file ###

# Create bibigrid.yml file with the asap-cloud-setup script. This script needs the asap.properties file in the same dir ($ASAP_CLOUD/)
java -jar $ASAP_CLOUD/asap-cloud-setup.jar -p $PROJECT_DIR

# Extract ID of data volume from asap.properties file
DATA_VOLUME_ID=$(cat $ASAP_CLOUD/asap.properties | grep "volume.data*" | cut -d= -f2)
echo "#############################################"
echo "OpenStack RC file = $OPENSTACK_RC_FILE"
echo "Project directory = $PROJECT_DIR"
echo "Data volume id = $DATA_VOLUME_ID"
echo "#############################################"

### 3) source openstack.rc file ###

# Source the environment variables for the Openstack CLI tool
source $OPENSTACK_RC_FILE


### 4) detach volume(s) in openstack ###

echo "#############################################"
echo "Prepare OpenStack project"
echo "#############################################"
# Unmount & detach the data volume & project dir
sudo umount /data/
nova volume-detach $INSTANCE_ID $DATA_VOLUME_ID


### 5) create new ssh-key for bibigrid ###

# Create new ssh keypair with Openstack CLI tool & write the key into a local file.
openstack keypair create asap-cluster > $ASAP_CLOUD/asap.cluster.key

# Change filemod
chmod 600 $ASAP_CLOUD/asap.cluster.key


### 6) start bibigrid cluster ###

echo "#############################################"
echo "Starting BiBiGrid cluster..."
echo "#############################################"
sleep 1

# Start of BiBiGrid SGE cluster
java -jar $ASAP_CLOUD/BiBiGrid-asap.jar -c -o $ASAP_CLOUD/bibigrid.yml | tee $ASAP_CLOUD/bibigrid-specs

# Extract ID & IP address of the newly created cluster
BIBIGRID_IP=$(grep -E 'BIBIGRID_MASTER=[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}' $ASAP_CLOUD/bibigrid-specs | grep -Eo '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')
BIBIGRID_ID=$(grep -E 'The cluster id of your started cluster is:' $ASAP_CLOUD/bibigrid-specs | grep -Eo '[A-Za-z0-9]{15}')

echo "#############################################"
echo "BiBiGrid ip = $BIBIGRID_IP"
echo "BiBiGrid id = $BIBIGRID_ID"
echo "#############################################"

# Remove temporary file containing BiBiGrid output
rm $ASAP_CLOUD/bibigrid-specs


### 7) login to master node & start ASA3P run on the cluster ###

# Disable StrictHostKeyChecking (Login via ssh without manual confirmation)
echo "#############################################"
echo "Disabling StrictHostKeyChecking"
echo "#############################################"
sudo sed -i "s/#   StrictHostKeyChecking ask/   StrictHostKeyChecking no/g" /etc/ssh/ssh_config

# Add ssh key
eval `ssh-agent -s`
ssh-add $ASAP_CLOUD/asap.cluster.key

# Remove known_hosts file to prevent login problems, as the Floating IP might be used several different VMs
ssh-keygen -f "/home/ubuntu/.ssh/known_hosts" -R $BIBIGRID_IP

# Start ASAP at the cluster
echo "#############################################"
echo "Starting ASA³P"
echo "#############################################"
ssh -l ubuntu $BIBIGRID_IP "export ASAP_HOME=/asap/ && java -jar /asap/asap.jar -d $PROJECT_DIR/"

# Enable StrictHostKeyChecking
echo "#############################################"
echo "Enabling StrictHostKeyChecking"
echo "#############################################"
sudo sed -i "s/   StrictHostKeyChecking no/#   StrictHostKeyChecking ask/g" /etc/ssh/ssh_config


### 8) terminate bibigrid cluster ###

echo "#############################################"
echo "ASA³P analysis finished"
echo "Terminating BiBiGrid cluster"
echo "#############################################"
java -jar $ASAP_CLOUD/BiBiGrid-asap-2.0.jar -o $ASAP_CLOUD/bibigrid.yml -t $BIBIGRID_ID


### 9) Delete ssh keypair

echo "#############################################"
echo "Clean up OpenStack project"
echo "#############################################"
# Delete key on local machine
rm $ASAP_CLOUD/asap.cluster.key

# Delete key in Openstack
openstack keypair delete asap-cluster


### 10) attach free volume with results from analysis ###

# Attach & mount the data volume & project dir
echo "#############################################"
echo "Attaching data volume"
echo "#############################################"
nova volume-attach $INSTANCE_ID $DATA_VOLUME_ID
sleep 10
echo "#############################################"
echo "Mounting data volume"
echo "#############################################"
sudo mount $(ls -t /dev/vd* | head -n 1 | grep -o "/dev/vd[a-z]") /data/
