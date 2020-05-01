#!/bin/bash

######################################################
# ASA³P bash wrapper script for Docker
#
# Use this bash wrapper script in order to forestall common
# issues with paths and Docker related options.
# Also make sure to leave this script within the ASA³P directory, as by this,
# the exact path to the ASA³P directory is auto-detected and forwarded, correctly.
######################################################

set -e
ASAP=""
DATA=""
SCRATCH=""
SKIP_CHAR=""
SKIP_COMP=""
DEBUG=""

usage() {
  echo "Usage: $0 -p <PROJECT_DIR> [-a <ASAP_DIR>] [-s <SCRATCH_DIR>] [-z] [-c] [-d] [-h]" 1>&2
}


exit_abnormal() {
  echo -e "\nFor further information, please have a look at: https://github.com/oschwengers/asap"
  exit 1
}


while getopts ":p:a:s:zcdh" options; do
    case "${options}" in
        p)
            if [[ "$OPTARG" == -* ]]; then
                usage
                echo "Error: -p requires an argument!"
                exit_abnormal
            else
                DATA=${OPTARG}
            fi
            ;;
        a)
            if [[ "$OPTARG" == -* ]]; then
                usage
                echo "Error: -a requires an argument!"
                exit_abnormal
            else
                ASAP=${OPTARG}
            fi
            ;;
        s)
            if [[ "$OPTARG" == -* ]]; then
                usage
                echo "Error: -s requires an argument!"
                exit_abnormal
            else
                SCRATCH=${OPTARG}
            fi
            ;;
        z)
            SKIP_CHAR="--skip-char"
            echo "Skip characterization steps"
            ;;
        c)
            SKIP_COMP="--skip-comp"
            echo "Skip comparative steps"
            ;;
        d)
            DEBUG="--debug"
            echo "Enable debugging output: $DEBUG"
            ;;
        h)
            usage && exit 0;;
        :) # expected argument omitted:
            usage
            echo "Error: -${OPTARG} requires an argument!"
            exit_abnormal
            ;;
        ?) # unknown option:
            usage
            echo "Error: unknown argument (${OPTARG}) detected!"
            exit_abnormal
            ;;
    esac
done


# set ASAP_DIR to default path and/or normalize path
if [ "$ASAP" = "" ]; then # if $ASAP was _not_ set by the user
    SCRIPT_PATH=$(realpath $0)
    ASAP=$(dirname $SCRIPT_PATH)
else
    ASAP=$(realpath $ASAP)
fi


# test existence of the ASAP directory
if [ ! -f $ASAP/asap.jar ]; then
    echo "ASA³P directory (${ASAP}) does not exist or seems to be corrupt!"
    echo "Is this script stored withtin the ASA³P directory? If not, please specify the absolute path to it via '-a <ASAP_DIR>' or copy it into the ASA³P directory."
    exit_abnormal
else
    echo "ASA³P:   ${ASAP}"
fi


if [ "$DATA" = "" ]; then # if $DATA was _not_ set by the user
    echo "Project directory must be specified!"
    usage
    exit_abnormal
fi


# test existence of the project directory
if [ ! -d $DATA ]; then
    echo "Project directory (${DATA}) does not exist!"
    exit_abnormal
else
    DATA=$(realpath $DATA)
    echo "DATA:    ${DATA}"
fi


# test existance of the config file
if [ ! -f $DATA/config.xls ]; then
    echo "No config file in project directory detected!"
    echo -e "\nPlease, provide a proper 'config.xls' file within the project directory."
    exit_abnormal
fi


# test existance of the data subdirectory
if [ ! -d $DATA/data ]; then
    echo "No data subdirectory in project directory detected!"
    echo -e "\nPlease, provide a proper 'data' subdirectory within the project directory, containing all reference and isolate related files."
    exit_abnormal
fi


# normalize SCRATCH if set by the user
if [ "$SCRATCH" != "" ]; then # if $SCRATCH _was_ set by the user
    if [ ! -d $SCRATCH ]; then
        echo "Scratch directory (${SCRATCH}) does not exist!"
        exit_abnormal
    else
        SCRATCH=$(realpath $SCRATCH)
        echo "Scratch: $SCRATCH"
    fi
fi


export USER_ID=$(id -u)
export GROUP_ID=$(id -g)


if [ "$SCRATCH" != "" ]; then
    sudo docker run \
        --privileged \
        --rm \
        --user $USER_ID:$GROUP_ID \
        --volume="$ASAP:/asap:ro" \
        --volume="$DATA:/data" \
        --volume="$SCRATCH:/var/scratch" \
        --volume="/etc/group:/etc/group:ro" \
        --volume="/etc/passwd:/etc/passwd:ro" \
        oschwengers/asap:v1.3.0 \
        $SKIP_CHAR $SKIP_COMP $DEBUG

else
    sudo docker run \
    --privileged \
    --rm \
    --user $USER_ID:$GROUP_ID \
    --volume="$ASAP:/asap:ro" \
    --volume="$DATA:/data" \
    --volume="/etc/group:/etc/group:ro" \
    --volume="/etc/passwd:/etc/passwd:ro" \
    oschwengers/asap:v1.3.0 \
    $SKIP_CHAR $SKIP_COMP $DEBUG
fi


# exit with 'docker run' exit code
exit $?
