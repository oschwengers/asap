#!/bin/bash

if [ $# -ne 2 ]; then
  echo 1>&2 "Usage: $0 <ASA³P_DIR> <PROJECT_DIR>"
  exit 3
fi

set -e

if [ ! -d $1 ]
  then
	echo "ASA³P directory does not exist!"
	exit 1
  else
	ASAP="$(dirname $(readlink -e $1))/$(basename $1)"
fi

if [ ! -d $2 ]
  then
	echo "Project directory does not exist!"
	exit 1
  else
	DATA="$(dirname $(readlink -e $2))/$(basename $2)"
fi

echo "ASA³P: $ASAP"
echo "DATA:  $DATA"

sudo docker run \
	--rm \
	-v $ASAP:/asap:ro \
	-v $DATA:/data \
	oschwengers/asap

