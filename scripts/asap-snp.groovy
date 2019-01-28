// always invoke this script via $ASAP_HOME/bin/groovy

/**********************
 *** Script Imports ***
**********************/


import java.nio.file.*
import groovy.util.CliBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.slf4j.LoggerFactory
import bio.comp.jlu.asap.api.DataType
import bio.comp.jlu.asap.api.FileType
import bio.comp.jlu.asap.api.FileFormat

import static bio.comp.jlu.asap.api.MiscConstants.*
import static bio.comp.jlu.asap.api.Paths.*




/************************
 *** Script Constants ***
************************/
final def env = System.getenv()
ASAP_HOME = env.ASAP_HOME

SAMTOOLS = "${ASAP_HOME}/share/samtools"
SNPEFF   = "java -jar ${ASAP_HOME}/share/snpeff/snpEff.jar"
SNPSIFT  = "java -jar ${ASAP_HOME}/share/snpeff/SnpSift.jar"
BGZIP    = "${SAMTOOLS}/bgzip"
TABIX    = "${SAMTOOLS}/tabix"




/*********************
 *** Script Params ***
*********************/


log = LoggerFactory.getLogger( getClass().getName() )

def cli = new CliBuilder( usage: 'asap-snp-detection-mpileup --project-path <project-path> --genome-id <genome-id>' )
    cli.p( longOpt: 'project-path', args: 1, argName: 'project-path', required: true, 'Path to ASAP project' )
    cli.g( longOpt: 'genome-id', args: 1, argName: 'genome-id', required: false, 'ID within the ASAP project' )
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
def config = (new JsonSlurper()).parseText( projectPath.resolve( 'config.json' ).text )


final def genome = config.genomes.find( { it.id == genomeId } )
if( !genome ) {
    log.error( "no genome found in config! genome-id=${genomeId}" )
    System.exit( 1 )
}
final String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
log.info( "genome-name: ${genomeName}")


final Path snpDetectionPath = projectPath.resolve( PROJECT_PATH_SNPS )
Files.createFile( snpDetectionPath.resolve( "${genomeName}.running" ) ) // create state.running


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
    path: snpDetectionPath.toString()
]




/********************
 *** Script Logic ***
********************/


// check for provided reads
def reads = null
genome.data.each( {
    if( FileType.getEnum( it.type )?.getDataType() == DataType.READS ) {
        reads = it
    }
} )
if( reads == null ) {
    log.warn( 'no reads provided!' )
    Files.move( snpDetectionPath.resolve( "${genomeName}.running" ), snpDetectionPath.resolve( "${genomeName}.skipped" ) ) // set state-file to failed
    System.exit( 0 )
}


// check if an annotated reference is provided
String refFileName = config.references[0]
FileFormat refType = FileFormat.getEnum( refFileName )
boolean hasReferenceAnnotation = false
if( refType == FileFormat.GENBANK  ||  refType == FileFormat.EMBL )
    hasReferenceAnnotation = true


// mpileup & call
String refName    = refFileName.substring( 0, refFileName.lastIndexOf( '.' ) )
Path fastaPath    = Paths.get( projectPath.toString(), PROJECT_PATH_REFERENCES, "${refName}.fasta" )
Path bamFilePath  = Paths.get( projectPath.toString(), PROJECT_PATH_MAPPINGS, "${genomeName}.bam" )
Path variantsPath = snpDetectionPath.resolve( "${genomeName}.vcf" )

ProcessBuilder pb
if( hasReferenceAnnotation ) {
    pb = new ProcessBuilder( 'sh', '-c',
    "${SAMTOOLS}/samtools mpileup -uRI -f ${fastaPath} ${bamFilePath} \
    | ${SAMTOOLS}/bcftools call --variants-only --skip-variants indels --output-type v --ploidy 1 -c  \
    | ${SNPSIFT} filter \"( QUAL >= 30 ) & (( na FILTER ) | (FILTER = 'PASS')) & ( DP >= 20 ) & ( MQ >= 20 )\" \
    | ${SNPEFF} ann -nodownload -no-intron -no-downstream -no SPLICE_SITE_REGION -upDownStreamLen 250 -config ${snpDetectionPath}/snpEff.config -csvStats ${genomeName}.csv ref - \
    > ${variantsPath}" )
} else {
    pb = new ProcessBuilder( 'sh', '-c',
    "${SAMTOOLS}/samtools mpileup -uRI -f ${fastaPath} ${bamFilePath} \
    | ${SAMTOOLS}/bcftools call --variants-only --skip-variants indels --output-type v --ploidy 1 -c  \
    | ${SNPSIFT} filter \"( QUAL >= 30 ) & (( na FILTER ) | (FILTER = 'PASS')) & ( DP >= 20 ) & ( MQ >= 20 )\" \
    > ${variantsPath}" )
}
pb.redirectErrorStream( true )
    .redirectOutput( ProcessBuilder.Redirect.INHERIT )
    .directory( snpDetectionPath.toFile() )
log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
if( pb.start().waitFor() != 0 ) terminate( 'could not exec samtools mpileup | bcftools call | snpSift filter (| snpEff)!', snpDetectionPath, genomeName )
log.info( '----------------------------------------------------------------------------------------------' )


// parse and store stats from snpEff
info.highImpactSNPs = []

info.impacts = [
    high: 0,
    moderate: 0,
    low: 0,
    modifier: 0
]
info.classes = [
    missense: 0,
    nonsense: 0,
    silent: 0
]
info.effects = [
    startLost: 0,
    stopGained: 0,
    stopLost: 0,
    synonymousVariant: 0
]
info.region = [
    downstream: 0,
    exon: 0,
    intergenic: 0,
    upstream: 0
]
info.baseChanges = [:]


if( hasReferenceAnnotation ) {
    variantsPath.eachLine( { line ->
        if( line.charAt(0) != '#' ) {
            (contig, pos, id, ref, alt, qual, filter, strInfo, rest) = line.split( '\t' )
            def infos = strInfo.split( ';' )
            def anns = infos.find( { it.contains( 'ANN' ) } )
            if( anns ) {
                anns.split( ',' ).each( { ann ->
                    //columns: variant, effect, impact, gene, rest...
                    def cols = ann.split( '\\|' )
                    if( cols[2] == 'HIGH' ) {
                        info.highImpactSNPs << [
                            contig: contig,
                            gene: cols[3] ? cols[3] : '-',
                            pos: pos as int,
                            ref: ref,
                            alt: alt,
                            cov: infos.find( { it.startsWith( 'DP=' ) } ).substring( 3 ),
                            meanQual: infos.find( { it.startsWith( 'MQ=' ) } ).substring( 3 ),
                            effect: cols[1].replaceAll( '_', ' ' ).split( '&' ).findAll( { it != 'splice region variant'} ).join( ', ' )
                        ]
                    }
                } )
            }
        }
    } )

    snpDetectionPath.resolve( "${genomeName}.csv" ).text.eachLine( { line ->
        if( line.startsWith( 'Number_of_effects' ) ) info.noEffects = line.split( ',' )[1] as int
        else if( line.startsWith( 'Change_rate' ) )  info.changeRate = line.split( ',' )[1] as int

        else if( line.startsWith( 'HIGH' ) )     info.impacts.high = line.split( ',' )[1].trim() as int
        else if( line.startsWith( 'LOW' ) )      info.impacts.low = line.split( ',' )[1].trim() as int
        else if( line.startsWith( 'MODERATE' ) ) info.impacts.moderate = line.split( ',' )[1].trim() as int
        else if( line.startsWith( 'MODIFIER' ) ) info.impacts.modifier = line.split( ',' )[1].trim() as int

        else if( line.startsWith( 'MISSENSE' ) ) info.classes.missense = line.split( ',' )[1].trim() as int
        else if( line.startsWith( 'NONSENSE' ) ) info.classes.nonsense = line.split( ',' )[1].trim() as int
        else if( line.startsWith( 'SILENT' ) )   info.classes.silent = line.split( ',' )[1].trim() as int

        else if( line.startsWith( 'start_lost' ) )         info.effects.startLost = line.split( ',' )[1].trim() as int
        else if( line.startsWith( 'stop_gained' ) )        info.effects.stopGained = line.split( ',' )[1].trim() as int
        else if( line.startsWith( 'stop_lost' ) )          info.effects.stopLost = line.split( ',' )[1].trim() as int
        else if( line.startsWith( 'synonymous_variant' ) ) info.effects.synonymousVariant = line.split( ',' )[1].trim() as int

        else if( line.startsWith( 'DOWNSTREAM' ) ) info.region.downstream = line.split( ',' )[1].trim() as int
        else if( line.startsWith( 'EXON' ) )       info.region.exon = line.split( ',' )[1].trim() as int
        else if( line.startsWith( 'INTERGENIC' ) ) info.region.intergenic = line.split( ',' )[1].trim() as int
        else if( line.startsWith( 'UPSTREAM' ) )   info.region.upstream = line.split( ',' )[1].trim() as int

        else if( line.startsWith( ' A  ' ) ) {
            ( id, a, c, g, t ) = line.replaceAll( ' ', '' ).split( ',' )
            info.baseChanges.A = [
                A: a as int,
                C: c as int,
                G: g as int,
                T: t as int
            ]
        } else if( line.startsWith( ' C  ' ) ) {
            ( id, a, c, g, t ) = line.replaceAll( ' ', '' ).split( ',' )
            info.baseChanges.C = [
                A: a as int,
                C: c as int,
                G: g as int,
                T: t as int
            ]
        }
        else if( line.startsWith( ' G  ' ) ) {
            ( id, a, c, g, t ) = line.replaceAll( ' ', '' ).split( ',' )
            info.baseChanges.G = [
                A: a as int,
                C: c as int,
                G: g as int,
                T: t as int
            ]
        }
        else if( line.startsWith( ' T  ' ) ) {
            ( id, a, c, g, t ) = line.replaceAll( ' ', '' ).split( ',' )
            info.baseChanges.T = [
                A: a as int,
                C: c as int,
                G: g as int,
                T: t as int
            ]
        }

    } )
}


// bgzip
pb = new ProcessBuilder(
    "${SAMTOOLS}/bgzip".toString(),
    variantsPath.toString() )
    .redirectErrorStream( true )
    .redirectOutput( ProcessBuilder.Redirect.INHERIT )
    .directory( snpDetectionPath.toFile() )
log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
if( pb.start().waitFor() != 0 ) terminate( 'could not exec bgzip!', snpDetectionPath, genomeName )
log.info( '----------------------------------------------------------------------------------------------' )


// tabix
Path zippedVariantsPath = snpDetectionPath.resolve( "${genomeName}.vcf.gz" )
pb = new ProcessBuilder(
    "${SAMTOOLS}/tabix".toString(),
    zippedVariantsPath.toString() )
    .redirectErrorStream( true )
    .redirectOutput( ProcessBuilder.Redirect.INHERIT )
    .directory( snpDetectionPath.toFile() )
log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
if( pb.start().waitFor() != 0 ) terminate( 'could not exec tabix!', snpDetectionPath, genomeName )
log.info( '----------------------------------------------------------------------------------------------' )


// bcftools consensus
String shortGenomeName = "${config.project.genus.toUpperCase().charAt(0)}_${genome.species}_${genome.strain}"
pb = new ProcessBuilder( 'sh', '-c',
    "${SAMTOOLS}/bcftools consensus --fasta-ref ${fastaPath} --haplotype 1 ${zippedVariantsPath} | sed 's/^>.*\$/>${shortGenomeName}/' > ${snpDetectionPath}/${genomeName}.consensus.fasta" )
    .redirectErrorStream( true )
    .redirectOutput( ProcessBuilder.Redirect.INHERIT )
    .directory( snpDetectionPath.toFile() )
log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
if( pb.start().waitFor() != 0 ) terminate( 'could not exec bcftools consensus!', snpDetectionPath, genomeName )
log.info( '----------------------------------------------------------------------------------------------' )


// bcftools stats
Path variantsStatsPath = snpDetectionPath.resolve( "${genomeName}.chk" )
pb = new ProcessBuilder( 'sh', '-c',
    "${SAMTOOLS}/bcftools stats ${zippedVariantsPath} > ${variantsStatsPath}" )
    .redirectErrorStream( true )
    .redirectOutput( ProcessBuilder.Redirect.INHERIT )
    .directory( snpDetectionPath.toFile() )
log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
if( pb.start().waitFor() != 0 ) terminate( 'could not exec bcftools stats!', snpDetectionPath, genomeName )
log.info( '----------------------------------------------------------------------------------------------' )


// parse and store stats from bcfstats
info.substitutions = []
info.snpCoverage = []
info.noSNPs = 0
String strSNPStats = variantsStatsPath.text
info.noSNPs = (strSNPStats =~ /(?m)^SN.+number of SNPs:\s+(\d+)$/)[0][1] as int
strSNPStats.eachLine( { line ->
    if( line.startsWith( 'TSTV' ) ) {
        (tstv, id, ts, tv, ratio, rest ) = line.split( '\t' )
        info.tstv = [
            ts: ts as int,
            tv: tv as int,
            tstv: ratio as double
        ]
    } else if( line.startsWith( 'DP' ) ) {
        (dp, id, bin, noGenotypes, foGenotypes,	noSites, foSites ) = line.split( '\t' )
        info.snpCoverage << [
            cov: (bin.charAt(0)=='>' ? bin.substring(1) : bin ) as int,
            count: noSites as int
        ]
    }
} )


if( !hasReferenceAnnotation ) { // calc change rate if not provided by SnpEff
    def fastaLines = fastaPath.text.split( '\n' ).drop( 1 )
    info.changeRate = fastaLines.join( '' ).length() / info.noSNPs
}


// store info.json
info.time.end = (new Date()).format( DATE_FORMAT )
File infoJson = snpDetectionPath.resolve( "${genomeName}.json" ).toFile()
infoJson << JsonOutput.prettyPrint( JsonOutput.toJson( info ) )


// set state-file to finished
Files.move( snpDetectionPath.resolve( "${genomeName}.running" ), snpDetectionPath.resolve( "${genomeName}.finished" ) )




/**********************
 *** Script Methods ***
**********************/


private void terminate( String msg, Path snpDetectionPath, String genomeName ) {
    terminate( msg, null, snpDetectionPath, genomeName )
}

private void terminate( String msg, Throwable t, Path snpDetectionPath, String genomeName ) {

    if( t ) log.error( msg, t )
    else    log.error( msg )
    Files.move( snpDetectionPath.resolve( "${genomeName}.running" ), snpDetectionPath.resolve( "${genomeName}.failed" ) ) // set state-file to failed
    System.exit( 1 )

}
