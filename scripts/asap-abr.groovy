// always invoke this script via $ASAP_HOME/bin/groovy

/**********************
 *** Script Imports ***
**********************/


import java.nio.file.*
import java.time.*
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

CARD             = "${ASAP_HOME}/share/card"
SINGULARITY_CARD = "${CARD}/card.simg"

PERC_SEQ_IDENT = 0.4



/*********************
 *** Script Params ***
*********************/


log = LoggerFactory.getLogger( getClass().getName() )

def cli = new CliBuilder( usage: 'asap-abr.groovy --project-path <project-path> --genome-id <genome-id>' )
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


final Path abrPath = projectPath.resolve( PROJECT_PATH_ABR )
Files.createFile( abrPath.resolve( "${genomeName}.running" ) ) // create state.running


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
    terminate( "no sequence file! gid=${genomeId}", abrPath, genomeName )


// create local tmp directory
final Path tmpPath = Paths.get( '/', 'var', 'scratch', "tmp-${System.currentTimeMillis()}-${Math.round(Math.random()*1000)}" )
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
    path: abrPath.toString(),
    abr: [:]
]




/********************
 *** Script Logic ***
********************/


// parse CARD aro terms
def aroTerms = (new JsonSlurper()).parseText( Paths.get( "${CARD}/aro.json" ).text )


// process
Path localGenomeSequencePath = tmpPath.resolve("${genomeName}.fasta")
Files.copy( genomeSequencePath, localGenomeSequencePath )
String cardOutput = 'card'
Path cardOutputPath = tmpPath.resolve( "${cardOutput}.json" )
ProcessBuilder pb = new ProcessBuilder( 'singularity',
    'exec',
    '--no-home',
    '--bind', tmpPath.toString(), // mount tmp (cwd) directory
    SINGULARITY_CARD.toString(), // path to Singularity container file
    'rgi', 'main',
    '--input_type', 'contig',
    '--num_threads', '1',
    '--input_sequence', localGenomeSequencePath.toString(),
    '--output_file', cardOutput,
    '--exclude_nudge' )
    .redirectErrorStream( true )
    .redirectOutput( ProcessBuilder.Redirect.INHERIT )
    .directory( tmpPath.toFile() )

log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
if( pb.start().waitFor() != 0 ) terminate( 'could not exec CARD rgi!', abrPath, genomeName )
log.info( '----------------------------------------------------------------------------------------------' )


// parse and aggregate CARD output
def p = ~/([a-zA-Z]+) antibiotic/
def abrs = [:]
(new JsonSlurper()).parseText( cardOutputPath.text ).each( { cardHit ->
    if( !cardHit.value[ 'data_type' ] ) {
        cardHit.value.values().findAll( { !it?.nudged } ) // skip "nudged" CARD entries
        .each( { hsp ->
            def tmp = hsp.orf_from.split('_')
            tmp = tmp[0..-2]
            String contig = tmp.join('_')
            def abr = [
                model: [
                    aroId: hsp.ARO_accession,
                    name: hsp.model_name,
                    desc: aroTerms.find( { it.accession.split(':')[1] == hsp.ARO_accession } ).description ?: '-',
                    type: hsp.model_type,
                    bitScore: hsp.pass_bitscore
                ],
                eValue: hsp.evalue,
                bitScore: hsp[ 'bit_score' ],
                percentSeqIdentity: hsp.perc_identity / 100,
                alignment: hsp.match,
                orf: [
                    contig: contig,
                    strand: hsp.orf_strand,
                    start: hsp.orf_start,
                    end: hsp.orf_end,
                    length: Math.abs( hsp.orf_start - hsp.orf_end ) + 1
                ],
                antibiotics: [],
                drugClasses: []
            ]
            if( hsp.snp ) {
                abr.snp = hsp.snp
            }
            hsp.ARO_category.each( {
                def aroCat = it.value
                if( aroCat.category_aro_class_name == 'Drug Class' ) {
                    abr.drugClasses << aroCat.category_aro_name - ' antibiotic'
                } else if( aroCat.category_aro_class_name == 'Antibiotic' ) {
                    abr.antibiotics << aroCat.category_aro_name
                }

            } )
            abr.antibiotics = abr.antibiotics.toUnique().sort()
            abr.drugClasses = abr.drugClasses.toUnique().sort()
            if( abr.percentSeqIdentity > PERC_SEQ_IDENT ) {
                if( abrs.containsKey( abr.orf.start ) ) {
                    abrs[ (abr.orf.start) ] << abr
                } else {
                    abrs[ (abr.orf.start) ] = [ abr ]
                }
            }
        } )
    }
} )


// extract perfect hits
def perfectHits = []
def additionalHits = []
abrs.each( { k, v ->
    def perfectHit = v.find( { it.percentSeqIdentity == 1d } )
    if( perfectHit ) {
        perfectHits << perfectHit
    } else {
        v.each( { additionalHits << it } )
    }
} )


// sort hits lists
info.abr.perfect = perfectHits.sort( { it.orf.start } )
info.abr.additional = additionalHits.sort( { a, b -> a.orf.start <=> b.orf.start ?: b.percentSeqIdentity <=> a.percentSeqIdentity ?: a.eValue <=> b.eValue } )


// store info.json
info.time.end = OffsetDateTime.now().toString()
File infoJson = abrPath.resolve( "${genomeName}.json" ).toFile()
infoJson << JsonOutput.prettyPrint( JsonOutput.toJson( info ) )


// set state-file to finished
Files.move( abrPath.resolve( "${genomeName}.running" ), abrPath.resolve( "${genomeName}.finished" ) )




/**********************
 *** Script Methods ***
**********************/


private void terminate( String msg, Path abrPath, String genomeName ) {
    terminate( msg, null, abrPath, genomeName )
}

private void terminate( String msg, Throwable t, Path abrPath, String genomeName ) {

    if( t ) log.error( msg, t )
    else    log.error( msg )
    Files.move( abrPath.resolve( "${genomeName}.running" ), abrPath.resolve( "${genomeName}.failed" ) ) // set state-file to failed
    System.exit( 1 )

}
