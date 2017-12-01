#!/bin/bash

wget ftp://ftp.ncbi.nih.gov/pub/taxonomy/taxdump.tar.gz
mkdir taxdump
tar -xvzf taxdump.tar.gz -C taxdump

$ASAP_HOME/bin/groovy asap-convert-ncbi-taxonomy.groovy -p taxdump > ncbi-taxonomies.tsv

grep 'Bacteria;' ncbi-taxonomies.tsv > ncbi-bacteria-taxonomies.tsv

rm -r taxdump* ncbi-taxonomies.tsv
