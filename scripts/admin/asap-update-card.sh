#!/bin/bash

curl -o card-software.tar.bz2 https://card.mcmaster.ca/latest/software
tar -xjf card-software.tar.bz2
tar -xzf release-rgi-*

mv release-rgi-*/ card/
rm -r card/_docs card/tests


curl -o card-data.tar.bz2 https://card.mcmaster.ca/latest/data
mkdir card-data
tar -xjf card-data.tar.bz2 --directory card-data

/usr/bin/env python3 card/load.py -i card-data/card.json
/usr/bin/env python3 card/clean.py

cp card-data/aro.json card
rm -r *.bz2 *.gz card-data

#DATA_VERSION=$(/usr/bin/env python3 card/rgi.py -dv)
#RGI_VERSION=$(/usr/bin/env python3 card/rgi.py -sv)
#echo "data version: ${DATA_VERSION}"
#echo "rgi version: ${RGI_VERSION}"
