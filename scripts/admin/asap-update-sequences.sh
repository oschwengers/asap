#!/bin/bash

curl -o resfinder.zip 'https://cge.cbs.dtu.dk/cge/download_data.php' -H 'Content-Type: application/x-www-form-urlencoded' --data 'folder=resfinder&filename=resfinder.zip'
unzip resfinder.zip

$ASAP_HOME/bin/groovy $ASAP_HOME/scripts/admin/asap-prokka-transform-resfinder-headers.groovy ./notes.txt

wget http://www.mgc.ac.cn/VFs/Down/VFDB_setA_pro.fas.gz
gunzip VFDB_setA_pro.fas.gz
sed 's/^>\(.*)\) (\([[:alnum:]/]*\)) \(.*\)$/>\1 ~~~\2~~~\3/' VFDB_setA_pro.fas > vfdb.ffa

cat ResFinder.ffa vfdb.ffa > asap-proteins.ffa
cp asap-proteins.ffa $ASAP_DB/sequences/
rm *.fsa notes.txt ResFinder.ffa resfinder.zip VFDB_setA_pro.fas asap-proteins.ffa


$ASAP_HOME/share/blast/bin/makeblastdb  -dbtype prot -in vfdb.ffa -title 'VFDB'
cp vfdb.ffa* $ASAP_DB/sequences/
rm vfdb.ffa*
