#!/bin/bash

######################################################
# ASA³P bash wrapper script for Docker
#
# Please, use this bash wrapper script in order to forestall common
# issues with paths and Docker related options.
# Also make sure to leave this script within the ASA³P directory, as by this,
# the exact path to the ASA³P directory is auto-detected and forwarded, correctly.
######################################################

set -e

if [ $# -ne 1 ] && [ $# -ne 2 ]; then
    echo 1>&2 "Usage: $0 <PROJECT_DIR> [<SCRATCH_DIR>]"
    echo -e 1>&2 "\nPlease, always execute a version of this script stored within the ASA³P directory."
    echo -e 1>&2 "By this, we can ensure that the exact path to the ASA³P directory is auto-detected and forwarded, correctly."
    exit 3
fi

SCRIPT_PATH=$(realpath $0)
ASAP=$(dirname $SCRIPT_PATH)
if [ ! -f $ASAP/asap.jar ]
    then
        echo "ASA³P directory does not exist or seems to be corrupt!"
        echo "Is this script stored withtin the ASA³P directory? If not, please copy it into the ASA³P directory."
        echo "In case your not sure how to provide your data, please have a look at the readme:"
        echo "https://github.com/oschwengers/asap"
        exit 1
    else
        echo "ASA³P:   $ASAP"
fi


# test existence of the project directory
if [ ! -d $1 ]
    then
        echo "Project directory does not exist!"
        exit 1
    else
        DATA=$(realpath $1)
        echo "DATA:    $DATA"
fi


# test existance of the config file
if [ ! -f $DATA/config.xls ]; then
    echo "No config file in project directory detected!"
    echo -e "\nPlease, provide a proper 'config.xls' file within the project directory."
    echo "In case your not sure how to provide your data, please have a look at the readme:"
    echo "https://github.com/oschwengers/asap"
    exit 1
fi


# test existance of the data subdirectory
if [ ! -d $DATA/data ]; then
    echo "No data subdirectory in project directory detected!"
    echo -e "\nPlease, provide a proper 'data' subdirectory within the project directory, containing all reference and isolate related files."
    echo "In case your not sure how to provide your data, please have a look at the readme:"
    echo "https://github.com/oschwengers/asap"
    exit 1
fi

# test provision and existence of a scratch directory
if [ $# -eq 2 ]
    then
        if [ ! -d $2 ]
            then
                echo "Scratch directory does not exist!"
                exit 1
            else
                SCRATCH=$(realpath $2)
                echo "Scratch: $SCRATCH"
                sudo docker run \
                    --privileged \
                    --rm \
                    -v $ASAP:/asap:ro \
                    -v $DATA:/data \
                    -v $SCRATCH:/var/scratch \
                    oschwengers/asap
        fi
    else
        echo "Scratch: internal container scratch"
	sudo docker run \
            --privileged \
            --rm \
            -v $ASAP:/asap:ro \
            -v $DATA:/data \
            oschwengers/asap
fi
