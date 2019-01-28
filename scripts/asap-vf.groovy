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

BLASTP   = "${ASAP_HOME}/share/blast/bin/blastp"
PRODIGAL = "${ASAP_HOME}/share/prodigal"
VF_DB    = "${ASAP_HOME}/db/sequences/vfdb"
VF_CATEGORIES    = "${ASAP_HOME}/db/sequences/vfdb-categories.tsv"

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


// create local tmp directory
final Path tmpPath = Paths.get( '/', 'var', 'scratch', "tmp-${System.currentTimeMillis()}-${Math.round(Math.random()*1000)}" )
try { // create tmp dir
    log.info( "tmp-folder: ${tmpPath}" )
    Files.createDirectory( tmpPath )
} catch( Throwable t ) {
    terminate( "could create tmp dir! gid=${genomeId}, tmp-dir=${tmpPath}", t, vfPath, genomeName )
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


def vfdbCategories = [:]
Paths.get( VF_CATEGORIES ).eachLine( {
    def cols = it.split( '\t' )
    vfdbCategories[ cols[0] ] = [
        id: cols[1],
        name: cols[2]
    ]
} )


/********************
 *** Script Logic ***
********************/

// check (and create) a valid protein sequence fasta file (.faa)
final aaSequencePath
Path aaAnnotationPath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName, "${genomeName}.faa" )
Path genbankPath      = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName, "${genomeName}.gbk" )
Path sequencePath     = Paths.get( projectPath.toString(), PROJECT_PATH_SEQUENCES, "${genomeName}.fasta" )
if( Files.isReadable( aaAnnotationPath ) ) { // genome was annotated by ASA³P
    aaSequencePath = aaAnnotationPath
} else if( Files.isReadable( genbankPath ) ) { // user provided (converted) genbank file -> extract aa sequences
    aaSequencePath = tmpPath.resolve( "${genomeName}.faa" )
    String script = /
from Bio import SeqIO
fhInput  = open("${genbankPath}", "r")
fhOutput = open("${aaSequencePath}", "w")

for seq_record in SeqIO.parse(fhInput, "genbank") :
    print ("extract GenBank record %s" % seq_record.id)
    for seq_feature in seq_record.features :
        if seq_feature.type=="CDS" :
            assert len(seq_feature.qualifiers['translation'])==1
            fhOutput.write(">%s %s\n%s\n" % (
                   seq_feature.qualifiers['locus_tag'][0],
                   seq_feature.qualifiers['product'][0],
                   seq_feature.qualifiers['translation'][0]))

fhOutput.close()
fhInput.close()
/
    try { // start ebl -> fasta conversion process
        ProcessBuilder pb = new ProcessBuilder( '/usr/bin/env', 'python3',
            '-c', script )
            .redirectErrorStream( true )
            .redirectOutput( ProcessBuilder.Redirect.INHERIT )
            .directory( tmpPath.toFile() )
        log.info( "exec: ${pb.command()}" )
        log.info( '----------------------------------------------------------------------------------------------' )
        int exitCode = pb.start().waitFor()
        if( exitCode != 0 )  throw new IllegalStateException( "exitCode = ${exitCode}" )
        log.info( '----------------------------------------------------------------------------------------------' )
    } catch( Throwable t ) {
        log.error( 'genbank->fasta conversion failed!', t )
        println( 'genbank->fasta conversion failed!' )
        System.exit( 1 )
    }
} else if( Files.isReadable( sequencePath ) ) { // user provided (converted) gff file -> run prodigal, write aa sequences
    aaSequencePath = tmpPath.resolve( "${genomeName}.faa" )
    try { // start prodigal process
        ProcessBuilder pb = new ProcessBuilder( PRODIGAL,
            '-i', sequencePath.toString(),
            '-a', aaSequencePath.toString() )
            .redirectErrorStream( true )
            .redirectOutput( ProcessBuilder.Redirect.INHERIT )
            .directory( tmpPath.toFile() )
        log.info( "exec: ${pb.command()}" )
        log.info( '----------------------------------------------------------------------------------------------' )
        int exitCode = pb.start().waitFor()
        if( exitCode != 0 )  throw new IllegalStateException( "exitCode = ${exitCode}" )
        log.info( '----------------------------------------------------------------------------------------------' )
    } catch( Throwable t ) {
        log.error( 'prodigal AA sequence extraction failed!', t )
        println( 'prodigal AA sequence extraction failed!' )
        System.exit( 1 )
    }
} else {
    terminate( 'neither GenBank nor Fasta file found!', vfPath, genomeName )
}


// process
ProcessBuilder pb = new ProcessBuilder( BLASTP,
    '-query', aaSequencePath.toString(),
    '-db', VF_DB,
    '-num_threads', '1',
    '-culling_limit', '2',
    '-outfmt', '6 qseqid sseqid qcovs pident evalue bitscore stitle' )
log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
def proc = pb.start()
def stdOut = new StringBuilder( 100000 ), stdErr = new StringBuilder()
proc.consumeProcessOutput( stdOut, stdErr )
if( proc.waitFor() != 0 ) terminate( "could not exec blastp! stderr=${stdErr}", vfPath, genomeName )
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
        bitScore: cols[5],
        gene: titleCols[1],
        product: titleCols[2]
    ]
    if( hit.coverage >= 0.8  &&  hit.pIdent >= 0.9 ) {

        def category = vfdbCategories[ hit.dbId ]
        hit.catId = category.id ?: ''
        hit.catName = category.name ?: ''

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
if( !tmpPath.deleteDir() ) terminate( "could not recursively delete tmp-dir=${tmpPath}", vfPath, genomeName )


// store info.json
info.time.end = (new Date()).format( DATE_FORMAT )
File infoJson = vfPath.resolve( "${genomeName}.json" ).toFile()
infoJson << JsonOutput.prettyPrint( JsonOutput.toJson( info ) )


// set state-file to finished
Files.move( vfPath.resolve( "${genomeName}.running" ), vfPath.resolve( "${genomeName}.finished" ) )




/**********************
 *** Script Methods ***
**********************/


private void terminate( String msg, Path vfPath, String genomeName ) {
    terminate( msg, null, vfPath, genomeName )
}

private void terminate( String msg, Throwable t, Path vfPath, String genomeName ) {

    if( t ) log.error( msg, t )
    else    log.error( msg )
    Files.move( vfPath.resolve( "${genomeName}.running" ), vfPath.resolve( "${genomeName}.failed" ) ) // set state-file to failed
    System.exit( 1 )

}
