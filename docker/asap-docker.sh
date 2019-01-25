#!/bin/bash

if [ $# -ne 2 ] && [ $# -ne 3 ]; then
    echo 1>&2 "Usage: $0 <ASA³P_DIR> <PROJECT_DIR> [<SCRATCH_DIR>]"
    exit 3
fi

set -e

if [ ! -d $1 ]
    then
        echo "ASA³P directory does not exist!"
        exit 1
    else
        ASAP=$(realpath $1)
        echo "ASA³P:   $ASAP"
fi

if [ ! -d $2 ]
    then
        echo "Project directory does not exist!"
        exit 1
    else
        DATA=$(realpath $2)
        echo "DATA:    $DATA"
fi

if [ $# -eq 3 ]
    then
        if [ ! -d $3 ]
            then
                echo "Scratch directory does not exist!"
                exit 1
            else
                SCRATCH=$(realpath $3)
                echo "Scratch: $SCRATCH"
                sudo docker run \
                    --rm \
                    -v $ASAP:/asap:ro \
                    -v $DATA:/data \
                    -v $SCRATCH:/var/scratch \
                    oschwengers/asap
        fi
    else
        echo "Scratch: internal container scratch"
	sudo docker run \
            --rm \
            -v $ASAP:/asap:ro \
            -v $DATA:/data \
            oschwengers/asap
fi
