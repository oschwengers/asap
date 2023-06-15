// always invoke this script via $ASAP_HOME/bin/groovy

/**********************
 *** Script Imports ***
**********************/


import java.nio.file.*
import java.time.*
import groovy.util.CliBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.slf4j.*
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger

import static bio.comp.jlu.asap.api.Paths.*




/************************
 *** Script Constants ***
************************/
final def env = System.getenv()
ASAP_HOME = env.ASAP_HOME
ROARY = 'roary'
BLASTP  = "${ASAP_HOME}/share/blast/bin/blastp"
MAKEBLASTDB  = "${ASAP_HOME}/share/blast/bin/makeblastdb"

NUM_THREADS = Integer.toString( Runtime.getRuntime().availableProcessors() )




/*********************
 *** Script Params ***
*********************/


log = LoggerFactory.getLogger( getClass().getName() )

def cli = new CliBuilder( usage: 'asap-corepan.groovy --project-path <project-path>' )
    cli.p( longOpt: 'project-path', args: 1, argName: 'project-path', required: true, 'Path to ASAÂ³P project' )
def opts = cli.parse( args )
if( !opts?.p ) {
    log.error( 'no project path provided!' )
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
def config = (new JsonSlurper()).parseText( projectPath.resolve( 'config.json' ).toFile().text )


if( config.project.debugging ) { // set logging to debug upon user request
    ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger( org.slf4j.Logger.ROOT_LOGGER_NAME )
    rootLogger.setLevel( ch.qos.logback.classic.Level.DEBUG )
}


final Path corePanPath = projectPath.resolve( PROJECT_PATH_CORE_PAN )
Files.createFile( corePanPath.resolve( 'state.running' ) ) // create state.running


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
    path: tmpPath.toString(),
    corepan: [
        includedGenomes: [],
        excludedGenomes: [],
        plots: [:]
    ]
]




/********************
 *** Script Logic ***
********************/


// copy genome GFF3 files
Path localGenomePath = tmpPath.resolve( 'genomes' )
Files.createDirectory( localGenomePath )
config.genomes.each( { genome ->
    String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
    Path gffGenomePath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName, "${genomeName}.gff" )
    if( Files.exists( gffGenomePath ) ) {
        Files.copy( gffGenomePath, localGenomePath.resolve( "${genomeName}.gff" ) )
        info.corepan.includedGenomes << genomeName
    } else {
        log.warn( "skip genome id=${genome.id}, genome-name=${genomeName}" )
        info.corepan.excludedGenomes << genomeName
    }
} )
log.debug( "# genomes: ${config.genomes.size()}" )
log.debug( "# included: ${info.corepan.includedGenomes.size()}" )
log.debug( "# excluded: ${info.corepan.excludedGenomes.size()}" )


// build core/pan genome -> run Roary
pb = new ProcessBuilder( ROARY,
    '-p', NUM_THREADS,
    '-e',
    '-n', //fast nucleotide alignment with mafft
    '-cd', '100', // a gene must be present in ALL genes, not only in 99 % (Roary standard)
    '-g', '100000', // increase max # of allowed clusters to 100k (default 50k)
    '-b', BLASTP,
    '-m', MAKEBLASTDB,
    '-z') // don't delete intermediate files
    .redirectErrorStream( true )
    .redirectOutput( ProcessBuilder.Redirect.INHERIT )
    .directory( tmpPath.toFile() )

def cmd = pb.command()
info.corepan.includedGenomes.each( { cmd << "genomes/${it}.gff".toString() } )

log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
if( pb.start().waitFor() != 0 ) terminate( 'could not exec Roary!', corePanPath )
log.info( '----------------------------------------------------------------------------------------------' )


// calculate core/pan/singletons mean values
[
    [ 'pan', 'number_of_genes_in_pan_genome.Rtab' ],
    [ 'core', 'number_of_conserved_genes.Rtab' ],
    [ 'singletons', 'number_of_unique_genes.Rtab' ],
].each( { task, file ->
    def simulations = []
    tmpPath.resolve( file ).eachLine( { line -> simulations << line.split( '\t' ).collect( { it as Integer } ) } )
    int noGenomes = simulations[0].size() // update real # of genomes analyzed by Roary
    def means = []
    for( int i=0; i<noGenomes; i++ ) { // noGenomes = # included genomes - 1
        def tmp = []
        for( sim in simulations ){ tmp << sim[i] }
        double mean = tmp.sum() / tmp.size()
        means << mean
    }
    info.corepan.plots[ task ] = means
} )


// extract core/pan/singletons genes per genome
def locusGenomeMap = [:]
def genomeNames = []
def genomeGeneSets = [:]
def genes = []
boolean isFirstLine = true
tmpPath.resolve( 'gene_presence_absence.csv' ).eachLine( { line ->
    if( isFirstLine ) {
        isFirstLine = false
        def cols = line.split( ',' )
        genomeNames = cols[ 14..(cols.size()-1) ].collect( { it.replaceAll( '"', '' ) } )
        log.debug( "genome names: ${genomeNames}" )
    } else {
        line = line.substring( 1 )
        int emptyLastLine = 0
        while( line.endsWith( ',""' ) ) {
            line = line.substring( 0, line.length()-3 )
            emptyLastLine++
        }
        line = line.substring( 0, line.length()-1 )
        def cols = line.split( '","' ) as List
        for( int i=0; i<emptyLastLine; i++ ) { cols << '' }
        String name = cols[0]
        if( name ==~ /group/  &&  cols[1] ) // take 'Non-unique Gene name'
            name = cols[1]
        def gene = [
            name: name,
            product: cols[2],
            abundance: Integer.parseInt( cols[3].replaceAll('"', '') )
        ]
        genes << gene
        for( int i=14; i<cols.size(); i++ ) { // store gene availability for each genome
            log.debug( "gene: ${cols[i]}" )
            if( cols[ i ] ) { // if column is not empty, genome contains gene
                String genomeName = genomeNames[ i - 14 ]
                assert genomeName != null
                if( gene.abundance == 1 )
                    gene.source = genomeName
                if( genomeGeneSets.containsKey( genomeName ) ) {
                    genomeGeneSets[ (genomeName) ] << gene
                } else {
                    genomeGeneSets[ (genomeName) ] = [ gene ]
                }
            }
        }
    }
} )

assert info.corepan.includedGenomes.size() == genomeNames.size()
assert info.corepan.includedGenomes.size() == genomeGeneSets.size()


info.corepan.noGenomes = info.corepan.includedGenomes.size()
info.corepan.pan = genes
info.corepan.noPan = info.corepan.pan.size()
info.corepan.core = genes.findAll( { it.abundance == info.corepan.includedGenomes.size() } )
info.corepan.noCore = info.corepan.core.size()
info.corepan.accessory = genes.findAll( { it.abundance > 1  &&  it.abundance < info.corepan.includedGenomes.size() } )
info.corepan.noAccessory = info.corepan.accessory.size()
info.corepan.singletons = genes.findAll( { it.abundance == 1 } )
info.corepan.noSingletons = info.corepan.singletons.size()

info.corepan.plots[ 'geneAbundances' ] = genes.collect( { it.abundance } ).sort( { -it } )
log.info( "pan: ${info.corepan.pan.size()}" )
log.info( "core: ${info.corepan.core.size()}" )
log.info( "accessory: ${info.corepan.accessory.size()}" )
log.info( "singletons: ${info.corepan.singletons.size()}" )


// write genome specific core/pan/singleton gene information
log.debug( 'write genome specific core/pan/singleton gene information' )
info.corepan.includedGenomes.each( { genomeName ->
    def genome = config.genomes.find( { genomeName == "${config.project.genus}_${it.species}_${it.strain}" } )
    assert genome != null
    log.debug( "genomeName: ${genomeName}" )
    def genomeGeneSet = genomeGeneSets[ (genomeName) ]
    assert genomeGeneSet != null
    if( genomeGeneSet ) {
        def genomeInfo = [
            genome: [
                id: genome.id,
                species: genome.species,
                strain: genome.strain
            ],
            corepan: [
                noGenomes:  info.corepan.includedGenomes.size(),
                core:       genomeGeneSet.findAll( { it.abundance == info.corepan.includedGenomes.size() } ).collect( { [name: it.name, product: it.product] } ),
                accessory:  genomeGeneSet.findAll( { it.abundance > 1  &&  it.abundance < info.corepan.includedGenomes.size() } ),
                singletons: genomeGeneSet.findAll( { it.abundance == 1 } ).collect( { [name: it.name, product: it.product] } )
            ]
        ]
        File genomeInfoJson = corePanPath.resolve( "${genomeName}.json" ).toFile()
        genomeInfoJson << JsonOutput.prettyPrint( JsonOutput.toJson( genomeInfo ) )
    }
} )


// copy pan genome / maxtrix files
Files.copy( tmpPath.resolve( 'pan_genome_reference.fa' ), corePanPath.resolve( 'pan.fasta' ) )
Files.copy( tmpPath.resolve( 'gene_presence_absence.Rtab' ), corePanPath.resolve( 'pan-matrix.tsv' ) )


// extract core genome sequences
genes = [:]
def m = tmpPath.resolve( 'pan_genome_reference.fa').text =~ /(?m)^>(.+?)\s(.+)$\n([ATGCNatgcn\n]+)$/
m.each( { match ->
    String geneName = match[2]
    String sequence = match[3].toUpperCase()
    genes[ geneName ] = sequence
} )
StringBuilder sbCoreGenes = new StringBuilder( 10000000 )
isFirstLine = true
tmpPath.resolve( 'gene_presence_absence.Rtab' ).eachLine( { line ->
    if( isFirstLine )
        isFirstLine = false
    else {
        def cols = line.split( '\t' )
        String geneName = cols[0]
        if( !cols[1..-1].contains( '0' ) )
            sbCoreGenes.append( ">${geneName}\n${genes[ geneName ]}\n" )
    }
} )
corePanPath.resolve( 'core.fasta' ).text = sbCoreGenes.toString()


// store info.json
info.time.end = OffsetDateTime.now().toString()
File infoJson = corePanPath.resolve( 'info.json' ).toFile()
infoJson << JsonOutput.prettyPrint( JsonOutput.toJson( info ) )


// set state-file to finished
Files.move( corePanPath.resolve( 'state.running' ), corePanPath.resolve( 'state.finished' ) )




/**********************
 *** Script Methods ***
**********************/


private void terminate( String msg, Path analysisPath ) {
    terminate( msg, null, analysisPath )
}

private void terminate( String msg, Throwable t, Path analysisPath ) {

    if( t ) log.error( msg, t )
    else    log.error( msg )
    Files.move( analysisPath.resolve( 'state.running' ), analysisPath.resolve( 'state.failed' ) ) // set state-file to failed
    System.exit( 1 )

}
