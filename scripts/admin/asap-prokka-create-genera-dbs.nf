#!$ASAP_HOME/bin/nextflow


println "ncbi genomes Bacteria folder: ${params.ncbiGenomes}"
println "Prokka genera db folder: ${params.prokkaGenusDB}"


dirs = Channel.fromPath( "${params.ncbiGenomes}/*", type: 'dir' )
    .map{ it.getFileName().toString().split('_')[0] }
    .filter{ it.length() > 0 && it[0] in 'A'..'Z' }
    .distinct()



process processGenus  {

    tag { genus }

    executor 'drmaa'
    cpus 5
    penv 'multislot'

    errorStrategy 'retry'
    maxRetries 3

    publishDir params.prokkaGenusDB, pattern: "${genus}*", overwrite: true, mode: 'move'

    input:
    val genus from dirs

    output:
    val genus into results
    file "${genus}*" into empty

    """
    echo path: ${params.ncbiGenomes}/${genus}
    $ASAP_HOME/share/prokka/bin/prokka-genbank_to_fasta_db ${params.ncbiGenomes}/${genus}_*/*.gbk >> ${genus}.faa
    $ASAP_HOME/share/cdhit/cd-hit -i ${genus}.faa -o ${genus}.fasta -T 0 -M 0 -g 1 -s 0.8 -c 0.9
    rm -fv ${genus}.faa ${genus}.fasta.clstr
    $ASAP_HOME/share/blast/bin/makeblastdb -dbtype prot -in ${genus}.fasta -out ${genus} -title "${genus} genus db"
    """

}

results.subscribe{ println "genus $it finished" }
