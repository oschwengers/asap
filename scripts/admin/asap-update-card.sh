#!/bin/bash

curl -o card-data.tar.bz2 https://card.mcmaster.ca/latest/data
mkdir card-data
tar -xjf card-data.tar.bz2 --directory card-data
/usr/bin/env python3 $ASAP_HOME/share/card/rgi load -i card-data/card.json
/usr/bin/env python3 $ASAP_HOME/share/card/rgi clean

curl -o card-ontology.tar.bz2 https://card.mcmaster.ca/latest/ontology
mkdir card-ontology
tar -xjf card-ontology.tar.bz2 --directory card-ontology
cp card-ontology/aro.json $ASAP_HOME/share/card/
rm -r card-*

DATA_VERSION=$(/usr/bin/env python3 $ASAP_HOME/share/card/rgi database)
RGI_VERSION=$(/usr/bin/env python3 $ASAP_HOME/share/card/rgi main -v)
echo "data version: ${DATA_VERSION}"
echo "rgi version: ${RGI_VERSION}"
