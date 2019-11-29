
package bio.comp.jlu.asap

import java.io.IOException
import java.nio.file.*
import java.time.*
import java.util.concurrent.*
import groovy.json.JsonSlurper
import groovy.util.CliBuilder
import ch.qos.logback.classic.*
import org.slf4j.*
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import bio.comp.jlu.asap.api.*
import bio.comp.jlu.asap.analyses.AnalysesRunner
import bio.comp.jlu.asap.genomes.GenomeRunner
import bio.comp.jlu.asap.reports.ReportRunner
import bio.comp.jlu.asap.steps.*

import static bio.comp.jlu.asap.api.AnalysesSteps.*
import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.RunningStates.*
import static bio.comp.jlu.asap.api.GenomeSteps.*
import static bio.comp.jlu.asap.ASAPConstants.*


// check options
def cli = new CliBuilder( usage: "java -jar asap-${ASAP_VERSION}.jar -p <project-directory> [-h|-i|-c|-r] [-n] [-l] [-s <grid-slots>] [-k|-a] [-d]" )
    cli.p( longOpt: 'project-dir', args: 1, argName: 'project-directory',    required: true,  'The path to a project directory.' )
    cli.h( longOpt: 'help',      args: 0, argName: 'show help',              required: false, 'Show ASA³P usage.' )
    cli.i( longOpt: 'info',      args: 0, argName: 'info',                   required: false, 'Show information about a certain project.' )
    cli.c( longOpt: 'check',     args: 0, argName: 'check config',           required: false, 'Check the spreadsheet config file along with all corresponding project files. This does not start ASA³P!' )
    cli.r( longOpt: 'reports',   args: 0, argName: 'reports',                required: false, 'Only (re)create reports. Existing reports will be removed!' )
    cli.n( longOpt: 'clean',     args: 0, argName: 'clean project',          required: false, 'Clean project folder. Attention! This will delete all data from earlier ASA³P runs.' )
    cli.l( longOpt: 'local',     args: 0, argName: 'local execution',        required: false, 'Carry out all computations locally on the current host. Use this option if no grid engine is available.' )
    cli.s( longOpt: 'slots',     args: 1, argName: 'grid-slots',             required: false, 'Amount of grid computing slots ASA³P should use. Default: 50' )
    cli.k( longOpt: 'skip-comp', args: 0, argName: 'skip comparatives',      required: false, 'Skip comparative analyses. Use this option to disable pan/core genome and phylogeny steps.' )
    cli.a( longOpt: 'skip-char', args: 0, argName: 'skip characterizations', required: false, 'Skip characterization analyses. Use this option to disable taxonomic classification, MLST, ABR, VF detection and mapping steps. WARNING: this also disables the comparative steps as those partially depend on the characterization steps!' )
    cli.d( longOpt: 'debug',     args: 0, argName: 'debug',                  required: false, 'Activate intensive logging for debugging purposes.' )

def opts = cli.parse( args )
if( !opts )
    return
else if( opts.h  ||  !opts.p )
    cli.usage()


// check ASAP_HOME env variable
final String ASAP_HOME = System.getenv()['ASAP_HOME']
if( !ASAP_HOME ) {
    println( "Error: unset environment variable ASAP_HOME!" )
    System.exit(1)
}


// check project dir and config file
Path rawProjectPath = Paths.get( opts.p )
if( !Files.exists( rawProjectPath ) ) {
    println( "Error: Project directory (${rawProjectPath}) does not exist!" )
    System.exit(1)
}
if( !Files.isReadable( rawProjectPath )  ||  !Files.isWritable( rawProjectPath )  ||  !Files.isExecutable( rawProjectPath ) ) {
    println( "Error: Wrong project directory (${rawProjectPath}) file permissions! The directory is either not readable, accessible or writable. Please, check the read/write/execute flags." )
    System.exit(1)
}
Path projectPath = rawProjectPath.toRealPath()


System.setProperty( 'PROJECT_PATH', projectPath.toString() ) // set project path for logging output
log = LoggerFactory.getLogger( getClass().getName() )
if( opts.d ) { // set logging to debug upon user request
    ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger( org.slf4j.Logger.ROOT_LOGGER_NAME )
    rootLogger.setLevel( ch.qos.logback.classic.Level.DEBUG )
}


log.info( "version: ${ASAP_VERSION}" )
def env = System.getenv()
log.debug( "USER: ${env.USER}" )
log.debug( "CWD: ${env.PWD}" )
log.debug( "HOSTNAME: ${env.HOSTNAME}" )
log.debug( "ASAP_HOME: ${env.ASAP_HOME}" )
log.debug( "PATH: ${env.PATH}" )
def props = System.getProperties()
log.debug( "user.name: ${props['user.name']}" )
log.debug( "user.dir: ${props['user.dir']}" )
log.debug( "user.home: ${props['user.home']}" )
log.debug( "os.name: ${props['os.name']}" )
log.debug( "os.version: ${props['os.version']}" )
log.debug( "file.encoding: ${props['file.encoding']}" )
log.info( "project-path: ${projectPath}" )
log.info( "command line: ${args}" )
log.debug( "option -s/--slots: ${opts.s}" )
log.debug( "option -i/--info: ${opts.i}" )
log.debug( "option -r/--reports: ${opts.r}" )
log.debug( "option -c/--check: ${opts.c}" )
log.debug( "option -n/--clean: ${opts.n}" )
log.debug( "option -l/--local: ${opts.l}" )
log.debug( "option -k/--skip-comp: ${opts.k}" )
log.debug( "option -a/--skip-char: ${opts.a}" )
log.debug( "option -d/--debug: ${opts.d}" )


if( opts.n ) { // clear project folder

    log.info( "option: -n/--clean" )
    [
        PROJECT_PATH_SEQUENCES,
        PROJECT_PATH_ABR,
        PROJECT_PATH_VF,
        PROJECT_PATH_ANNOTATIONS,
        PROJECT_PATH_ASSEMBLIES,
        PROJECT_PATH_SCAFFOLDS,
        PROJECT_PATH_MAPPINGS,
        PROJECT_PATH_MLST,
        PROJECT_PATH_REFERENCES,
        PROJECT_PATH_READS_RAW,
        PROJECT_PATH_READS_QC,
        PROJECT_PATH_REPORTS,
        PROJECT_PATH_SNPS,
        PROJECT_PATH_TAXONOMY,
        PROJECT_PATH_PHYLOGENY
    ].each( {
        Path folderPath = projectPath.resolve( it )
        if( !folderPath.deleteDir()  )
            Misc.exit( log, "Couldn't delete directory! dir=${folderPath}", null )
    } )
    [
        'asap.log',
        'state.running',
        'state.failed',
        'state.finished',
        'config.json',
    ].each( {
        Path filePath = projectPath.resolve( it )
        try{
            Files.delete( filePath )
        } catch( NoSuchFileException nsfe ) {
        } catch( IOException | SecurityException ex ) {
            Misc.exit( log, "couldn't delete file! dir=${filePath}", ex )
        }
    } )
}


// remove existing config.json for proper config checks
Path configPath = projectPath.resolve( 'config.json' )
if( opts.c ) {
    // Remove config.json only for new/emtpy empty project.
    if( !Files.exists( projectPath.resolve( 'state.finished' ) )  &&  !Files.exists( projectPath.resolve( 'state.running' ) ) )
        try{
            log.debug( "delete pre-existing config.json: ${configPath.toString()}" )
            Files.delete( configPath )
        } catch( Exception ex ) {}
}


// convert spreadsheet config into JSON config
try {
    ConfigWriterThread.convertConfig( log, projectPath )
} catch( Exception ex ) {
    Misc.exit( log, "Could not convert configuration file to JSON! Please, check the configuration file and test it via the '-c/--check' option.", ex )
}
if( !Files.exists( configPath ) )
    Misc.exit( log, "Configuration file (${configPath}) does not exist!", null )


// parse config.json
def config = (new JsonSlurper()).parseText( configPath.toFile().text )
config.project.path = projectPath.toString() // store project path
config.project.debugging = opts.b // store debbuging state


if( opts.c ) { // only check config and data files

    log.info( "option: -c/--check" )

    checkConfig( config, projectPath )
    log.info( 'checked spreadsheet config and project data files.' )
    println( "Checked project directory: ${projectPath.toString()}" )
    println( "Checked configuration file: ${projectPath.resolve( 'config.xls' ).toString()}" )
    println( "All checks successfully passed. " )

    System.exit( 0 )

} else if( opts.i ) { // only show info and statistics about current pipeline

    log.info( "option: -i/--info" )

    // print config.json
    printInfo( config )

    // print runtime statistics
    if( Files.exists( projectPath.resolve( 'state.finished' ) ) )
        printRuntime( config )

} else if( opts.r ) { // (re)create reports

    log.info( "option: -r/--reports" )

    if( !Files.exists( projectPath.resolve( 'state.finished' ) ) )
        Misc.exit( log, 'couldn\'t detect a former pipeline run! (no state.finished)', null )

    def configWriterThread = new ConfigWriterThread( config )
        configWriterThread.start() // start config writer thread

    // start html reporting
    def reportRunner = new ReportRunner( config )
        reportRunner.start()
        reportRunner.waitFor()

    // shutdown config writer thread
    configWriterThread.finish()
    configWriterThread.join()

} else { // normal setup, start pipeline

    log.info( "option: none -> normal execution" )

    /* perform initialization code
     * before pipeline steps start
     */

    int slotSize // set / estimate suitable slot size
    boolean localMode = false
    if( opts.l ) {
        // use twice the estimated max per step CPU usage (8) due to parallel task execution
        slotSize = Runtime.getRuntime().availableProcessors() / (2*8)
        localMode = true
        if( slotSize < 1 )
            slotSize = 1
        log.info( 'execution mode: local' )
        log.info( "slot size: ${slotSize}" )
    } else if( opts.s ) {
        log.info( 'execution mode: grid' )
        try {
            slotSize = Integer.parseInt( opts.s )
            log.info( "custom slot size: ${slotSize}" )
        }
        catch( Exception ex ) {
            Misc.exit( log, 'Wrong slot parameter!', ex )
        }
    } else {
        slotSize = 50
        log.info( 'default slot size: 50' )
    }


    // store comparative analyses option (opts.k disables comp. analyses)
    config.project.comp = !opts.k


    // store characterization analyses option (opts.a disables char. analyses)
    config.project.characterization = !opts.a
    if( opts.a )
        config.project.comp = false


    log.debug( 'write "state.running" file' )
    if( Files.exists( projectPath.resolve( 'state.finished' ) ) )
        Files.move( projectPath.resolve( 'state.finished' ), projectPath.resolve( 'state.running' ) )
    else if( Files.exists( projectPath.resolve( 'state.running' ) ) ) {
        Misc.exit( log, 'state.running file indicates an already running instance!', null )
    } else
        Files.createFile( projectPath.resolve( 'state.running' ) )


    config.dates.start = OffsetDateTime.now().toString()

    checkConfig( config, projectPath )
    setupStepSelections( config ) // sanitize step selection

    if( !Files.exists( projectPath.resolve( 'state.finished' ) ) ) // make sure it's the first run
        setupDirectories( config, projectPath ) // create project folder subdirectories

    def configWriterThread = new ConfigWriterThread( config )
        configWriterThread.start() // start config writer thread

    printInfo( config ) // print config.json

    if( !config.analyses ) // tmp fix for config bug
        config.analyses = [:]
    if( !config.steps ) // tmp fix for config bug
        config.steps = [:]


    // start init steps
    def initSteps = [
        new ReferenceProcessings( config ),
        new MappingIndices( config ),
        new SNPAnnotationSetup( config ),
        new GenomeConversions( config )
    ]
    initSteps.each( { it.start() } )


    // wait for init steps
    initSteps.each( {
        it.waitFor()
        if( it.getStatus() == FAILED )
            Misc.exit( log, "init step (${it.getStepName()}) failed!", null )
    } )


    /* start genome runners
     * in order to run genome specific Steps
     */
    def threadPool = Executors.newFixedThreadPool( slotSize, new ASAPThreadFactory() )
    try {
        List<Future> futures = config.genomes.collect{ genome ->
            threadPool.submit( new GenomeRunner( config, genome, localMode ) );
        }
        futures.each( { it.get() } )
    } catch( Throwable ex ) {
        Misc.exit( log, "error in internal thread pool!", ex )
    }


    if( config.project.comp ) {
        // start global analyses steps after single runs ran in parallel
        Step analysesRunner = new AnalysesRunner( config, localMode ) // start analyses runner
        analysesRunner.start()
        analysesRunner.waitFor()
    }


    config.dates.end = OffsetDateTime.now().toString()


    // start html reporting
    def reportRunner = new ReportRunner( config )
        reportRunner.start()
        reportRunner.waitFor()


    // shutdown config writer thread
    configWriterThread.finish()
    configWriterThread.join()


    // delete tmp sequences dir
    projectPath.resolve( PROJECT_PATH_SEQUENCES ).deleteDir()


    // print runtime statistics
    printRuntime( config )


    log.debug( 'move "state.running" into "state.finished"' )
    Files.move( projectPath.resolve( 'state.running' ), projectPath.resolve( 'state.finished' ) )

}




def checkConfig( def config, Path projectPath ) {

    log.debug( 'check config file...' )

    log.debug( 'check user info' )
    if( !config.user )
        Misc.exit( log, 'User information is missing in configuration file!', null )

    if( !config.user.name )
        Misc.exit( log, 'User name is missing or empty in configuration file!', null )
    config.user.name = config.user.name.trim()

    if( !config.user.surname )
        Misc.exit( log, 'User surname is missing or empty in configuration file!', null )
    config.user.surname = config.user.surname.trim()

    if( !config.user.email )
        Misc.exit( log, 'User email is missing or empty in configuration file!', null )
    config.user.email = config.user.email.trim()
    if( !(config.user.email ==~ /[\w-_\.]+@(?:[\w+-]+\.)+[a-z]{2,20}/) )
        Misc.exit( log, "Unvalid user email (${config.user.email}) in configuration file!\nThe provided email seems to be unvalid.\nIf this is not the case, please do not hesitate to file an issue at GitHub.", null )


    log.debug( 'check project info' )
    if( !config.project )
        Misc.exit( log, 'Project information is missing!', null )

    if( !config.project.name )
        Misc.exit( log, 'Project name in configuration file is missing or empty!', null )
    config.project.name = config.project.name.trim()

    if( !config.project.description )
        Misc.exit( log, 'Project description in configuration file is missing or empty!', null )
    config.project.description = config.project.description.trim()

    if( !config.project.genus )
        Misc.exit( log, 'Project genus designation in configuration file is missing or empty!', null )
    String genus = config.project.genus.trim()
    if( !(genus ==~ /[a-zA-Z]{5,30}/) )
        Misc.exit( log, "Genus designation (${genus}) in configuration file contains unvalid characters!\nOnly alphabetical characters are allowed. It should contain at least 5 and a maximum of 30 characters.\nJust in case, this is not enough, please do not hesitate to file an issue at GitHub.", null )
    config.project.genus = genus.toLowerCase().capitalize()


    Path dataPath = projectPath.resolve( PROJECT_PATH_DATA )
    log.debug( 'check genomes' )
    if( !config.genomes ) {
        Misc.exit( log, 'Configuration file contains no genome(s)/strain(s) information!\nPlease, check the configuration file\'s strain table', null )
    } else {
        def idList = []
        def sampleList = []
        config.genomes.each( { genome ->
            if( !genome )
                Misc.exit( log, "Genome information in configuration file is missing!", null )

            if( !genome.id )
                Misc.exit( log, "Genome id designation in configuration file is missing!", null )
            if( idList.contains( genome.id ) )
                Misc.exit( log, "Duplicated genome id in configuration file! (${genome.id})", null )
            idList << genome.id

            if( !genome.species )
                Misc.exit( log, "Genome species designation in configuration file for genome (${genome}) is empty!", null )
            String species = genome.species.trim().toLowerCase()
            if( species != 'sp.'  &&  !(species ==~ /[a-z]{2,50}/) )
                Misc.exit( log, "Species designation (${species}) in configuration file contains unvalid characters!\nOnly alphabetical characters or 'sp.' are allowed. It should contain at least 2 and a maximum of 50 characters.\nJust in case, this is not enough, please do not hesitate to file an issue at GitHub.", null )
            genome.species = species

            if( !genome.strain )
                Misc.exit( log, "Genome strain designation in configuration file for genome (${genome}) is empty!", null )
            String strain = genome.strain.trim()
            if( !(strain ==~ /[a-zA-Z0-9\.-_]{2,50}/) )
                Misc.exit( log, "Strain designation (${strain}) in configuration file contains unvalid characters!\nOnly alpha-numerics, '-', '_' and '.' are allowed. It should contain at least 2 and a maximum of 50 characters.\nJust in case, this is not enough, please do not hesitate to file an issue at GitHub.", null )
            genome.strain = strain

            String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
            if( sampleList.contains( genomeName ) )
                Misc.exit( log, "Duplicated genome name (${genomeName}) in configuration file!\nThe combination of <genus>, <species> and <strain> designations of each genome must be unique within this project as ASA³P uses this for as an identifier within the project directory.", null )
            sampleList << genomeName

            if( !genome.data  &&  genome.data.size() == 0 )
                Misc.exit( log, "Input data in configuration file for genome (${genome}) is missing!", null )
            genome.data.each( { datum ->
                if( !datum.type )
                    Misc.exit( log, "Input data type information for genome (${genome}) in configuration file is missing!", null )

                FileType ft = FileType.getEnum( datum.type )
                try {
                    if( datum.files.size() == 0 )
                        Misc.exit( log, "Input data file/s in configuration file for genome (${genome}) is/are missing!", null )
                    if( (ft?.getDataType() == DataType.READS)  ||  (ft == FileType.GENOME) ) {
                        datum.files.each( {
                            if( !Files.isReadable( dataPath.resolve( it ) ) ) {
                                Misc.exit( log, "Could not read input data file (${fileName}) for genome (${genomeName}) in project data subdirectory!", null )
                            }
                        } )
                    } else {
                        if( !(ft in [FileType.CONTIGS, FileType.CONTIGS_ORDERED] ) )
                            Misc.exit( log, "Wrong input data file type (${ft}) for genome (${genomeName}) detected! ", null )
                        String fileName = datum.files[0]
                        if( !Files.isReadable( dataPath.resolve( fileName ) ) ) {
                            Misc.exit( log, "Could not read input data file (${fileName}) for genome (${genomeName}) in project data subdirectory!", null )
                        }
                    }
                } catch( IOException ex ) {
                    Misc.exit( log, "Input data files for genome (${genomeName}) are not readable! (${datum.files})", ex )
                }
            } )
        } )
    }


    log.debug( 'check references' )
    if( config.references ) {
        Path referencePath = projectPath.resolve( PROJECT_PATH_REFERENCES )
        config.references.each( { ref ->
            if( !(ref ==~ /[a-zA-Z0-9\._-]{2,}/) ) {
                Misc.exit( log, "Wrong reference file name (${ref}) in configuration file!\nOnly alpha-numerics, '_', '-' and '.' are allowed. It should contain at least 2 characters.", null )
            } else if( !Files.isReadable( dataPath.resolve( ref ) ) ) {
                Misc.exit( log, "Reference file (${ref}) does not exist or is not readable in project data subdirectory!", null )
            } else if( !ref.contains( '.' )  ||  FileFormat.getEnum( ref ) == null ) {
                Misc.exit( log, "Missing or wrong reference file suffix (${ref})!\nThe following file suffices are supported:\nFasta: '.fa', '.fas', '.fsa', '.fna', '.fasta'\nGenBank: '.gb', '.gbk', '.gbff', '.genbank'\nGFF: '.gff', '.gff3'\nEMBL: '.el', '.ebl', '.embl'", null )
            }
        } )
    } else {
        Misc.exit( log, "Empty reference list in configuration file! Please, specify at least 1 reference genome.", null )
    }

}


def setupStepSelections( config ) {

    // parse provided input data and add necessary steps
    config.genomes.each( { genome ->

        Set<GenomeSteps> necessarySteps = new LinkedHashSet<>()

        FileType ft = FileType.getEnum( genome.data[0].type )
        if( ft?.getDataType() == DataType.READS ) {
            necessarySteps << QC
            necessarySteps << ASSEMBLY
            necessarySteps << SCAFFOLDING
            necessarySteps << ANNOTATION
            if( config.project.characterization ) {
                necessarySteps << MAPPING
                necessarySteps << SNP_DETECTION
                necessarySteps << TAXONOMY
                necessarySteps << MLST
                necessarySteps << ABR
                necessarySteps << VF
            }
        } else if( ft == FileType.CONTIGS ) {
            necessarySteps << SCAFFOLDING
            necessarySteps << ANNOTATION
            if( config.project.characterization ) {
                necessarySteps << TAXONOMY
                necessarySteps << MLST
                necessarySteps << ABR
                necessarySteps << VF
            }
        } else if( ft == FileType.CONTIGS_ORDERED  ||  ft == FileType.CONTIGS_LINKED ) {
            necessarySteps << ANNOTATION
            if( config.project.characterization ) {
                necessarySteps << TAXONOMY
                necessarySteps << MLST
                necessarySteps << ABR
                necessarySteps << VF
            }
        } else if( ft == FileType.GENOME ) {
            if( config.project.characterization ) {
                necessarySteps << TAXONOMY
                necessarySteps << MLST
                necessarySteps << ABR
                necessarySteps << VF
            }
        }

        genome.stepselection = necessarySteps.collect( { it.getCharCode() } ).join( '' )

    } )

}


def setupDirectories( def config, def projectPath ) {

    log.debug( 'build project folder structure' )
    [
        PROJECT_PATH_SEQUENCES,
        PROJECT_PATH_REFERENCES,
        PROJECT_PATH_READS_RAW,
        PROJECT_PATH_READS_QC,
        PROJECT_PATH_ASSEMBLIES,
        PROJECT_PATH_SCAFFOLDS,
        PROJECT_PATH_ANNOTATIONS,
        PROJECT_PATH_TAXONOMY,
        PROJECT_PATH_MLST,
        PROJECT_PATH_ABR,
        PROJECT_PATH_VF,
        PROJECT_PATH_MAPPINGS,
        PROJECT_PATH_SNPS
    ].each( { folderName ->
        Path folderPath = projectPath.resolve( folderName )
        if( !Files.exists( folderPath ) ) {
            try{
                Files.createDirectory( folderPath )
                log.debug( "created folder: ${folderPath}" )
            } catch( FileAlreadyExistsException faee ) {
                log.debug( "folder already existed: ${folderPath}" )
            }
        }
    } )


    // hard link files from DATA folder into corresponding subdirs
    try {
        Path dataPath = projectPath.resolve( PROJECT_PATH_DATA )
        config.genomes.each( { genome ->
            String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
            genome.data.each( { datum ->
                FileType ft = FileType.getEnum( datum.type )
                if( ft?.getDataType() == DataType.READS ) {
                    Path destPath = Paths.get( projectPath.toString(), PROJECT_PATH_READS_RAW, genomeName )
                    if( !Files.exists( destPath ) ) Files.createDirectory( destPath )
                    datum.files.each( { Files.createLink( destPath.resolve( it ), dataPath.resolve( it ) ) } )
                } else if( ft == FileType.CONTIGS ) {
                    Path destPath = Paths.get( projectPath.toString(), PROJECT_PATH_ASSEMBLIES, genomeName )
                    if( !Files.exists( destPath ) ) Files.createDirectory( destPath )
                    Files.createLink( destPath.resolve( "${genomeName}.fasta" ), dataPath.resolve( datum.files[0] ) )
                } else if( ft == FileType.CONTIGS_ORDERED ) {
                    Path destPath = Paths.get( projectPath.toString(), PROJECT_PATH_SCAFFOLDS, genomeName )
                    if( !Files.exists( destPath ) ) Files.createDirectory( destPath )
                    Files.createLink( destPath.resolve( "${genomeName}.fasta" ), dataPath.resolve( datum.files[0] ) )
                    // link scaffold file to sequence directory for genome characterization steps
                    destPath = projectPath.resolve( PROJECT_PATH_SEQUENCES )
                    Files.createLink( destPath.resolve( "${genomeName}.fasta" ), dataPath.resolve( datum.files[0] ) )
                } else if( ft == FileType.GENOME ) {
                    Path destPath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName )
                    if( !Files.exists( destPath ) ) Files.createDirectory( destPath )
                    FileFormat ff = FileFormat.getEnum( datum.files[0] )
                    if( datum.files.size() == 1 ) { // either GenBank or GFF3 (both including sequence information)
                        String fileSuffix = null
                        if( ff == FileFormat.GENBANK )
                            fileSuffix = 'gbk'
                        else if( ff == FileFormat.EMBL )
                            fileSuffix = 'ebl'
                        else if( ff == FileFormat.GFF )
                            fileSuffix = 'gff'
                        assert fileSuffix != null
                        Files.createLink( destPath.resolve( "${genomeName}.${fileSuffix}" ), dataPath.resolve( datum.files[0] ) )
                    } else if( datum.files.size() == 2 ) { // check if its GFF3 + Fasta
                        /** As Roary expects GFF files containing the sequence information
                         *  we have to make a proper copy in order to later on join
                         *  sequence and annotation during the GenomeConversion step.
                        /*  This way, the raw data stays unmodified.
                        */
                        FileFormat ff2 = FileFormat.getEnum( datum.files[1] )
                        if( (ff == FileFormat.FASTA)  &&  (ff2 == FileFormat.GFF) ) {
                            Files.createLink( destPath.resolve( "${genomeName}.fasta" ), dataPath.resolve( datum.files[0] ) )
                            Files.copy( dataPath.resolve( datum.files[1] ), destPath.resolve( "${genomeName}.gff" ) )
                        } else if( (ff == FileFormat.GFF)  &&  (ff2 == FileFormat.FASTA) ) {
                            Files.copy( dataPath.resolve( datum.files[0] ), destPath.resolve( "${genomeName}.gff" ) )
                            Files.createLink( destPath.resolve( "${genomeName}.fasta" ), dataPath.resolve( datum.files[1] ) )
                        } else {
                            Misc.exit( log, "genome file suffices do not match GFF3/Fasta format! (${datum.files})", ex )
                        }
                    }
                }
            } )
        } )
        config.references.each( { ref ->
            Files.createLink( Paths.get( projectPath.toString(), PROJECT_PATH_REFERENCES, ref ), dataPath.resolve( ref ) )
        } )
    } catch( Throwable t ) {
        Misc.exit( log, "could not copy data content to corresponding subfolders!", t )
    }

}


def printInfo( config ) {

    println( "ASAP version ${ASAP_VERSION}" )
    println()

    println( 'project:' )
    println( '\tname:'.padRight(12) + config.project.name )
    println( '\tdesc:'.padRight(12) + config.project.description )
    println( '\tpath:'.padRight(12)  + config.project.path )
    println( '\tgenus:'.padRight(12)  + config.project.genus )
    println( '\treferences:' )
    config.references.each( { println( "\t\t${it}" ) } )

    println()
    println( 'user:' )
    println( '\tname:'.padRight(12)    + config.user.name )
    println( '\tsurname:'.padRight(12) + config.user.surname )
    println( '\temail:'.padRight(12)   + config.user.email )

    println()
    println( 'genomes:' )
    config.genomes.each( {

        println( '\tid:'.padRight(12)       + "${it.id}" )
        println( '\torganism:'.padRight(12) + "${config.project.genus.substring(0,1)}. ${it.species} ${it.strain}" )
        println( '\tdata: ' )

        int i = 1;
        it.data.each( { datum ->
            println( "\t\t[${i++}]: ${datum.type}, ${datum.files}" )
        } )
        println()

    } )

}


def printRuntime( config ) {

    def stepsStatistics = [:]
    GenomeSteps.values().each( { stepsStatistics[ it.getAbbreviation() ] = [ 'amount': 0, 'sum': 0L, 'min': Duration.ofSeconds(Long.MAX_VALUE), 'max': Duration.ofSeconds(0L), 'suc': 0, 'err': 0, 'skip': 0 ] } )

    // print genome statistics
    println()
    println( 'genome statistics:' )
    config.genomes.each( {
        if( it.steps )
            println( "\tid: ${it.id}, organism: ${config.project.genus} ${it.species} ${it.strain}" )
        it.steps.each( { abbr, step ->
            if( step.status == FINISHED.toString() ) {
                stepsStatistics[ abbr ].suc++
                if( step.start  &&  step.end  ) {
                    Duration runtime = Duration.between( OffsetDateTime.parse( step.start ), OffsetDateTime.parse( step.end ) )
                    if( runtime < stepsStatistics[ abbr ].min )
                    stepsStatistics[ abbr ].min = runtime
                    if( runtime > stepsStatistics[ abbr ].max )
                    stepsStatistics[ abbr ].max = runtime
                    stepsStatistics[ abbr ].amount++
                    stepsStatistics[ abbr ].sum += runtime.toSeconds()
                    println( String.format( '\t%-11s %s, %s, (%s -> %s)', abbr+':', step.status, Misc.formatRuntimes( runtime ), step.start, step.end ) )
                } else {
                    println( String.format( '\t%-11s %s', abbr+':', step.status ) )
                }
            } else if( step.status == SKIPPED.toString() )
                stepsStatistics[ abbr ].skip++
            else if( step.status == FAILED.toString() )
                stepsStatistics[ abbr ].err++
            else if( step.status == null )
                step.status == ''
        } )
        if( it.steps )
            println()
    } )


    // print step statistics
    println()
    println( String.format( '%-28s %4s | %4s | %4s \t %12s | %12s | %12s', 'genome analyses statistics:', 'suc', 'err', 'skip', 'min', 'mean', 'max' ) );
    stepsStatistics.each( { abbr, step ->
        String minRuntime  = step.amount > 0 ? Misc.formatRuntimes( step.min ) : '-'
        String meanRuntime = step.amount > 0 ? Misc.formatRuntimes( Duration.ofSeconds((long)(step.sum / step.amount)) ) : '-'
        String maxRuntime  = step.amount > 0 ? Misc.formatRuntimes( step.max ) : '-'
        println( String.format( '\t%-20s %4d | %4d | %4d \t %12s | %12s | %12s', "${abbr} steps:", step.suc, step.err, step.skip, minRuntime, meanRuntime, maxRuntime ) )
    } )


    // print analyses statistics
    println()
    println( 'comparative analyses statistics:' )
    if( config.analyses ) {
        AnalysesSteps.values().each{ it ->
            def analysis = config.analyses[ it.getAbbreviation() ]
            if( analysis != null  &&  analysis?.start != null  &&  analysis?.end != null ) {
                Duration runtime = Duration.between( OffsetDateTime.parse( analysis.start ), OffsetDateTime.parse( analysis.end ) )
                println( String.format( '\t%-16s %s, %s, (%s -> %s)', it.getName()+':', analysis.status, Misc.formatRuntimes( runtime ), analysis.start, analysis.end ) )
            } else
                println( String.format( '\t%-16s %s', it.getName()+':', analysis?.status ?: '-' ) )
        }
    } else
        println( '-> no executed analyses detected!' )


    // print total statistics
    println()
    println()
    if( config.dates.start  &&  config.dates.end ) {
        Duration runtime = Duration.between( OffsetDateTime.parse( config.dates.start ), OffsetDateTime.parse( config.dates.end ) )
        String totalRuntimes = Misc.formatRuntimes( runtime )
        println( "total runtime: ${totalRuntimes}" )
    } else
        println( 'total runtime:' )
    println( '\tstart: '  + (config.dates.start ?: '-') )
    println( '\tend:   '  + (config.dates.end ?: '-') )

}
