#!/bin/bash

curl -o card-ontology.tar.bz2 https://card.mcmaster.ca/latest/ontology
mkdir card-ontology
tar -xjf card-ontology.tar.bz2 --directory card-ontology
cp card-ontology/aro.json $ASAP_HOME/share/card/
rm -r card-*
