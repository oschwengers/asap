# ASA³P database scripts
Scripts within this directory build several databases used in ASA³P.

## MLST
The primary MLST scheme source for ASA³P is PubMLST site which is hosted at The Department of Zoology, University of Oxford, UK and is funded by The Wellcome Trust.
In order to update the MLST database execute the subsequent commands:

    $ASAP_HOME/bin/groovy $ASAP_HOME/scripts/admin/asap-mlst-download-pubmlst.groovy -o .
    $ASAP_HOME/bin/groovy $ASAP_HOME/scripts/admin/asap-mlst-create-db-json.groovy -p schemes/ > mlst-db.json
    tar -czf schemes.tar.gz schemes/
    rm -r schemes
    cp -r ./* $ASAP_HOME/db/mlst/


## Annotation sequences
asap-proteins.ffa contains a combination of additional 3rd party protein sequences of high quality.
These are used during annotation with Prokka via its --proteins option.

In order to update the proteins sequences execute the subsequent commands:
    $ASAP_HOME/scripts/admin/asap-update-sequences.sh

### 3rd Party Sequences

#### CARD
CARD (https://card.mcmaster.ca/) is a database for antimicrobial resistance models.
Last version: 2018-01-22

#### VFDB
VFDB (http://www.mgc.ac.cn/VFs/main.htm) is a database for virulence factor genes.
Last version: 2018-01-22
