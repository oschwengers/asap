
import java.nio.file.*

ncbiPath = 'ftp://ftp.ncbi.nlm.nih.gov/genomes'


Channel.fromPath( "${ncbiPath}/refseq/bacteria/assembly_summary.txt" )
    .splitCsv( skip: 2, sep: '\t'  )
    .map( {
        def species = it[7]
        def strain  = it[8] - 'strain='
        if( species.contains( strain ) )
            return [ it[0], it[5], species, it[19] - 'ftp://ftp.ncbi.nlm.nih.gov/genomes/' ]
        else
            return [ it[0], it[5], "${species} ${strain}", it[19] - 'ftp://ftp.ncbi.nlm.nih.gov/genomes/' ]
    } )
    .set { validGenomes }


process download {

    maxForks 3
    maxRetries 3
    tag { "${acc} - ${orgName}" }

    input:
    set val(acc), val(taxId), val(orgName), val(path) from validGenomes

    output:
    set val(acc), val(taxId), file("${acc}") into buildMashDb
    set val(acc), val(taxId), val(orgName) into dbEntries

    script:
    """
    wget -O ${path.split('/').last()}_genomic.fna.gz ${ncbiPath}/${path}/${path.split('/').last()}_genomic.fna.gz
    mv ${path.split('/').last()}_genomic.fna.gz genome.fasta.gz
    """
}



dbEntries.map { "${it[0]}\t${it[1]}\t${it[2]}" }
    .collectFile( name: 'db.tsv', storeDir: './db/', newLine: true )



process buildMashDb {

    tag { "${acc} - ${orgName}" }

    cpus 1

    input:
    set val(acc), val(taxId), file("${acc}") from buildMashDb

    output:
    file("${acc}.msh") into output

    publishDir path: './db/', pattern: '*.msh', mode: 'link'

    script:
    """
    #gunzip -f genome.fasta.gz
    mash sketch -k 32 -s 10000 ${acc}
    """
}

