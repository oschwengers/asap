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

FASTQC          = 'fastqc'
FASTP           = 'fastp'
FILTLONG        = 'filtlong'
FASTQ_SCREEN    = 'fastq_screen'

int noCores = Runtime.getRuntime().availableProcessors()
NUM_THREADS = noCores < 8 ? Integer.toString( noCores ) : '8'



/*********************
 *** Script Params ***
*********************/


log = LoggerFactory.getLogger( getClass().getName() )

def cli = new CliBuilder( usage: 'asap-qc.groovy --project-path <project-path> --genome-id <genome-id>' )
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


final Path genomeRawReadsPath = Paths.get( projectPath.toString(), PROJECT_PATH_READS_RAW, genomeName )
final Path genomeQCReadsPath = Paths.get( projectPath.toString(), PROJECT_PATH_READS_QC, genomeName )
try { // create tmp dir
    if( !Files.exists( genomeQCReadsPath ) ) {
        Files.createDirectory( genomeQCReadsPath )
        log.info( "create genome-filtered-reads folder: ${genomeQCReadsPath}" )
    }
} catch( Throwable t ) {
    log.error( "could not create genome filtered read dir! gid=${genomeId}, filtered-read-dir=${genomeQCReadsPath}" )
    System.exit( 1 )
}
Files.createFile( genomeQCReadsPath.resolve( 'state.running' ) ) // create state.running


// create local tmp directory
final Path tmpPath = Paths.get( config.runtime.tmp, "tmp-${System.currentTimeMillis()}-${Math.round(Math.random()*1000)}" )
final Path tmpTrimmedPath = tmpPath.resolve( 'trimmed' )
try { // create tmp dir
    log.info( "tmp-folder: ${tmpPath}" )
    Files.createDirectory( tmpPath )
    log.debug( "create tmp-trimmed folder: ${tmpTrimmedPath}" )
    Files.createDirectory( tmpTrimmedPath )
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
    terminate( "could not create tmp dir! gid=${genomeId}, tmp-dir=${tmpPath}", t, genomeQCReadsPath )
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
    path: genomeQCReadsPath.toString(),
    rawReads: [],
    qcReads: [],
    contaminations: [
        references: [:]
    ]

]




/********************
 *** Script Logic ***
********************/


// process reads
genome.data.each( { datum ->

    log.debug( "process data: type=${datum.type}" )

    final FileType ft = FileType.getEnum( datum.type )
    if( ft?.getDataType() == DataType.READS ) {
        
        datum.files.each( {
            info.rawReads << runFastQC( genomeRawReadsPath.resolve( it ), tmpPath, genomeRawReadsPath )
        } )

        int l = info.rawReads.size()
        info.rawReadsAvg = [
            encoding: info.rawReads*.encoding,
            gc: info.rawReads*.gc.sum() / l,
            noReads: info.rawReads*.noReads.sum(),
            readLengths: [
                min: info.rawReads*.readLengths.min.min(),
                mean: info.rawReads*.readLengths.mean.sum() / l,
                max: info.rawReads*.readLengths.max.max()
            ],
            qual: [
                min: info.rawReads*.qual.min.min(),
                mean: info.rawReads*.qual.mean.sum() / l,
                max: info.rawReads*.qual.max.max()
            ]
        ]


        // trim reads
        if( ft == FileType.READS_ILLUMINA_SINGLE ) {
            def fileName = datum.files[0]
            log.info( "trim single reads: file=${fileName}" )
            // exec fastp -> adapter, qual
            Path tmpFilePath = tmpPath.resolve( 'read.tmp' )
            String illuminaEncoding = info.rawReads[0].encoding
            ProcessBuilder pb = new ProcessBuilder( FASTP,
                '--thread', NUM_THREADS,
                genomeRawReadsPath.resolve( fileName ).toString(),
                tmpFilePath.toString(),
                )
                .redirectErrorStream( true )
                .redirectOutput( ProcessBuilder.Redirect.INHERIT )
            log.info( "exec: ${pb.command()}" )
            log.info( '----------------------------------------------------------------------------------------------' )
            if( pb.start().waitFor() != 0 ) terminate( 'could not exec fastp!', genomeQCReadsPath )
            log.info( '----------------------------------------------------------------------------------------------' )
        } else if( ft == FileType.READS_ILLUMINA_PAIRED_END ) {
            log.info( "trim paired reads: file=${datum.files}" )
            // exec fastp -> adapter, qual
            Path tmpFile1Path = tmpPath.resolve( 'read-1.tmp' )
            Path tmpFile2Path = tmpPath.resolve( 'read-2.tmp' )
            ProcessBuilder pb = new ProcessBuilder( FASTP,
                '--thread', NUM_THREADS,
                genomeRawReadsPath.resolve( datum.files[0] ).toString(),
                genomeRawReadsPath.resolve( datum.files[1] ).toString(),
                )
                .redirectErrorStream( true )
                .redirectOutput( ProcessBuilder.Redirect.INHERIT )
            log.info( "exec: ${pb.command()}" )
            log.info( '----------------------------------------------------------------------------------------------' )
            if( pb.start().waitFor() != 0 ) terminate( 'could not exec trimmomatic (adapter)!', genomeQCReadsPath )
            log.info( '----------------------------------------------------------------------------------------------' )
        } else if( ft == FileType.READS_NANOPORE ) {

            log.info( "trim ONT reads: file=${datum.files}" )
            // exec Filtlong -> length, qual
            ProcessBuilder pb = new ProcessBuilder( 'sh', '-c',
                "${FILTLONG} --min_length 500 --min_mean_q 85 --min_window_q 65 ${genomeRawReadsPath.resolve( datum.files[0] )} | gzip > ${tmpTrimmedPath.resolve( datum.files[0] )}" )
                .redirectErrorStream( true )
                .redirectOutput( ProcessBuilder.Redirect.INHERIT )
                .directory( tmpPath.toFile() )
                log.info( "exec: ${pb.command()}" )
                log.info( '----------------------------------------------------------------------------------------------' )
                if( pb.start().waitFor() != 0 ) terminate( 'could not exec filtlong|gzip!', genomeQCReadsPath )
                log.info( '----------------------------------------------------------------------------------------------' )

        } else if( ft == FileType.READS_PACBIO ) {

            log.info( "trim PB reads: file=${datum.files}" )
            // exec Filtlong -> length, qual
            ProcessBuilder pb = new ProcessBuilder( 'sh', '-c',
                "${FILTLONG} --min_length 500 --min_mean_q 85 --min_window_q 65 ${genomeRawReadsPath.resolve( datum.files[0] )} | gzip > ${tmpTrimmedPath.resolve( datum.files[0] )}" )
                .redirectErrorStream( true )
                .redirectOutput( ProcessBuilder.Redirect.INHERIT )
                .directory( tmpPath.toFile() )
                log.info( "exec: ${pb.command()}" )
                log.info( '----------------------------------------------------------------------------------------------' )
                if( pb.start().waitFor() != 0 ) terminate( 'could not exec filtlong|gzip!', genomeQCReadsPath )
                log.info( '----------------------------------------------------------------------------------------------' )

        } else {

            log.warn( 'no trimming for non-Illumina/ONT/PacBio reads!' )
            datum.files.each( {
                Files.createSymbolicLink( tmpTrimmedPath.resolve( it ), genomeRawReadsPath.resolve( it ) )
            } )

        }




        // copy trimmed read files to reads_qc folder
        datum.files.each( {
            Path localQCReadFilePath = tmpTrimmedPath.resolve( it )
            Path genomeQCReadFilePath = genomeQCReadsPath.resolve( it )
            log.info( "copy: ${localQCReadFilePath} -> ${genomeQCReadFilePath}" )
            Files.copy( localQCReadFilePath, genomeQCReadFilePath )
        } )




        // assess qc reads quality
        datum.files.each( {
            info.qcReads << runFastQC( tmpTrimmedPath.resolve( it ), tmpPath, genomeQCReadsPath )
        } )

        l = info.qcReads.size()
        info.qcReadsAvg = [
            encoding: info.qcReads*.encoding,
            gc: info.qcReads*.gc.sum() / l,
            noReads: info.qcReads*.noReads.sum(),
            readLengths: [
                min: info.qcReads*.readLengths.min.min(),
                mean: info.qcReads*.readLengths.mean.sum() / l,
                max: info.qcReads*.readLengths.max.max()
            ],
            qual: [
                min: info.qcReads*.qual.min.min(),
                mean: info.qcReads*.qual.mean.sum() / l,
                max: info.qcReads*.qual.max.max()
            ]
        ]




        // create FastQ Screen conf file
        String script = /
sed "s,%ASAP_HOME%,${ASAP_HOME},g" ${FASTQ_SCREEN}\/fastq_screen.conf.template > fastq_screen.conf
/
        ProcessBuilder pb = new ProcessBuilder( 'sh',
            '-c', script )
            .directory( tmpPath.toFile() )
        log.info( "exec: ${pb.command()}" )
        log.info( '----------------------------------------------------------------------------------------------' )
        if( pb.start().waitFor() != 0 ) terminate( "could not create FastQ Screen conf! conf-template=${localQCReadFilePath}, output-dir=${tmpTrimmedPath}", genomeQCReadsPath )
        log.info( '----------------------------------------------------------------------------------------------' )


        // create merged read files
        Path localQCReadFilePath = tmpTrimmedPath.resolve( datum.files[0] )
        if( ft == FileType.READS_ILLUMINA_PAIRED_END ) { // only use forward files
            localQCReadFilePath = tmpTrimmedPath.resolve( datum.files[0] )
        }


        // run FastQ Screen
        pb = new ProcessBuilder( "${FASTQ_SCREEN}/fastq_screen".toString(),
            '--aligner', 'bwa',
            '--conf', "${tmpPath}/fastq_screen.conf",
            '--threads', NUM_THREADS,
            '--outdir', tmpTrimmedPath.toString() )
            .redirectErrorStream( true )
            .redirectOutput( ProcessBuilder.Redirect.INHERIT )
        def cmd = pb.command()
        if( ft == FileType.READS_PACBIO  ||  ft == FileType.READS_NANOPORE) {
            cmd << '--subset'
            cmd << '1000'
        }
        cmd << localQCReadFilePath.toString()
        log.debug( "exec: ${pb.command()}" )
        log.info( '----------------------------------------------------------------------------------------------' )
        if( pb.start().waitFor() != 0 ) terminate( "could not exec FastQ Screen! read-file=${localQCReadFilePath}, output-dir=${tmpTrimmedPath}", genomeQCReadsPath )
        log.info( '----------------------------------------------------------------------------------------------' )

        // parse FastQ Screen output
        String fileName = localQCReadFilePath.toFile().name.replaceAll( '.gz', '' ).replaceAll( '.fastq', '' )
        tmpTrimmedPath.resolve( "${fileName}_screen.txt" ).eachLine( { line ->
            if( !line.isEmpty() ) {
                char first = line.charAt(0)
                if( first != '#'  &&  first != '%'  &&  !line.startsWith( 'Genome' ) ) {
                    (ref, noReads, noUnMapped, rest ) = line.split( '\t' )
                    def refStats = info.contaminations.references[ (ref) ]
                    if( refStats ) {
                        refStats.noReads += noReads as int
                        refStats.noUnMapped += noUnMapped as int
                    } else {
                        refStats = [
                            name: ref,
                            noReads: noReads as int,
                            noUnMapped: noUnMapped as int
                        ]
                        info.contaminations.references[ (ref) ] = refStats
                    }
                }
            }
        } )

        int noReads = 0
        int sumPotentialContaminations = 0
        info.contaminations.references.each( { k,v ->
            v.noPotentialContaminations = v.noReads - v.noUnMapped
            noReads = v.noReads
            sumPotentialContaminations += v.noPotentialContaminations
        } )
        info.contaminations.potentialContaminations = (float) sumPotentialContaminations / noReads

    }

} )


// store info.json
info.time.end = OffsetDateTime.now().toString()
File infoJson = genomeQCReadsPath.resolve( 'info.json' ).toFile()
infoJson << JsonOutput.prettyPrint( JsonOutput.toJson( info ) )


// set state-file to finished
Files.move( genomeQCReadsPath.resolve( 'state.running' ), genomeQCReadsPath.resolve( 'state.finished' ) )




/**********************
 *** Script Methods ***
**********************/


private void terminate( String msg, Path genomePath ) {
    terminate( msg, null, genomePath )
}

private void terminate( String msg, Throwable t, Path genomePath ) {

    if( t ) log.error( msg, t )
    else    log.error( msg )
    Files.move( genomePath.resolve( 'state.running' ), genomePath.resolve( 'state.failed' ) ) // set state-file to failed
    System.exit( 1 )

}


private def runFastQC( Path readsPath, Path tmpPath, Path destinationPath ) {

    log.info( "check read: file=${readsPath}" )
    ProcessBuilder pb = new ProcessBuilder( FASTQC,
        "--outdir=${tmpPath}",
        '--quiet',
        '--extract',
        '--threads', NUM_THREADS,
        readsPath.toString() )
    .redirectErrorStream( true )
    .redirectOutput( ProcessBuilder.Redirect.INHERIT )
    log.info( '----------------------------------------------------------------------------------------------' )
    log.info( "exec: ${pb.command()}" )
    log.info( '----------------------------------------------------------------------------------------------' )
    if( pb.start().waitFor() != 0 ) terminate( "could not exec FastQC! read-file=${readsPath}, output-dir=${tmpPath}", genomeQCReadsPath )

    // parse FastQC summary and copy files
    String fileName = readsPath.toFile().name
    [
        '.gz',
        '.fq',
        '.fastq'
    ].each( {
        fileName = fileName.replace( it, '' )
    } )
    Path readFolderPath = destinationPath.resolve( fileName )
    Files.createDirectory( readFolderPath )
    Path fastQCZipFolderPath = tmpPath.resolve( "${fileName}_fastqc" )
    [
        'per_base_quality.png',
        'per_base_n_content.png',
        'per_base_sequence_content.png',
        'per_sequence_quality.png',
        'per_sequence_gc_content.png',
        'sequence_length_distribution.png'
    ].each( {
        try { Files.move( fastQCZipFolderPath.resolve( 'Images' ).resolve( it ), readFolderPath.resolve( it ) ) }
        catch( NoSuchFileException nsfe ) { log.warn( "file not available: ${it}" ) }
    } )
    def stats = parseFastQCStats( fastQCZipFolderPath.resolve( 'fastqc_data.txt' ) )
    stats.file = readsPath.toFile().name
    stats.fileName = fileName

    return stats

}


private def parseFastQCStats( Path fastQCDataPath ) {

    log.debug( "parse ${fastQCDataPath}" )
    def stats = [:]

    String data = fastQCDataPath.toFile().text

    // get file encoding
    String encoding = ''
    if( data =~ /(?m)^Encoding\s+?Sanger.+?$/ ) encoding = '33'
    else if( data =~ /(?m)^Encoding\s+?Illumina.*?$/ ) encoding = '64'
    log.debug( "encoding: ${encoding}" )
    stats.encoding = encoding.toInteger()

    // get # sequences
    def m = data =~ /(?m)^Total Sequences\s+?(\d+)$/
    log.debug( "total seq: " + m[0][1] )
    stats.noReads = m[0][1].toInteger()

    // get sequence length
    m = data =~ /(?m)^Sequence length\s+?([\d-]+)$/
    log.debug( "seq len:" + m[0][1] )
    def lengths = m[0][1].split( '-' )
    stats.readLengths = [
        min: lengths[0].toInteger(),
        max: lengths[0].toInteger()
    ]
    if( lengths.size() > 1 )
        stats.readLengths.max = lengths[1].toInteger()

    // get %GC
    m = data =~ /(?m)^%GC\s+?(\d+)$/
    log.debug( "gc:" + m[0][1] )
    stats.gc = m[0][1].toInteger()

    m = data =~ /(?ms)^>>Per base sequence quality\s+(?:pass|warn|fail)\n#.+?\n(.+?)^>>END_MODULE$/
    if( m && m[0][1] ) {
        def qualMeans = [:]
        m[0][1].eachLine( {
            def cols = it.split( '\t' )
            String basePos = cols[0]
            if( basePos.contains('-') ) qualMeans << [ (basePos.split('-')[0]): cols[1] ]
            else qualMeans << [ (basePos): cols[1] ]
        } )
        def quals = qualMeans.values().collect( {it as double} )
        stats.qual = [
            min: quals.min().toInteger(),
            max: quals.max().toInteger(),
            mean: (quals.sum() as double) / quals.size(),
            means: qualMeans
        ]
    }

    m = data =~ /(?ms)^>>Sequence Length Distribution\s+(?:pass|warn|fail)\n#.+?\n(.+?)^>>END_MODULE$/
    if( m && m[0][1] ) {
        def lengthDist = [:]
        m[0][1].eachLine( {
            def cols = it.split( '\t' )
            String seqLength = cols[0]
            if( seqLength.contains('-') ){
                def seqLengths = seqLength.split('-')
                def meanLength = ((seqLengths[0] as int) + (seqLengths[1] as int)) / 2
                lengthDist << [ (meanLength): cols[1] as double ]
            } else lengthDist << [ (seqLength as int): cols[1] as double ]
        } )
        stats.readLengths.mean = (lengthDist.collect( { k,v -> k * v } ).sum() as double) / lengthDist.values().sum()
        stats.readLengths.dist = lengthDist.toSorted( { a,b -> a.getKey() <=> b.getKey() } )
    }

    return stats

}
