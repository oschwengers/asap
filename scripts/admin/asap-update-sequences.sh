
curl -o card-data.tar.bz2 https://card.mcmaster.ca/latest/data
mkdir card-data
tar -xjf card-data.tar.bz2 --directory card-data
$ASAP_HOME/bin/groovy $ASAP_HOME/scripts/admin/asap-extract-card-proteins.groovy card-data/card.json > card.faa

curl -o VFDB_setA_pro.fas.gz http://www.mgc.ac.cn/VFs/Down/VFDB_setA_pro.fas.gz
gunzip VFDB_setA_pro.fas.gz
$ASAP_HOME/bin/groovy $ASAP_HOME/scripts/admin/asap-extract-vf-proteins.groovy VFDB_setA_pro.fas > vfdb.faa

cat card.faa vfdb.faa > $ASAP_DB/sequences/asap-proteins.faa
cp vfdb.faa $ASAP_DB/sequences/

$ASAP_HOME/share/blast/bin/makeblastdb -dbtype prot -in $ASAP_DB/sequences/vfdb.faa -title 'VFDB'

rm -r card* vfdb.faa VFDB_setA_pro.fas

