// always invoke this script via $ASAP_HOME/bin/groovy

/**********************
 *** Script Imports ***
**********************/


import java.nio.file.*
import java.util.regex.Pattern
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.CliBuilder
import org.slf4j.LoggerFactory
import bio.comp.jlu.asap.api.DataType
import bio.comp.jlu.asap.api.FileType

import static bio.comp.jlu.asap.api.MiscConstants.*
import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.GenomeSteps.*




/************************
 *** Script Constants ***
************************/
final def env = System.getenv()
ASAP_HOME = env.ASAP_HOME

SPADES        = "${ASAP_HOME}/share/spades/bin/spades.py"
SMRTLINK      = "${ASAP_HOME}/share/smrtlink"
DATASET       = "${SMRTLINK}/smrtcmds/bin/dataset"
BAM2FASTQ     = "${SMRTLINK}/smrtcmds/bin/bam2fastq"
HGAP4         = "${SMRTLINK}/smrtcmds/bin/pbsmrtpipe"
UNICYCLER     = "${ASAP_HOME}/share/unicycler"
RACON         = "${ASAP_HOME}/share/racon"
MAKEBLASTDB   = "${ASAP_HOME}/share/blast/bin/makeblastdb"
TBLASTN       = "${ASAP_HOME}/share/blast/bin/tblastn"
SAMTOOLS      = "${ASAP_HOME}/share/samtools/samtools"
BOWTIE2       = "${ASAP_HOME}/share/bowtie2/bowtie2"
BOWTIE2_BUILD = "${ASAP_HOME}/share/bowtie2/bowtie2-build"
PILON         = "${ASAP_HOME}/share/pilon.jar"
MINIMAP2      = "${ASAP_HOME}/share/minimap2"

SAMTOOLS_SORT_MEM = '1G' // max-ram usage until tmp-file is created during sorting (optimum 4G for avg. files)
NUM_THREADS       = '8'


/*********************
 *** Script Params ***
*********************/


log = LoggerFactory.getLogger( getClass().getName() )

def cli = new CliBuilder( usage: 'asap-assembly.groovy --project-path <project-path> --genome-id <genome-id>' )
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
final def config = (new JsonSlurper()).parseText( projectPath.resolve( 'config.json' ).toFile().text )

final def genome = config.genomes.find( { it.id == genomeId } )
if( !genome ) {
    log.error( "no genome found in config! genome-id=${genomeId}" )
    System.exit( 1 )
}
final String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
log.info( "genome-name: ${genomeName}")


// get paths / create dirs of filtered reads and assembled contigs
final Path genomeContigsPath = Paths.get( projectPath.toString(), PROJECT_PATH_ASSEMBLIES, genomeName )
final Path genomeQCReadsPath = Paths.get( projectPath.toString(), PROJECT_PATH_READS_QC, genomeName )
if( !Files.exists( genomeContigsPath ) ) {
    try {
        Files.createDirectory( genomeContigsPath )
        log.info( "create genome-contigs folder: ${genomeContigsPath}" )
    } catch( Throwable t ) {
        log.error( "could create genome contigs dir! gid=${genomeId}, contigs-dir=${genomeContigsPath}" )
        System.exit( 1 )
    }
}


// create local tmp dir
Path tmpPath = Paths.get( '/', 'var', 'scratch', "tmp-${System.currentTimeMillis()}-${Math.round(Math.random()*1000)}" )
try { // create tmp dir
    log.info( "tmp-folder: ${tmpPath}" )
    Files.createDirectory( tmpPath )
} catch( Throwable t ) {
    terminate( "could create tmp dir! gid=${genomeId}, tmp-dir=${tmpPath}", t, genomeContigsPath )
}


// create state.running
Files.createFile( genomeContigsPath.resolve( 'state.running' ) )




/********************
 *** Script Logic ***
********************/


// cp read files to local tmp
genome.data.each( { datum ->
    FileType ft = FileType.getEnum( datum.type )
    if( ft?.getDataType() == DataType.READS ) {
        if( ft == FileType.READS_PACBIO_RSII  ||  ft == FileType.READS_PACBIO_SEQUEL ) {
            try{
                Path srcFile  = genomeQCReadsPath.resolve( "${genomeName}.subreads.bam" )
                Path destFile = tmpPath.resolve( "${genomeName}.subreads.bam" )
                log.info( "copy: ${srcFile} -> ${destFile}" )
                Files.copy( srcFile, destFile )

                srcFile  = genomeQCReadsPath.resolve( "${genomeName}.subreads.bam.pbi" )
                destFile = tmpPath.resolve( "${genomeName}.subreads.bam.pbi" )
                log.info( "copy: ${srcFile} -> ${destFile}" )
                Files.copy( srcFile, destFile )
            } catch( Exception ex ) {
                terminate( "Failed to copy file!", ex, genomeContigsPath )
            }
        } else {
            datum.files.each( { file ->
                try{
                    Path srcFile  = genomeQCReadsPath.resolve( file )
                    Path destFile = tmpPath.resolve( file )
                    log.info( "copy: ${srcFile} -> ${destFile}" )
                    Files.copy( srcFile, destFile )
                } catch( Exception ex ) {
                    terminate( "Failed to copy file! src=${srcFile}, dest=${destFile}", ex, genomeContigsPath )
                }
            } )
        }
    }
} )


// setup assembly info map
def info = [
    time: [
        start: (new Date()).format( DATE_FORMAT )
    ],
    genome: [
        id: genome.id,
        species: genome.species,
        strain: genome.strain
    ],
    path: genomeContigsPath.toString(),
    contigs: []
]


/** Determine which assembler to use
  *  - only Illumina reads -> SPAdes
  *  - only PacBio reads -> HGap
  *  - only ONT reads -> Unicycler
  *  - ONT/PacBio + Illumina reads -> Unicycler
**/
def fileTypes = genome.data*.type
log.info( "provided file types: ${fileTypes}" )
if( (FileType.READS_NANOPORE.toString() in fileTypes)  &&  (FileType.READS_ILLUMINA_PAIRED_END.toString() in fileTypes)) {

    // ONT + Illumina reads -> Unicycler
    log.info( 'detected ONT & Illumina PE reads -> run Unicycler' )
    runUnicycler( config, genome, genomeContigsPath, tmpPath, info )

} else if( ((FileType.READS_PACBIO_RSII.toString() in fileTypes)  ||  (FileType.READS_PACBIO_SEQUEL.toString() in fileTypes))  &&  (FileType.READS_ILLUMINA_PAIRED_END.toString() in fileTypes)) {

    // PacBio + Illumina reads -> Unicycler
    log.info( 'detected PacBio & Illumina PE reads -> run Unicycler' )
    runUnicycler( config, genome, genomeContigsPath, tmpPath, info )

} else if( FileType.READS_NANOPORE.toString() in fileTypes ) {

    // ONT only reads -> Unicycler
    log.info( 'detected ONT reads -> run Unicycler' )
    runUnicycler( config, genome, genomeContigsPath, tmpPath, info )

} else if( (FileType.READS_PACBIO_RSII.toString() in fileTypes)  ||  (FileType.READS_PACBIO_SEQUEL.toString() in fileTypes) ) {

    // all reads in bax.h5 or bam file format -> use HGap
    log.info( 'detected PacBio reads -> run HGap' )
    runHGap( config, genome, genomeContigsPath, tmpPath, info )

} else if( FileType.READS_ILLUMINA_PAIRED_END.toString() in fileTypes  ||  FileType.READS_ILLUMINA_SINGLE.toString() in fileTypes  ||  FileType.READS_SANGER.toString() in fileTypes ) {

    // only short reads -> SPAdes
    log.info( 'detected Illumina PE reads -> run SPAdes' )
    runSpades( config, genome, genomeContigsPath, tmpPath, info )

} else {

    /**
     * What else ?
     */
    terminate( "wrong sequencing file constallation. Use either only PacBio/ONT/Illumina reads or long with short reads!", genomeContigsPath )

}


// store info.json
info.time.end = (new Date()).format( DATE_FORMAT )
File infoJson = genomeContigsPath.resolve( 'info.json' ).toFile()
infoJson << JsonOutput.prettyPrint( JsonOutput.toJson( info ) )


// cleanup
log.debug( 'delete tmp-dir' )
if( !tmpPath.deleteDir() ) terminate( "could not recursively delete tmp-dir=${tmpPath}", genomeContigsPath )

// set state-file to finished
Files.move( genomeContigsPath.resolve( 'state.running' ), genomeContigsPath.resolve( 'state.finished' ) )




/**********************
 *** Script Methods ***
**********************/

private void runUnicycler( def config, def genome, Path genomeContigsPath, Path tmpPath, def info ) {

    try {

        final Path longReadsPath
        FileType ft = FileType.getEnum( genome.data[0].type )
        if( ft == FileType.READS_PACBIO_SEQUEL  ||  ft == FileType.READS_PACBIO_RSII ) {
            // convert PacBio bam to fastq
            pb = new ProcessBuilder( BAM2FASTQ,
                '-o', "${genomeName}",
                '-u',
                tmpPath.resolve( "${genomeName}.subreads.bam" ).toString() )
            .directory( tmpPath.toFile() )
            log.info( "exec: ${pb.command()}" )
            log.info( '----------------------------------------------------------------------------------------------' )
            if( pb.start().waitFor() != 0 ) terminate( 'could not exec bam2fastq!', genomeContigsPath )
            log.info( '----------------------------------------------------------------------------------------------' )
            longReadsPath = tmpPath.resolve( "${genomeName}.fastq" )
        } else {
            longReadsPath = tmpPath.resolve( genome.data[0].files[0] )
        }


        // run Unicycler
        ProcessBuilder pb = new ProcessBuilder( UNICYCLER.toString(),
            '--threads', NUM_THREADS,
            '--mode', 'normal',
            '--keep', '0',
            '--min_fasta_length', '150',
            '--racon_path', RACON.toString(),
            '--makeblastdb_path', MAKEBLASTDB.toString(),
            '--tblastn_path', TBLASTN.toString(),
            '--bowtie2_path', BOWTIE2.toString(),
            '--bowtie2_build_path', BOWTIE2_BUILD.toString(),
            '--samtools_path', SAMTOOLS.toString(),
            '--pilon_path', PILON.toString(),
            '--out', tmpPath.toString(),
            '--long', longReadsPath.toString() )
            .redirectErrorStream( true )
            .redirectOutput( ProcessBuilder.Redirect.INHERIT )
            .directory( tmpPath.toFile() )


        def cmd = pb.command()
        if( genome.data.size() == 2 ) { // multiple reads -> hybrid assembly
            cmd << '--spades_path'
                cmd << SPADES.toString()
            cmd << '--short1'
                cmd << tmpPath.resolve( genome.data[1].files[0] ).toString()
            cmd << '--short2'
                cmd << tmpPath.resolve( genome.data[1].files[1] ).toString()
        }


        log.info( "exec: ${pb.command()}" )
        log.info( '----------------------------------------------------------------------------------------------' )
        int exitCode = pb.start().waitFor()
        log.info( '----------------------------------------------------------------------------------------------' )
        if( exitCode != 0 ) terminate( "abnormal Unicycler exit code! exitCode!=${exitCode}", genomeContigsPath )


        // calc preFilter assembly stats
        Path rawAssemblyPath = tmpPath.resolve( 'assembly.fasta' )
        def preFilterStatistics = calcAssemblyStatistics( rawAssemblyPath )


        // 1. create mapping (minimap2) for subsequent samtools depth call
        // 2. sort mapped reads
        // 3. compute depth per position
        def contigDepths = [:]
        pb = new ProcessBuilder( 'sh', '-c',
            "${MINIMAP2} -a -Q -t ${NUM_THREADS} -x map-ont ${rawAssemblyPath} ${longReadsPath} | \
            ${SAMTOOLS} sort --threads ${NUM_THREADS} -m ${SAMTOOLS_SORT_MEM} -O BAM | \
            ${SAMTOOLS} depth -" )
            .directory( tmpPath.toFile() )
        log.info( "exec: ${pb.command()}" )
        log.info( '----------------------------------------------------------------------------------------------' )
        Process ps = pb.start()
        ps.in.eachLine( {
            String[] content = it.split( '\t' )
            String contigName = content[0]
            int depth = content[2] as int
            if( contigDepths[ contigName ] )
                contigDepths[ contigName ] += depth
            else
                contigDepths[ contigName ] = depth
        } )
        log.info( '----------------------------------------------------------------------------------------------' )

        // calc contig coverage based on summed contig depths and contig length
        log.debug( "assembly info: ${info}" )
        log.debug( "contig depths: ${contigDepths}" )
        log.debug( 'calc contig coverage:' )
        contigDepths.each( { key, val ->
            log.trace( "\tkey=${key}, val=${val}")
            def contig = preFilterStatistics.contigs.find( {
                log.trace( "\t\tcontigs=${it}" )
                it.name == key
            } )
            assert contig != null
            contig.coverage = val / contig.length
            log.debug( "\tname=${key}, depth-sum=${val}, length=${contig.length}, coverage=${contig.coverage}" )
        } )

        // calc N50/N90 Coverage
        def n50Contigs = preFilterStatistics.contigs.findAll( { it.length >= preFilterStatistics.n50 } )
        preFilterStatistics.n50Coverage = n50Contigs.collect( {it.coverage * it.length} ).sum() / n50Contigs.collect( {it.length} ).sum()
        log.debug( "pre-filter-N50=${preFilterStatistics.n50Coverage}" )
        def n90Contigs = preFilterStatistics.contigs.findAll( { it.length >= preFilterStatistics.n90 } )
        preFilterStatistics.n90Coverage = n50Contigs.collect( {it.coverage * it.length} ).sum() / n90Contigs.collect( {it.length} ).sum()

        // filter contigs due to GC content, length and coverage
        filterContigs( config, genome, rawAssemblyPath, genomeContigsPath, preFilterStatistics, info )
        Path assemblyPath = genomeContigsPath.resolve( "${config.project.genus}_${genome.species}_${genome.strain}.fasta" )
        info << calcAssemblyStatistics( assemblyPath )

        // calc contig coverage based on summed contig depths and contig length
        log.debug( 'calc contig coverage:' )
        contigDepths.each( { key, val ->
            log.trace( "\tkey=${key}, val=${val}")
            def contig = info.contigs.find( {
                log.trace( "\t\tcontigs=${it}" )
                it.name == key
            } )
            assert contig != null
            contig.coverage = val / contig.length
            log.debug( "\tname=${key}, depth-sum=${val}, length=${contig.length}, coverage=${contig.coverage}" )
        } )
        // calc N50/N90 Coverage based on filtered contigs
        n50Contigs = info.contigs.findAll( { it.length >= info.n50 } )
        info.n50Coverage = n50Contigs.collect( {it.coverage * it.length} ).sum() / n50Contigs.collect( {it.length} ).sum()
        log.debug( "post-filter-N50=${info.n50Coverage}" )
        n90Contigs = info.contigs.findAll( { it.length >= info.n90 } )
        info.n90Coverage = n50Contigs.collect( {it.coverage * it.length} ).sum() / n90Contigs.collect( {it.length} ).sum()

    } catch( Throwable t ) {
        terminate( "Unicycler assembly failed! gid=${genome.id}", t, genomeContigsPath )
    }


}


private void runSpades( def config, def genome, Path genomeContigsPath, Path tmpPath, def info ) {

    try {

        // run SPAdes
        ProcessBuilder pb = new ProcessBuilder( SPADES,
            '--threads', NUM_THREADS,
            '--memory', '16',
            '--careful',
            '--disable-gzip-output',
            '--cov-cutoff', 'auto',
            '--phred-offset', '33',
            '-o', tmpPath.toString() )
            .redirectErrorStream( true )
            .redirectOutput( ProcessBuilder.Redirect.INHERIT )
            .directory( tmpPath.toFile() )
        def cmd = pb.command()
        int readLibNr = 1
        genome.data.each( { datum ->
                switch( FileType.getEnum( datum.type ) ) {
                    case FileType.READS_ILLUMINA_SINGLE:
                        cmd << "--s${readLibNr}".toString()
                        cmd << datum.files[0]
                        readLibNr++
                        break
                    case FileType.READS_ILLUMINA_PAIRED_END:
                        cmd << "--pe${readLibNr}-1".toString()
                        cmd << datum.files[0]
                        cmd << "--pe${readLibNr}-2".toString()
                        cmd << datum.files[1]
                        readLibNr++
                        break
                    case FileType.READS_ILLUMINA_MATE_PAIRS:
                        cmd << "--mp${readLibNr}-1".toString()
                        cmd << datum.files[0]
                        cmd << "--mp${readLibNr}-2".toString()
                        cmd << datum.files[1]
                        readLibNr++
                        break
                    case FileType.READS_SANGER:
                        cmd << '--sanger'
                        cmd << datum.files[0]
                        break
                    default:
                        break
                }
        } )
        log.info( "exec: ${pb.command()}" )
        log.info( '----------------------------------------------------------------------------------------------' )
        int exitCode = pb.start().waitFor()
        log.info( '----------------------------------------------------------------------------------------------' )
        if( exitCode != 0 ) terminate( "abnormal SPAdes exit code! exitCode!=${exitCode}", genomeContigsPath )

        // cp assembled contigs to genome contig folder
        Path rawAssemblyPath = tmpPath.resolve( 'scaffolds.fasta' )
        if( !Files.exists( rawAssemblyPath ) ) {
            rawAssemblyPath = tmpPath.resolve( 'contigs.fasta' )
        }

        // calc preFilter assembly stats
        def preFilterStatistics = calcAssemblyStatistics( rawAssemblyPath )
        // extract SPAdes converage stats
        rawAssemblyPath.eachLine( { line ->
            def m = line =~ /^>(NODE_\d+_length_(\d+)_cov_([\d\.e+]+))/
            if( m ){
                String name = m[0][1]
                int length = m[0][2] as int
                double coverage = m[0][3] as double
                def contig = preFilterStatistics.contigs.find( { it.name == name } )
                assert contig != null
                assert contig.length == length
                contig.coverage = coverage
            }
        } )
        // calc N50/N90 Coverage
        def n50Contigs = preFilterStatistics.contigs.findAll( { it.length >= preFilterStatistics.n50 } )
        preFilterStatistics.n50Coverage = n50Contigs.collect( {it.coverage * it.length} ).sum() / n50Contigs.collect( {it.length} ).sum()
        log.debug( "pre-filter-N50=${preFilterStatistics.n50Coverage}" )
        def n90Contigs = preFilterStatistics.contigs.findAll( { it.length >= preFilterStatistics.n90 } )
        preFilterStatistics.n90Coverage = n50Contigs.collect( {it.coverage * it.length} ).sum() / n90Contigs.collect( {it.length} ).sum()


        // filter contigs due to GC content, length and coverage
        filterContigs( config, genome, rawAssemblyPath, genomeContigsPath, preFilterStatistics, info )
        Path assemblyPath = genomeContigsPath.resolve( "${config.project.genus}_${genome.species}_${genome.strain}.fasta" )
        info << calcAssemblyStatistics( assemblyPath )
        // extract SPAdes converage stats
        assemblyPath.eachLine( { line ->
            def m = line =~ /^>(NODE_\d+_length_(\d+)_cov_([\d\.e+]+))/
            if( m ){
                String name = m[0][1]
                int length = m[0][2] as int
                double coverage = m[0][3] as double
                def contig = info.contigs.find( { it.name == name } )
                assert contig != null
                assert contig.length == length
                contig.coverage = coverage
            }
        } )
        // calc N50/N90 Coverage based on filtered contigs
        n50Contigs = info.contigs.findAll( { it.length >= info.n50 } )
        info.n50Coverage = n50Contigs.collect( {it.coverage * it.length} ).sum() / n50Contigs.collect( {it.length} ).sum()
        log.debug( "post-filter-N50=${info.n50Coverage}" )
        n90Contigs = info.contigs.findAll( { it.length >= info.n90 } )
        info.n90Coverage = n50Contigs.collect( {it.coverage * it.length} ).sum() / n90Contigs.collect( {it.length} ).sum()

    } catch( Throwable t ) {
        terminate( "SPAdes assembly failed! gid=${genome.id}", t, genomeContigsPath )
    }
}


private void runHGap( def config, def genome, Path genomeContigsPath, Path tmpPath, def info ) {

    try {

        String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"

        // create dataset file
        ProcessBuilder pb = new ProcessBuilder( DATASET,
            'create',
            '--type', 'SubreadSet',
            '--name', genomeName,
            "${genomeName}.xml".toString(),
            "${genomeName}.subreads.bam".toString() )
            .redirectErrorStream( true )
            .redirectOutput( ProcessBuilder.Redirect.INHERIT )
            .directory( tmpPath.toFile() )
        log.info( "exec: ${pb.command()}" )
        log.info( '----------------------------------------------------------------------------------------------' )
        int exitCode = pb.start().waitFor()
        if( exitCode != 0 ) terminate( "abnormal dataset exit code! exitCode!=${exitCode}", genomeContigsPath )
        log.info( '----------------------------------------------------------------------------------------------' )

        // run HGap4
        pb = new ProcessBuilder( HGAP4,
            'pipeline-id',
            '-e', "eid_subread:${genomeName}.xml",
            "--preset-xml=${SMRTLINK}/global_options.xml".toString(),
            "--preset-xml=${SMRTLINK}/hgap_options.xml".toString(),
            '-o', '.',
            'pbsmrtpipe.pipelines.polished_falcon_lean' )
            .redirectErrorStream( true )
            .redirectOutput( ProcessBuilder.Redirect.INHERIT )
            .directory( tmpPath.toFile() )
        log.info( "exec: ${pb.command()}" )
        log.info( '----------------------------------------------------------------------------------------------' )
        exitCode = pb.start().waitFor()
        if( exitCode != 0 ) terminate( "abnormal HGap4 exit code! exitCode!=${exitCode}", genomeContigsPath )
        log.info( '----------------------------------------------------------------------------------------------' )

        // copy assembled contigs
        Path rawAssemblyPath = Paths.get( tmpPath.toString(), 'tasks', 'genomic_consensus.tasks.variantcaller-0', 'consensus.fasta' )


        // TODO: add circularization step

        // calc assembly stats
        def preFilterStatistics = calcAssemblyStatistics( rawAssemblyPath )

        // store contig depths per base position
        def contigDepths = [:]
        Path coveragePath = Paths.get( tmpPath.toString(), 'tasks', 'pbalign.tasks.pbalign-0', 'mapped.alignmentset.bam' )
        log.info( "exec: samtools depth ${coveragePath}" )
        Process ps = "samtools depth ${coveragePath}".execute()
        ps.in.eachLine( {
            String[] content = it.split( '\t' )
            String contigName = content[0] + '|quiver' // adopt contig names to names in fasta header
            int depth = content[2] as int
            if( contigDepths[ contigName ] )
                contigDepths[ contigName ] += depth
            else
                contigDepths[ contigName ] = depth
        } )

        // calc contig coverage based on summed contig depths and contig length
        log.debug( "assembly info: ${info}" )
        log.debug( "contig depths: ${contigDepths}" )

        log.debug( 'calc contig coverage:' )
        contigDepths.each( { key, val ->
            log.trace( "\tkey=${key}, val=${val}")
            def contig = preFilterStatistics.contigs.find( {
                log.trace( "\t\tcontigs=${it}" )
                it.name == key
            } )
            assert contig != null
            contig.coverage = val / contig.length
            log.debug( "\tname=${key}, depth-sum=${val}, length=${contig.length}, coverage=${contig.coverage}" )
        } )
        // calc N50/N90 Coverage
        def n50Contigs = preFilterStatistics.contigs.findAll( { it.length >= preFilterStatistics.n50 } )
        preFilterStatistics.n50Coverage = n50Contigs.collect( {it.coverage * it.length} ).sum() / n50Contigs.collect( {it.length} ).sum()
        log.debug( "pre-filter-N50=${preFilterStatistics.n50Coverage}" )
        def n90Contigs = preFilterStatistics.contigs.findAll( { it.length >= preFilterStatistics.n90 } )
        preFilterStatistics.n90Coverage = n50Contigs.collect( {it.coverage * it.length} ).sum() / n90Contigs.collect( {it.length} ).sum()

        // filter contigs due to GC content, length and coverage
        filterContigs( config, genome, rawAssemblyPath, genomeContigsPath, preFilterStatistics, info )
        Path assemblyPath = genomeContigsPath.resolve( "${config.project.genus}_${genome.species}_${genome.strain}.fasta" )
        info << calcAssemblyStatistics( assemblyPath )

        // calc contig coverage based on summed contig depths and contig length
        log.debug( 'calc contig coverage:' )
        contigDepths.each( { key, val ->
            log.trace( "\tkey=${key}, val=${val}")
            def contig = info.contigs.find( {
                log.trace( "\t\tcontigs=${it}" )
                it.name == key
            } )
            assert contig != null
            contig.coverage = val / contig.length
            log.debug( "\tname=${key}, depth-sum=${val}, length=${contig.length}, coverage=${contig.coverage}" )
        } )
        // calc N50/N90 Coverage based on filtered contigs
        n50Contigs = info.contigs.findAll( { it.length >= info.n50 } )
        info.n50Coverage = n50Contigs.collect( {it.coverage * it.length} ).sum() / n50Contigs.collect( {it.length} ).sum()
        log.debug( "post-filter-N50=${info.n50Coverage}" )
        n90Contigs = info.contigs.findAll( { it.length >= info.n90 } )
        info.n90Coverage = n50Contigs.collect( {it.coverage * it.length} ).sum() / n90Contigs.collect( {it.length} ).sum()

    } catch( Throwable t ) {
        terminate( "HGap assembly failed! gid=${genome.id}", t, genomeContigsPath )
    }

}


private def calcAssemblyStatistics( Path contigsPath ) {

    def stats = [
        contigs: []
    ]

    def totalNGaps = []
    StringBuilder sb = new StringBuilder( 10_000_000 )

    def m = contigsPath.text =~ /(?m)^>(.+)$\n([ATGCNatgcn\n]+)$/
    m.each( { match ->
            String contig = match[2].replaceAll( '[^ATGCNatgcn]', '' )
            sb.append( contig )
            def contigInfo = [
                name: match[1].split(' ')[0],
                length: contig.length(),
//                gc:   (contig =~ /[GCgc]/).count / (contig =~ /[ATGCatgc]/).count,
                noAs: (contig =~ /[Aa]/).count,
                noTs: (contig =~ /[Tt]/).count,
                noGs: (contig =~ /[Gg]/).count,
                noCs: (contig =~ /[Cc]/).count,
                noNs: (contig =~ /[Nn]/).count
            ]
            contigInfo.gc = ( contigInfo.noGs + contigInfo.noCs ) / (contigInfo.length - contigInfo.noNs)

            def nGaps = []
            def noMultiNs = (contig =~ /[Nn]+/)
            if( noMultiNs.count > 0 ) {
                for( int i=0; i<noMultiNs.count; i++ ) {
                    nGaps << noMultiNs[i].length()
                    totalNGaps << noMultiNs[i].length()
                }
                contigInfo.minNGap = nGaps.min()
                contigInfo.maxNGap = nGaps.max()
                contigInfo.meanNGap = nGaps.sum() / nGaps.size()
                contigInfo.medianNGap = calcMedian( nGaps )
            } else {
                contigInfo.minNGap = 0
                contigInfo.maxNGap = 0
                contigInfo.meanNGap = 0
                contigInfo.medianNGap = 0
            }
            stats.contigs << contigInfo
    } )
    String sequence = sb.toString()

    stats.length = sequence.length()
    stats.noContigs = stats.contigs.size()
    stats.gc   = (sequence =~ /[GCgc]/).count / (sequence =~ /[ATGCatgc]/).count
    stats.noAs = (sequence =~ /[Aa]/).count
    stats.noTs = (sequence =~ /[Tt]/).count
    stats.noGs = (sequence =~ /[Gg]/).count
    stats.noCs = (sequence =~ /[Cc]/).count
    stats.noNs = (sequence =~ /[Nn]/).count

    // calc length stats
    def contigLengths = stats.contigs*.length
    stats.statsLength = [
        min:    contigLengths.min(),
        max:    contigLengths.max(),
        mean:   contigLengths.sum() / contigLengths.size(),
        median: calcMedian( contigLengths ),
        noLt1kb:   0,
	noGt1kb:   0,
        noGt10kb:  0,
        noGt100kb: 0,
        noGt1mb:   0
    ]
    contigLengths.each( {
            if( it >= 10**6 ) stats.statsLength.noGt1mb++
            else if( it >= 10**5 ) stats.statsLength.noGt100kb++
            else if( it >= 10**4 ) stats.statsLength.noGt10kb++
            else if( it >= 10**3 ) stats.statsLength.noGt1kb++
            else stats.statsLength.noLt1kb++
    } )

    // calc N50 / N90
    Collections.sort( contigLengths )
    contigLengths = contigLengths.reverse()
    int nSum = 0
    int i = -1
    while( nSum < 0.5*stats.length) {
        i++
        nSum += contigLengths[ i ]
    }
    stats.n50 = contigLengths[ i ]
    stats.l50 = i + 1

    while( nSum < 0.9*stats.length) {
        i++
        nSum += contigLengths[ i ]
    }
    stats.n90 = contigLengths[ i ]
    stats.l90 = i + 1

    // calc NGaps stats
    stats.statsNGap = [
        no:    totalNGaps.size(),
        min:    totalNGaps.isEmpty() ? 0 : totalNGaps.min(),
        max:    totalNGaps.isEmpty() ? 0 : totalNGaps.max(),
        mean:   totalNGaps.isEmpty() ? 0 : (totalNGaps.sum() / totalNGaps.size()),
        median: totalNGaps.isEmpty() ? 0 : calcMedian( totalNGaps )
    ]

    return stats

}


private static Number calcMedian( def numbers ) {

    Collections.sort( numbers )
    int midNumber = (int)(numbers.size() / 2)

    return numbers.size()%2 != 0 ? numbers[midNumber] : (numbers[midNumber] + numbers[midNumber-1]) / 2

}


private void filterContigs( def config, def genome, Path rawAssemblyPath, Path genomeContigsPath, def preFilterStatistics, def info ) {

    def infoValidContigs     = []
    def infoDiscardedContigs = []

    StringBuilder sbValidContigs = new StringBuilder( 10000000 )
    StringBuilder sbDiscardedContigs = new StringBuilder( 10000000 )

    log.info( "filter-N50-coverage=${0.1*preFilterStatistics.n50Coverage}" )
    def m = rawAssemblyPath.text =~ /(?m)^>(.+)$\n([ATGCNatgcn\n]+)$/
    m.each( { match ->
            String name = match[1].split(' ')[0]
            String sequence = match[2]
            def contigInfo = preFilterStatistics.contigs.find( { it.name == name } )
            if( contigInfo.gc < 0.15  ||  contigInfo.gc > 0.85
                ||  contigInfo.length < 150  ||  (contigInfo.length < 500  &&  contigInfo.coverage < 0.1*preFilterStatistics.n50Coverage) ) {
                log.debug( "discard contig: name=${name}, GC=${contigInfo.gc}, length=${contigInfo.length}, coverage=${contigInfo.coverage}" )
                infoDiscardedContigs << contigInfo
                sbDiscardedContigs.append( '>' ).append( name ).append( '\n' )
                sbDiscardedContigs.append( sequence ).append( '\n' )
            } else {
                log.debug( "accept contig: name=${name}, GC=${contigInfo.gc}, length=${contigInfo.length}, coverage=${contigInfo.coverage}" )
                infoValidContigs << contigInfo
                sbValidContigs.append( '>' ).append( name ).append( '\n' )
                sbValidContigs.append( sequence ).append( '\n' )
            }
    } )

    log.info( "# accepted contigs=${infoValidContigs.size()}" )
    log.info( "# discarded contigs=${infoDiscardedContigs.size()}" )

    Path pathValidContigs = genomeContigsPath.resolve( "${config.project.genus}_${genome.species}_${genome.strain}.fasta" )
    Path pathDiscardedContigs = genomeContigsPath.resolve( "${config.project.genus}_${genome.species}_${genome.strain}-discarded.fasta" )

    pathValidContigs.text = sbValidContigs.toString()
    pathDiscardedContigs.text = sbDiscardedContigs.toString()

    info.discardedContigs = infoDiscardedContigs

}




private void terminate( String msg, Path genomePath ) {
    terminate( msg, null, genomePath )
}


private void terminate( String msg, Throwable t, Path genomePath ) {

    if( t ) log.error( msg, t )
    else    log.error( msg )
    Files.move( genomePath.resolve( 'state.running' ), genomePath.resolve( 'state.failed' ) ) // set state-file to failed
    System.exit( 1 )

}
