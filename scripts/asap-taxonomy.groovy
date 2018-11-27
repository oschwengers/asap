// always invoke this script via $ASAP_HOME/bin/groovy

/**********************
 *** Script Imports ***
**********************/


import java.nio.file.*
import groovy.json.*
import groovy.util.CliBuilder
import org.slf4j.LoggerFactory
import bio.comp.jlu.asap.api.DataType
import bio.comp.jlu.asap.api.FileType

import static bio.comp.jlu.asap.api.GenomeSteps.*
import static bio.comp.jlu.asap.api.MiscConstants.*
import static bio.comp.jlu.asap.api.Paths.*




/************************
 *** Script Constants ***
************************/
final def env = System.getenv()
ASAP_HOME = env.ASAP_HOME
ASAP_DB   = env.ASAP_DB

KRAKEN       = "${ASAP_HOME}/share/kraken"
NUCMER       = "${ASAP_HOME}/share/mummer/nucmer"
DELTA_FILTER = "${ASAP_HOME}/share/mummer/delta-filter"
BLASTN       = "${ASAP_HOME}/share/blast/bin/blastn"
CMSEARCH     = "${ASAP_HOME}/share/infernal/cmsearch"

KRAKEN_DB = "${ASAP_DB}/kraken"
RDP_DB   = "${ASAP_DB}/rdp/rdp-bacteria.fasta"
RFAM_CM_SSU_RRNA = "${ASAP_DB}/RF00177.cm"


NUM_THREADS       = 4
MIN_FRAGMENT_SIZE = 100



/*********************
 *** Script Params ***
*********************/


log = LoggerFactory.getLogger( getClass().getName() )

def cli = new CliBuilder( usage: 'asap-taxonomy.groovy --project-path <project-path> --genome-id <genome-id>' )
    cli.p( longOpt: 'project-path', args: 1, argName: 'project-path', required: true, 'Path to project directory' )
    cli.g( longOpt: 'genome-id', args: 1, argName: 'genome-id', required: false, 'Genome ID in config file' )
def opts = cli.parse( args )

if( !opts?.p ) {
    log.error( 'no project path provided!' )
    System.exit( 1 )
}

def genomeId
if( opts?.g  &&  opts.g ==~ /\d+/ )
    genomeId = Integer.parseInt( opts.g )
else if( env.SGE_TASK_ID  &&  env.SGE_TASK_ID ==~ /\d+/ )
    genomeId = Integer.parseInt( env.SGE_TASK_ID )
else {
    log.error( 'no genome id provided!' )
    System.exit( 1 )
}

// log system environment vars and Java properties
log.info( "SCRIPT: ${getClass().protectionDomain.codeSource.location.path}" )
log.info( "USER: ${env.USER}" )
log.info( "CWD: ${env.PWD}" )
log.info( "HOSTNAME: ${env.HOSTNAME}" )
log.info( "ASAP_HOME: ${env.ASAP_HOME}" )
log.info( "ASAP_DB: ${env.ASAP_DB}" )
log.info( "PATH: ${env.PATH}" )
def props = System.getProperties()
log.info( "script.name: ${props['script.name']}" )
log.info( "groovy.home: ${props['groovy.home']}" )
log.info( "file.encoding: ${props['file.encoding']}" )
log.info( "genome-id: ${genomeId}" )




/********************
 *** Script Paths ***
********************/


Path rawProjectPath = Paths.get( opts.p )
if( !Files.exists( rawProjectPath ) ) {
    println( "Error: project directory (${rawProjectPath}) does not exist!" )
    System.exit(1)
}
final Path projectPath = rawProjectPath.toRealPath()
log.info( "project-path: ${projectPath}")


// read config json
Path configPath = projectPath.resolve( 'config.json' )
if( !Files.isReadable( configPath ) ) {
    log.error( 'config.json not readable!' )
    System.exit( 1 )
}
final def config = (new JsonSlurper()).parseText( projectPath.resolve( 'config.json' ).text )


final def genome = config.genomes.find( { it.id == genomeId } )
if( !genome ) {
    log.error( "no genome found in config! genome-id=${genomeId}" )
    System.exit( 1 )
}
final String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
log.info( "genome-name: ${genomeName}")


final Path taxPath = projectPath.resolve( PROJECT_PATH_TAXONOMY )
Files.createFile( taxPath.resolve( "${genomeName}.running" ) ) // create state.running


// sequence path
final Path genomeSequencePath
Path scaffoldsPath = Paths.get( projectPath.toString(), PROJECT_PATH_SCAFFOLDS, genomeName, "${genomeName}.fasta" )
Path sequencePath  = Paths.get( projectPath.toString(), PROJECT_PATH_SEQUENCES, "${genomeName}.fasta" )
if( Files.isReadable( scaffoldsPath ) ) {
    genomeSequencePath = scaffoldsPath
    log.info( "sequence file (scaffolds): ${genomeSequencePath}" )
} else if( Files.isReadable( sequencePath ) ) {
    genomeSequencePath = sequencePath
    log.info( "sequence file: ${genomeSequencePath}" )
} else
    terminate( "no sequence file! gid=${genomeId}", taxPath, genomeName )


// create local tmp directory
final Path tmpPath = Paths.get( '/', 'var', 'scratch', "tmp-${System.currentTimeMillis()}-${Math.round(Math.random()*1000)}" )
try { // create tmp dir
    log.info( "tmp-folder: ${tmpPath}" )
    Files.createDirectory( tmpPath )
} catch( Throwable t ) {
    terminate( "could create tmp dir! gid=${genomeId}, tmp-dir=${tmpPath}", t, taxPath, genomeName )
}


// create info object
def info = [
    time: [
        start: (new Date()).format( DATE_FORMAT )
    ],
    genome: [
        id: genome.id,
        species: genome.species,
        strain: genome.strain
    ],
    path: taxPath.toString(),
    kmer: [
        lineages: [],
        classification: [:]
    ],
    rrna: [
        lineages: [],
        classification: [:]
    ],
    ani: [
        all: []
    ]
]




/********************
 *** Script Logic ***
 *** kmer (kraken) **
********************/

log.info( 'start kmer species classification via Kraken' )
ProcessBuilder pb = new ProcessBuilder( 'sh', '-c',
"${KRAKEN}/kraken --fasta-input ${genomeSequencePath} --threads ${NUM_THREADS} \
| ${KRAKEN}/kraken-translate \
> tax.txt" )
    .redirectErrorStream( true )
    .redirectOutput( ProcessBuilder.Redirect.INHERIT )
    .directory( tmpPath.toFile() )

def pbEnv = pb.environment() // set path variables
pbEnv.put( 'KRAKEN_DEFAULT_DB', KRAKEN_DB )

log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
if( pb.start().waitFor() != 0 ) terminate( 'could not exec kraken | kraken-translate > tax.txt!', taxPath, genomeName )
log.info( '----------------------------------------------------------------------------------------------' )


def taxSet = new HashMap<String,Integer>()
int hits = 0
tmpPath.resolve( 'tax.txt' ).eachLine( { line ->
    hits++
    String tax = line.split( '\t' )[1]
    Integer noTax = taxSet.get( tax )
    if( noTax == null ) taxSet.put( tax, 1 )
    else                taxSet.put( tax, noTax+1 )
} )
def taxList = taxSet.collect { k,v -> [ lineage: k, freq: v ] }
taxList.sort( { -it.freq } )
info.kmer.hits = hits
info.kmer.classification = taxList[0]
taxList.each( {
    it.lineage -= 'root;cellular organisms;'
    it.lineage = it.lineage.split( ';' )
    it.classification = it.lineage[ it.lineage.size()-1 ]
    info.kmer.lineages << it
} )




/********************
 *** Script Logic ***
 *** ANI (nucmer) ***
********************/

log.info( 'start 16S rRNA classification via blastn vs RDP db' )
Path cmOutPath = tmpPath.resolve( 'cm.out' )
pb = new ProcessBuilder( CMSEARCH,
    '--rfam',
    '--noali',
    '--cpu', Integer.toString( NUM_THREADS ),
    '--tblout', cmOutPath.toString(),
    RFAM_CM_SSU_RRNA,
    genomeSequencePath.toString() )
    .directory( tmpPath.toFile() )
log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
if( pb.start().waitFor() != 0 ) terminate( 'could not exec cmsearch!', taxPath, genomeName )
log.info( '----------------------------------------------------------------------------------------------' )

def ssu = [ 'score': 0.0 ]
cmOutPath.eachLine( { line ->
    if( line.charAt( 0 ) != '#' ) {
        def cols = line.split( '\\s+' )
        if( Float.parseFloat( cols[14] ) > ssu.score ) {
            ssu = [
                contig: cols[0],
                start: Integer.parseInt( cols[7] ),
                end: Integer.parseInt( cols[8] ),
                score: Float.parseFloat( cols[14] )
            ]
            if( ssu.start > ssu.end ) {
                int tmp = ssu.start
                ssu.start = ssu.end
                ssu.end = tmp
            }
            log.info( "detected 16S rRNA: contig=${ssu.contig}, start=${ssu.start}, end=${ssu.end}, score=${ssu.score}" )
        }
    }
} )

String ribSequence = null
def m = genomeSequencePath.text =~ /(?m)^>(.+)$\r?\n([ATGCNatgcn\r\n]+)$/ //include Windows line breaks (\r\n) as user provided scaffolds might be written on Windows systems
m.each( { match ->
    String name = match[1].split(' ')[0].trim()
    String contig = match[2].replaceAll( '[^ATGCNatgcn]', '' )
    if( name == ssu.contig ) {
        ribSequence = contig.substring( ssu.start - 1, ssu.end )
        log.info( "extracted 16S rRNA: contig=${name}, seq=${ ribSequence.substring( 0, 50 ) }..." )
    }
} )
assert ribSequence != null

Path seq16SPath = tmpPath.resolve( '16S.fasta' )
seq16SPath << '>16S\n'
seq16SPath << "${ribSequence}\n"


// blast genome vs RDP db
pb = new ProcessBuilder( BLASTN,
    '-query', seq16SPath.toString(),
    '-db', RDP_DB,
    '-num_threads', Integer.toString( NUM_THREADS ),
    '-evalue', '1E-10',
    '-outfmt', '6 sseqid slen nident score stitle' )
    .directory( tmpPath.toFile() )
log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
proc = pb.start()
stdOut = new StringBuilder()
stdErr = new StringBuilder()
proc.consumeProcessOutput( stdOut, stdErr )
if( proc.waitFor() != 0 ){
    log.error( stdErr.toString() )
    terminate( 'could not exec blastn!', taxPath, genomeName )
}
log.info( '----------------------------------------------------------------------------------------------' )


int highestScore = 0
taxList = []
stdOut.toString().eachLine( { line ->

    def cols = line.split( '\t' )
    int score = Integer.parseInt( cols[3] )
    if( score >= highestScore ) {
        highestScore = score
        def desc = cols[4].split( '   ' )
        String species = desc[0].substring( desc[0].indexOf( ' ' ) + 1 ).replaceAll( '"', '' ).replaceAll( ';', '' ).replaceAll( '\'', '' )
        def tmp = desc[1].replaceAll( '"', '' ).split( '=' )[1].split(';')
        def tax = []
        for( int i=2; i<tmp.length-1; i+=2 ) {
            tax << tmp[i]
        }
        /**
        * Test if there is a splitted taxon (e.g. genus Escherichia/Shigella)
        * -> split taxon and build 2 tax/lineage objects
        */
        boolean containsSplittedTaxon = false
        tax.each { if( it.contains('/') ) containsSplittedTaxon = true }
        if( containsSplittedTaxon ) {
            def taxA = []
            def taxB = []
            tax.each( {
                if( it.contains('/') ) {
                    def taxon = it.split( '/' )
                    taxA << taxon[0]
                    taxB << taxon[1]
                } else {
                    taxA << it
                    taxB << it
                }
            } )
            taxA << species
            taxB << species
            taxList << taxA
            taxList << taxB
        } else {
            tax << species
            taxList << tax
        }
    }

} )

// add extra 'species' taxon build from 'strain'
taxList.each( { tax ->
    int size = tax.size()
    def species = tax[ size - 1 ].replaceAll( '//s+', ' ' )
    def taxa = species.split( ' ' )
    if( taxa.size() > 2 ) {
        tax[ size - 1 ] = taxa[0] + ' ' + taxa[1]
        tax << species
    }
} )

// count abundances of lineages
def taxaCounts = [:]
taxList.collect( { it.join('-') } ).each( {
    if( taxaCounts.containsKey( it ) ) {
        taxaCounts[ it ] += 1
    } else {
        taxaCounts[ it ] = 1
    }
} )
info.rrna.lineages = taxList.toSet().collect( {
    [
        lineage: it,
        freq: taxaCounts[ it.join('-') ],
        classification: it.last()
    ]
} )
info.rrna.classification = info.rrna.lineages.sort( { -it.freq } )[0]
info.rrna.hits = taxList.size()



/********************
 *** Script Logic ***
 *** ANI (nucmer) ***
********************/

log.info( 'start ANI calculation against provided references' )
config.references.each( { ref ->

    String refName = ref.substring( 0, ref.lastIndexOf( '.' ) )
    Path referencePath = Paths.get( projectPath.toString(), PROJECT_PATH_REFERENCES, "${refName}.fasta" )
    log.info( "ANI reference=${refName}" )


    // build and write DNA fragemnts of length 1020
    log.debug( 'build dna-fragments.fasta file...' )
    int dnaFragmentIdx = 1
    def dnaFragments = []
    Path dnaFragmentsPath = tmpPath.resolve( 'dna-fragments.fasta' )
    Files.deleteIfExists( dnaFragmentsPath )
    Files.createFile( dnaFragmentsPath )
    m = genomeSequencePath.text =~ /(?m)^>.+$\r?\n([ATGCNatgcn\r\n]+)$/
    m.each( { match ->
        String sequence = match[1].replaceAll( '\n', '' )
        while( sequence.length() > (1020 + MIN_FRAGMENT_SIZE) ) {
            String dnaFragment = sequence.substring( 0, 1020 )
            dnaFragmentsPath << ">${dnaFragmentIdx}\n" + dnaFragment + '\n'
            dnaFragments << [
                id: dnaFragmentIdx,
                length: dnaFragment.length()
            ]
            sequence = sequence.substring( 1020 )
            dnaFragmentIdx++;
        }
        String dnaFragment = sequence
        dnaFragmentsPath << ">${dnaFragmentIdx}\n" + dnaFragment + '\n'
        dnaFragments << [
            id: dnaFragmentIdx,
            length: dnaFragment.length()
        ]
        dnaFragmentIdx++;
    } )

    // perform global alignments via nucmer
    log.debug( 'map contig fragments via nucmer...' )
    pb = new ProcessBuilder( NUCMER,
        referencePath.toString(),
        dnaFragmentsPath.toString() )
    .directory( tmpPath.toFile() )
    .redirectErrorStream( true )
    .redirectOutput( ProcessBuilder.Redirect.INHERIT )
    log.info( "exec: ${pb.command()}" )
    log.info( '----------------------------------------------------------------------------------------------' )
    if( pb.start().waitFor() != 0 ) terminate( 'could not exec nucmer!', taxPath, genomeName )
    log.info( '----------------------------------------------------------------------------------------------' )

    log.debug( 'filter hits via delta-filter...' )
    Path filterPath = tmpPath.resolve( 'out.filtered.delta' )
    pb = new ProcessBuilder( DELTA_FILTER,
        '-q',
        tmpPath.resolve( 'out.delta' ).toString() )
    .directory( tmpPath.toFile() )
    .redirectOutput( ProcessBuilder.Redirect.to( filterPath.toFile() ) )
    log.info( "exec: ${pb.command()}" )
    log.info( '----------------------------------------------------------------------------------------------' )
    if( pb.start().waitFor() != 0 ) terminate( 'could not exec nucmer!', taxPath, genomeName )
    log.info( '----------------------------------------------------------------------------------------------' )

    // parse nucmer output
    log.debug( 'parse nucmer output...' )
    def dnaFragment = null
    def dnaFragementMatches = []
    filterPath.text.eachLine( { line ->
        if( line.charAt(0) == '>' ) {
            def dnaFragmentId = line.split( ' ' )[1]
            dnaFragment = dnaFragments.find( { it.id == Integer.parseInt( dnaFragmentId ) } )
        } else if( dnaFragment != null ) {
            def cols = line.split( ' ' )
            if( cols.size() > 1 ) {
                def (rStart, rStop, qStart, qStop, noNonIdentities, noNonSimilarities, noStopCodons ) = cols
                dnaFragment.alignmentLength = Math.abs( Integer.parseInt(qStop) - Integer.parseInt(qStart) )
                dnaFragment.noNonIdentities = Integer.parseInt( noNonIdentities )
                dnaFragementMatches << dnaFragment
            }
        }
    } )

    // calc % conserved DNA
    log.debug( 'calc ANI...' )
    def alignmentSum = dnaFragementMatches.findAll( { ((it.alignmentLength-it.noNonIdentities)/it.alignmentLength) > 0.9 } )
        .collect( { it.alignmentLength } )
        .sum() ?: 0
    def genomeLength = dnaFragments.collect( { it.length } ).sum() ?: 0
    def conservedDNA = genomeLength > 0 ? alignmentSum/genomeLength : 0
    log.info( "conserved DNA: ${ conservedDNA*100 } %" )

    // calc average nucleotide identity
    def aniMatches = dnaFragementMatches.findAll( { (((it.length-it.noNonIdentities)/it.length) > 0.3 )  &&  ( (it.alignmentLength/it.length) >= 0.7) } )
    def niSum = aniMatches.collect( { (it.alignmentLength-it.noNonIdentities)/it.alignmentLength } ).sum() ?: 0
    def ani = aniMatches.size() > 0 ? niSum / aniMatches.size() : 0
    log.info( "ANI: ${ani*100} %" )

    info.ani.all << [
        reference: refName ,
        conservedDNA: conservedDNA,
        ani: ani
    ]

} )
info.ani.best = info.ani.all.sort( { -it.ani } )[0]




// cleanup
log.debug( 'delete tmp-dir' )
if( !tmpPath.deleteDir() ) terminate( "could not recursively delete tmp-dir=${tmpPath}", taxPath, genomeName )


// store info.json
info.time.end = (new Date()).format( DATE_FORMAT )
File infoJson = taxPath.resolve( "${genomeName}.json" ).toFile()
infoJson << JsonOutput.prettyPrint( JsonOutput.toJson( info ) )


// set state-file to finished
Files.move( taxPath.resolve( "${genomeName}.running" ), taxPath.resolve( "${genomeName}.finished" ) )




/**********************
 *** Script Methods ***
**********************/


private void terminate( String msg, Path taxPath, String genomeName ) {
    terminate( msg, null, taxPath, genomeName )
}

private void terminate( String msg, Throwable t, Path taxPath, String genomeName ) {

    if( t ) log.error( msg, t )
    else    log.error( msg )
    Files.move( taxPath.resolve( "${genomeName}.running" ), taxPath.resolve( "${genomeName}.failed" ) ) // set state-file to failed
    System.exit( 1 )

}
