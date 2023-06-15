// always invoke this script via $ASAP_HOME/bin/groovy

/**********************
 *** Script Imports ***
**********************/


import java.nio.file.*
import java.time.*
import java.util.zip.GZIPInputStream
import groovy.json.*
import groovy.util.CliBuilder
import org.slf4j.*
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import bio.comp.jlu.asap.api.DataType
import bio.comp.jlu.asap.api.FileType

import static bio.comp.jlu.asap.api.GenomeSteps.*
import static bio.comp.jlu.asap.api.Paths.*




/************************
 *** Script Constants ***
************************/
final def env = System.getenv()
ASAP_HOME = env.ASAP_HOME

MASH         = "${ASAP_HOME}/share/mash"
NUCMER       = "${ASAP_HOME}/share/mummer/nucmer"
DELTA_FILTER = "${ASAP_HOME}/share/mummer/delta-filter"
BLASTN       = "${ASAP_HOME}/share/blast/bin/blastn"
CMSEARCH     = "${ASAP_HOME}/share/infernal/cmsearch"

REFSEQ_DB    = "${ASAP_HOME}/db/refseq"
SILVA_DB  = "${ASAP_HOME}/db/silva/silva-bacteria"
RFAM_CM_SSU_RRNA = "${ASAP_HOME}/db/RF00177.cm"

MIN_FRAGMENT_SIZE = 100

int noCores = Runtime.getRuntime().availableProcessors()
NUM_THREADS = noCores < 4 ? Integer.toString( noCores ) : '4'


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
    log.error( "Error: project directory (${rawProjectPath}) does not exist!" )
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


if( config.project.debugging ) { // set logging to debug upon user request
    ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger( org.slf4j.Logger.ROOT_LOGGER_NAME )
    rootLogger.setLevel( ch.qos.logback.classic.Level.DEBUG )
}


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
final Path tmpPath = Paths.get( config.runtime.tmp, "tmp-${System.currentTimeMillis()}-${Math.round(Math.random()*1000)}" )
try { // create tmp dir
    log.info( "tmp-folder: ${tmpPath}" )
    Files.createDirectory( tmpPath )
    if( !config.project.debugging ) {
        addShutdownHook( {
            try {
                tmpPath.deleteDir()
                // cleanup
                log.debug( 'delete tmp-dir' )
            } catch( IOException ex ) {
                log.error( "could not recursively delete tmp-dir=${tmpPath}", ex )
            }
        } )
    }
} catch( Throwable t ) {
    terminate( "could create tmp dir! gid=${genomeId}, tmp-dir=${tmpPath}", t, taxPath, genomeName )
}


// create info object
def info = [
    time: [
        start: OffsetDateTime.now().toString()
    ],
    genome: [
        id: genome.id,
        species: genome.species,
        strain: genome.strain
    ],
    path: taxPath.toString(),
    kmer: [
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
 *** kmer / ANI (Mash/MUMmer) **
********************/

log.info( 'start kmer species classification via Mash' )
ProcessBuilder pb = new ProcessBuilder( MASH,
    'dist',
    '-p', NUM_THREADS,
    '-d', '0.1',
    "${REFSEQ_DB}/db.msh".toString(),
    genomeSequencePath.toString() )
    .directory( tmpPath.toFile() )
    log.info( "exec: ${pb.command()}" )
    log.info( '----------------------------------------------------------------------------------------------' )
    proc = pb.start()
    stdOut = new StringBuilder()
    stdErr = new StringBuilder()
    proc.consumeProcessOutput( stdOut, stdErr )
    if( proc.waitFor() != 0 ){
        log.error( stdErr.toString() )
        terminate( 'could not exec mash!', taxPath, genomeName )
    }
    log.info( '----------------------------------------------------------------------------------------------' )


def hits = []
stdOut.toString().eachLine( { line ->
    log.debug( "mash-hit: ${line}" )
    def cols = line.split( '\t' )
    hits << [
        'accessionId': cols[0],
        'dist': cols[2] as float,
        'isClassifier': false  // only one hit can be selected as the kmer classifier hit
    ]
} )
hits = hits.sort( { it.dist } )
if( hits.size() > 10 )
    hits = hits[0..9]


// load taxonomic information & annotate result hits
Paths.get("${REFSEQ_DB}/db.tsv").eachLine( { line ->
    def cols = line.split( '\t' )
    String accessionId = cols[0]
    String taxonId = cols[1]
    String species = cols[2]
    def result = hits.find( { it.accessionId == accessionId } )
    if( result ) {
        result[ 'taxonId' ] = taxonId
        result[ 'classification' ] = species
    }
} )


log.info('start ANI calculation against RefSeq genomes')
// build DNA fragemnts
final def genomeFragmentObject = createDnaFragments( genomeSequencePath, tmpPath )

hits.each( { hit ->
    Path referenceDbPath = Paths.get( REFSEQ_DB, "${hit.accessionId}.fna.gz")
    Path referencePath = tmpPath.resolve( "${hit.accessionId}.fna" )
    decompressAndCopy( referenceDbPath, referencePath )
    def ani = computeANI( genomeFragmentObject, referencePath, tmpPath )
    hit << ani
    hit['isSpecies'] = ( ani.ani >= 0.95  &&  ani.conservedDNA >= 0.69 ) ? true : false
} )
hits.sort( { -it.ani * it.conservedDNA } )
info.kmer.hits = hits

speciesHits = hits.findAll( { it.isSpecies } ).sort( { -it.ani * it.conservedDNA } )
if( speciesHits.size() > 0) {
    speciesHit = speciesHits[0]
    speciesHit.isClassifier = true
    info.kmer.classification = speciesHit
} else {
    info.kmer.classification = [:]
}




/********************
 *** Script Logic ***
 *** 16S (infernal/blast) ***
********************/

log.info( 'start 16S rRNA classification via blastn vs SILVA db' )
Path cmOutPath = tmpPath.resolve( 'cm.out' )
pb = new ProcessBuilder( CMSEARCH,
    '--rfam',
    '--noali',
    '--cpu', NUM_THREADS,
    '--tblout', cmOutPath.toString(),
    RFAM_CM_SSU_RRNA,
    genomeSequencePath.toString() )
    .directory( tmpPath.toFile() )
log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
if( pb.start().waitFor() != 0 ) terminate( 'could not exec cmsearch!', taxPath, genomeName )
log.info( '----------------------------------------------------------------------------------------------' )

def ssuContigMapList = [:]
cmOutPath.eachLine( { line ->
    if( line.charAt( 0 ) != '#' ) {
        def cols = line.split( '\\s+' )
        def ssu = [
            contig: cols[0],
            start: cols[7] as int,
            end: cols[8] as int,
            score: cols[14] as float,
            complete: true ? cols[10] == 'no' : false // trunc(ated) 'no' -> complete
        ]
        if( ssu.start > ssu.end ) { // swap start/end if on '-' strand
            int tmp = ssu.start
            ssu.start = ssu.end
            ssu.end = tmp
        }
        if( ssu.complete  &&  ssu.score >= 1000.0f ) {
            if( ssuContigMapList.containsKey(ssu.contig) )
                ssuContigMapList[ ssu.contig ] << ssu
            else
                ssuContigMapList[ ssu.contig ] = [ ssu ]
            log.info( "detected valid 16S rRNA: contig=${ssu.contig}, start=${ssu.start}, end=${ssu.end}, length=${ssu.end-ssu.start+1}, score=${ssu.score}, complete=${ssu.complete}" )
        } else {
            log.debug( "discard unvalid 16S rRNA: contig=${ssu.contig}, start=${ssu.start}, end=${ssu.end}, length=${ssu.end-ssu.start+1}, score=${ssu.score}, complete=${ssu.complete}" )
        }
    }
} )
def ssus = ssuContigMapList.values().flatten()
log.debug("ssus raw: ${ssus.size()}")


if( ssus.size() > 0 ) {
    ssus.each( {
        log.debug("ssu: contig=${it.contig}, start=${it.start}, end=${it.end}, length=${it.end-it.start+1}, score=${it.score}, complete=${it.complete}")
    } )


    def m = genomeSequencePath.text =~ /(?m)^>(.+)$\r?\n([ATGCNatgcn\r\n]+)$/ //include Windows line breaks (\r\n) as user provided scaffolds might be written on Windows systems
    m.each( { match ->
        String contig = match[1].split(' ')[0].trim()
        def ssuList = ssuContigMapList[ contig ]
        if( ssuList != null ) {
            String sequence = match[2].replaceAll( '[^ATGCNatgcn]', '' )
            ssuList.each( { it
                it.sequence = sequence.substring( it.start - 1, it.end )
                log.info( "extracted 16S rRNA: contig=${contig}, seq=${ it.sequence.substring( 0, 50 ) }..." )
            } )
        }
    } )
    log.debug("ssus full: ${ssus.size()}")
    ssus.each( {
        log.debug("ssu: contig=${it.contig}, start=${it.start}, end=${it.end}, length=${it.end-it.start+1}, score=${it.score}, complete=${it.complete}, seq=${it.sequence}")
    } )


    // remove 100 % sequence duplicates
    ssuContigMapList = [:]
    ssuSequences = new HashSet()
    ssus.each( {
        if( !ssuSequences.contains( it.sequence ) ) {
            if( ssuContigMapList.containsKey( it.contig ) )
                ssuContigMapList[ it.contig ] << it
            else
                ssuContigMapList[ it.contig ] = [ it ]
            log.debug("ssu-added: contig=${it.contig}, start=${it.start}, end=${it.end}, length=${it.end-it.start+1}, score=${it.score}, complete=${it.complete}, seq=${it.sequence}")
            ssuSequences << it.sequence
        }
    } )
    ssus = ssuContigMapList.values().flatten()
    log.info("ssus w/o dups: # ${ssus.size()}")
    ssus.each( {
        log.debug("ssu: contig=${it.contig}, start=${it.start}, end=${it.end}, length=${it.end-it.start+1}, score=${it.score}, complete=${it.complete}, seq=${it.sequence}")
    })


    Path seq16SPath = tmpPath.resolve( '16S.fasta' )
    seq16SPath << '' // if no 16S sequence was found Blast will still run though, without results
    ssus.each( {
        seq16SPath << ">${it.contig}-|-${it.start}-|-${it.end}\n"
        seq16SPath << "${it.sequence}\n"
    } )


    // blast genome vs SILVA db
    pb = new ProcessBuilder( BLASTN,
        '-query', seq16SPath.toString(),
        '-db', SILVA_DB,
        '-num_threads', NUM_THREADS,
        '-evalue', '1E-10',
        '-outfmt', '6 qseqid sseqid length nident bitscore stitle' )
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


    float highestScore = 0.0f
    taxList = []
    stdOut.toString().eachLine( { line ->
        def cols = line.split( '\t' )
        def headerCols = cols[0].split('-\\|-')
        def hit = [
            contig: headerCols[0],
            contigStart: headerCols[1] as int,
            contigEnd: headerCols[2] as int,
            id: cols[1],
            alignmentLength: cols[2] as int,
            nIdent: cols[3]as int,
            bitscore: cols[4] as float,
            desc: cols[5]
        ]
        log.debug( "raw hit: ${hit}" )
        String rRNASequence = ssus.find( { it.contig == hit.contig  &&  it.start == hit.contigStart  &&  it.end == hit.contigEnd } )?.sequence
        if( rRNASequence != null
            &&  (hit.alignmentLength / rRNASequence.length()) >= 0.8
            &&  (hit.nIdent / hit.alignmentLength) >= 0.8
            &&  hit.bitscore >= highestScore ) {
            log.debug( "valid hit: ${hit}" )
            highestScore = hit.bitscore
            def tax = hit.desc.split(';')
            tax[0] = tax[0].split(' ')[1]
            taxList << tax
        }
    } )

    // count abundances of lineages
    def lineageBins = [:]
    taxList.each( {
        def key = it.join('-')
        if( lineageBins.containsKey( key ) ) {
            def lineage = lineageBins.get( key )
            lineage.freq += 1
        } else {
            lineageBins[ key ] = [
                lineage: it,
                freq: 1,
                classification: it.last()
            ]
        }
    } )
    info.rrna.lineages = lineageBins.values().sort( { -it.freq } )
    info.rrna.classification = info.rrna.lineages[0]
    info.rrna.hits = taxList.size()
} else {
    log.warn( "no valid 16S ssu sequence detected!" )
    info.rrna.lineages = []
    info.rrna.classification = null
    info.rrna.hits = 0
}


/********************
 *** Script Logic ***
 *** ANI (nucmer) ***
********************/

log.info( 'start ANI calculation against provided references' )
config.references.each( { ref ->

    String refName = ref.substring( 0, ref.lastIndexOf( '.' ) )
    Path referencePath = Paths.get( projectPath.toString(), PROJECT_PATH_REFERENCES, "${refName}.fasta" )
    log.info( "ANI reference=${refName}" )

    // compute ANI & conservedDNA
    def ani = computeANI( genomeFragmentObject, referencePath, tmpPath )
    ani.reference = refName
    info.ani.all << ani

} )
info.ani.best = info.ani.all.sort( { -it.ani } )[0]


// store info.json
info.time.end = OffsetDateTime.now().toString()
File infoJson = taxPath.resolve( "${genomeName}.json" ).toFile()
infoJson << JsonOutput.prettyPrint( JsonOutput.toJson( info ) )


// set state-file to finished
Files.move( taxPath.resolve( "${genomeName}.running" ), taxPath.resolve( "${genomeName}.finished" ) )




/**********************
 *** Script Methods ***
**********************/


private def createDnaFragments( Path genomeSequencePath, Path tmpPath ) {

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

    return [
        dnaFragmentsPath: dnaFragmentsPath,
        dnaFragments: dnaFragments
    ]
}


private def computeANI( def genomeFragmentObject, Path referencePath, Path tmpPath ) {

    // perform global alignments via nucmer
    log.debug( 'map contig fragments via nucmer...' )
    ProcessBuilder pb = new ProcessBuilder( NUCMER,
        "--threads=${NUM_THREADS}".toString(),
        referencePath.toString(),
        genomeFragmentObject.dnaFragmentsPath.toString() )
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
                dnaFragment = genomeFragmentObject.dnaFragments.find( { it.id == Integer.parseInt( dnaFragmentId ) } )
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
    def genomeLength = genomeFragmentObject.dnaFragments.collect( { it.length } ).sum() ?: 0
    def conservedDNA = genomeLength > 0 ? alignmentSum/genomeLength : 0
    log.info( "conserved DNA: ${ conservedDNA*100 } %" )

    // calc average nucleotide identity
    def aniMatches = dnaFragementMatches.findAll( { (((it.length-it.noNonIdentities)/it.length) > 0.3 )  &&  ( (it.alignmentLength/it.length) >= 0.7) } )
    def niSum = aniMatches.collect( { (it.alignmentLength-it.noNonIdentities)/it.alignmentLength } ).sum() ?: 0
    def ani = aniMatches.size() > 0 ? niSum / aniMatches.size() : 0
    log.info( "ANI: ${ani*100} %" )

    return [
        conservedDNA: conservedDNA,
        ani: ani
    ]
}


private void decompressAndCopy( Path inputPath, Path outputPath ) {

    try {
        BufferedReader bi = new BufferedReader( new InputStreamReader( GZIPInputStream gzis = new GZIPInputStream( Files.newInputStream( inputPath ) ) ) )
        BufferedWriter bo = Files.newBufferedWriter( outputPath )

        int len
        char[] buffer = new char[1024]
        while( (len = bi.read( buffer, 0, 1024 ) ) > 0 ) {
            bo.write( buffer, 0, len )
        }

        bi.close()
        bo.close()
    } catch( Exception ex ) {
        throw ex
    }
}


private void terminate( String msg, Path taxPath, String genomeName ) {
    terminate( msg, null, taxPath, genomeName )
}

private void terminate( String msg, Throwable t, Path taxPath, String genomeName ) {

    if( t ) log.error( msg, t )
    else    log.error( msg )
    Files.move( taxPath.resolve( "${genomeName}.running" ), taxPath.resolve( "${genomeName}.failed" ) ) // set state-file to failed
    System.exit( 1 )

}
