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

import static groovy.io.FileType.FILES
import static bio.comp.jlu.asap.api.GenomeSteps.*
import static bio.comp.jlu.asap.api.MiscConstants.*
import static bio.comp.jlu.asap.api.Paths.*




/************************
 *** Script Constants ***
************************/
final def env = System.getenv()
ASAP_HOME = env.ASAP_HOME
ASAP_DB   = env.ASAP_DB

MLST_DB = "${ASAP_DB}/mlst"
BLASTN  = "${ASAP_HOME}/share/blast/bin/blastn"


/*********************
 *** Script Params ***
*********************/


log = LoggerFactory.getLogger( getClass().getName() )

def cli = new CliBuilder( usage: 'asap-mlst.groovy --project-path <project-path> --genome-id <genome-id>' )
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


final Path mlstPath = projectPath.resolve( PROJECT_PATH_MLST )
Files.createFile( mlstPath.resolve( "${genomeName}.running" ) ) // create state.running


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
    terminate( "no sequence file! gid=${genomeId}, tmp-dir=${tmpPath}", genomeName, taxPath, tmpPath )


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
    path: mlstPath.toString(),
    mlst: [
        perfect: [],
        related: []
    ]
]




/********************
 *** Script Logic ***
********************/


// blast genome vs mlst db
ProcessBuilder pb = new ProcessBuilder( BLASTN,
    '-query', genomeSequencePath.toString(),
    '-db', "${MLST_DB}/mlst.fna".toString(),
    '-num_threads', '1',
    '-ungapped',
    '-dust', 'no',
    '-evalue', '1E-20',
    '-word_size', '32',
    '-max_target_seqs', '10000',
    '-culling_limit', '2',
    '-perc_identity', '95',
    '-outfmt', '6 sseqid slen length nident' )
    .directory( mlstPath.toFile() )
log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
def proc = pb.start()
def stdOut = new StringBuilder(), stdErr = new StringBuilder()
proc.consumeProcessOutput( stdOut, stdErr )
if( proc.waitFor() != 0 ) terminate( "could not exec blastn! stderr=${stdErr}", genomeName, mlstPath )
log.info( '----------------------------------------------------------------------------------------------' )


//  parse blastn output
def blastHits = [:]
stdOut.eachLine( { line ->
    def blastCols = (line =~ /^(\w+)\.(\w+)[_-](\d+)\t(\d+)\t(\d+)\t(\d+)/)[0]
    def bh = [
        scheme : blastCols[1],
        gene : blastCols[2],
        allele : blastCols[3],
        geneLength : Integer.parseInt( blastCols[4] ),
        alignmentLength : Integer.parseInt( blastCols[5] ),
        identities : Integer.parseInt( blastCols[6] )
    ]
    bh.mismatches = bh.geneLength - bh.identities
    if( bh.identities / bh.geneLength > 0.95 ) { // discard all hits below 95 % subject identity
        String key = "${bh.scheme}-${bh.gene}"
        def tmpBH = blastHits[ (key) ]
        if( !tmpBH )
            blastHits[ (key) ] = bh
        else if( bh.alignmentLength > tmpBH.alignmentLength  ||  bh.identities > tmpBH.identities )
            blastHits[ (key) ] = bh
    }
} )
blastHits = blastHits.values()

if( blastHits.size() > 1 ) {

    // read mlst-db in dense JSON format
    def stProfiles = (new JsonSlurper()).parseText( Paths.get( MLST_DB, 'mlst-db.json' ).text )

    // find ST profile hits
    def foundSTProfiles = []
    stProfiles.each( { p -> // find ST matches
        boolean wrongAllele = false
        boolean missedGene  = false
        p.mismatches = []
        for( gene in p.alleles.keySet() ) {
            def bh = blastHits.find( { it.scheme == p.scheme  &&  it.gene == gene } )
            if( bh  &&  bh.allele == p.alleles[ gene ] ) {
                if( bh.mismatches > 0 ) p.mismatches << gene
            } else if ( bh ) wrongAllele = true
            else             missedGene  = true
        }
        if( !missedGene  &&  !wrongAllele )
            foundSTProfiles << p
    } )

    // sort ST
    foundSTProfiles = foundSTProfiles.sort( { a, b ->
        a.mismatches.size() <=> b.mismatches.size()
    } )

    for( p in foundSTProfiles ) {
        if( p.mismatches )
            info.mlst.related << p
        else {
            p.remove( 'mismatches' )
            info.mlst.perfect << p
        }
    }
}


// store info.json
info.time.end = (new Date()).format( DATE_FORMAT )
File infoJson = mlstPath.resolve( "${genomeName}.json" ).toFile()
infoJson << JsonOutput.prettyPrint( JsonOutput.toJson( info ) )


// set state-file to finished
Files.move( mlstPath.resolve( "${genomeName}.running" ), mlstPath.resolve( "${genomeName}.finished" ) )




/**********************
 *** Script Methods ***
**********************/


private void terminate( String msg, String genomeName, Path mlstPath ) {
    terminate( msg, null, genomeName, mlstPath )
}

private void terminate( String msg, Throwable t, String genomeName, Path mlstPath ) {

    if( t ) log.error( msg, t )
    else    log.error( msg )
    Files.move( mlstPath.resolve( "${genomeName}.running" ), mlstPath.resolve( "${genomeName}.failed" ) ) // set state-file to failed
    System.exit( 1 )

}
