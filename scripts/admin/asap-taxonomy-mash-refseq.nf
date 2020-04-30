
import java.nio.file.*


assemblySummary = params.ass_sum
ncbiPath        = 'ftp://ftp.ncbi.nlm.nih.gov/genomes'
domain          = 'bacteria'


Channel.fromPath( assemblySummary )
    .splitCsv( skip: 2, sep: '\t'  )
    .filter( { (it[4].toLowerCase() == 'representative genome')  ||  (it[4].toLowerCase() == 'reference genome') } )
    .map( {
        def species = it[7]
        def strain  = it[8] - 'strain='
        if( species.contains( strain ) )
            return [ it[0], it[5], species, it[19] - 'ftp://ftp.ncbi.nlm.nih.gov/genomes/' ]
        else
            return [ it[0], it[5], "${species} ${strain}", it[19] - 'ftp://ftp.ncbi.nlm.nih.gov/genomes/' ]
    } )
    .set { validGenomes }


process sketch {

    tag { "${acc} - ${orgName}" }

    maxForks 3
    errorStrategy 'ignore'
    maxRetries 3

    input:
    set val(acc), val(taxId), val(orgName), val(path) from validGenomes

    output:
    set val(acc), val(taxId), val(orgName) into dbEntries
    file("${acc}.msh") into outMash
    file("${acc}.fna") into outFasta

    publishDir pattern: '*.fna', path: "./${domain}/", mode: 'move'
    publishDir pattern: '*.msh', path: './sketches/',  mode: 'move'

    script:
    """
    wget -O ${acc}.gz ${ncbiPath}/${path}/${path.split('/').last()}_genomic.fna.gz
    gunzip ${acc}.gz
    ${REFERENCE_SEEKER_HOME}/share/mash/mash sketch -k 21 -s 10000 ${acc}
    mv ${acc} ${acc}.fna
    """
}


dbEntries.map { "${it[0]}\t${it[1]}\t${it[2]}" }
    .collectFile( name: 'db.tsv', storeDir: "./${domain}/", newLine: true )
