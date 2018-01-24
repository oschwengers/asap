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

BLASTP   = "${ASAP_HOME}/share/blast/bin/blastp"
VF_DB    = "${ASAP_DB}/sequences/vfdb.faa"

PERC_SEQ_IDENT = '0.9'



/*********************
 *** Script Params ***
*********************/


log = LoggerFactory.getLogger( getClass().getName() )

def cli = new CliBuilder( usage: 'asap-vf.groovy --project-path <project-path> --genome-id <genome-id>' )
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


//final Path vfPath = projectPath.resolve( PROJECT_PATH_VF )
final Path vfPath = projectPath.resolve( 'vf' )
Files.createFile( vfPath.resolve( "${genomeName}.running" ) ) // create state.running

// polished genome path
final Path annotationGenomePath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName, "${genomeName}.faa" )

// create local tmp directory
final Path tmpPath = Paths.get( '/', 'var', 'scratch', "tmp-${System.currentTimeMillis()}-${Math.round(Math.random()*1000)}" )
try { // create tmp dir
    log.info( "tmp-folder: ${tmpPath}" )
    Files.createDirectory( tmpPath )
} catch( Throwable t ) {
    terminate( "could create tmp dir! gid=${genomeId}, tmp-dir=${tmpPath}", t, genomeName, vfPath, tmpPath )
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
    path: vfPath.toString(),
    vf: []
]




/********************
 *** Script Logic ***
********************/


// process
ProcessBuilder pb = new ProcessBuilder( BLASTP,
    '-query', annotationGenomePath.toString(),
    '-db', VF_DB,
    '-num_threads', '1',
    '-culling_limit', '2',
    '-outfmt', '6 qseqid sseqid qcovs pident evalue bitscore stitle' )
log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
def proc = pb.start()
def stdOut = new StringBuilder( 100000 ), stdErr = new StringBuilder()
proc.consumeProcessOutput( stdOut, stdErr )
if( proc.waitFor() != 0 ) terminate( "could not exec blastp! stderr=${stdErr}", genomeName, vfPath, tmpPath )
log.info( '----------------------------------------------------------------------------------------------' )


//  parse blastp output
def p = ~/(.+?) \[(.+?)\].*/
def blastHits = [:]
stdOut.eachLine( { line ->

    def cols = line.split( '\t' )
    def titleCols = cols[6].split( '~~~' )
    def hit = [
        locus: cols[0],
        dbId: cols[1],
        coverage: (cols[2] as double) / 100,
        pIdent: (cols[3] as double) / 100,
        eValue: cols[4],
        bitScore: cols[5]
    ]
    String rawDesc
    if( titleCols.size() > 1 ) {
        hit.ec   = titleCols[0]
        hit.gene = titleCols[1]
        rawDesc  = titleCols[2]
    } else {
        hit.ec   = ''
        hit.gene = ''
        rawDesc  = cols[6]
    }
    if( hit.coverage >= 0.8  &&  hit.pIdent >= 0.9 ) {

        def m = (rawDesc =~ p)
        if( m ) {
            hit.product = m[0][1]
            hit.category = m[0][2]
        } else
            hit.product = titleCols[2]

        def altHit = blastHits[ hit.locus ]
        if( !altHit )
            blastHits[ hit.locus ] = hit
        else if( hit.bitScore > altHit.bitScore )
            blastHits[ hit.locus ] = hit

    }

} )


// sort hits
info.vf = blastHits.values().sort( { it.bitScore } )


// cleanup
log.debug( 'delete tmp-dir' )
if( !tmpPath.deleteDir() ) terminate( "could not recursively delete tmp-dir=${tmpPath}", genomeName, vfPath, tmpPath )


// store info.json
info.time.end = (new Date()).format( DATE_FORMAT )
File infoJson = vfPath.resolve( "${genomeName}.json" ).toFile()
infoJson << JsonOutput.prettyPrint( JsonOutput.toJson( info ) )


// set state-file to finished
Files.move( vfPath.resolve( "${genomeName}.running" ), vfPath.resolve( "${genomeName}.finished" ) )




/**********************
 *** Script Methods ***
**********************/


private void terminate( String msg, String genomeName, Path vfPath, Path tmpPath ) {
    terminate( msg, null, genomeName, vfPath, tmpPath )
}

private void terminate( String msg, Throwable t, String genomeName, Path vfPath, Path tmpPath ) {

    if( t ) log.error( msg, t )
    else    log.error( msg )
    Files.move( vfPath.resolve( "${genomeName}.running" ), vfPath.resolve( "${genomeName}.failed" ) ) // set state-file to failed
    tmpPath.deleteDir() // cleanup tmp dir
    log.debug( "removed tmp-dir: ${tmpPath}" )
    System.exit( 1 )

}
