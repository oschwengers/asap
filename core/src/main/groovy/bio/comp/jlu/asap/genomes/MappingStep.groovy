
package bio.comp.jlu.asap.genomes


import groovy.io.FileType
import groovy.util.logging.Slf4j
import java.nio.file.*
import bio.comp.jlu.asap.api.GenomeSteps

import static bio.comp.jlu.asap.ASAPConstants.*
import static bio.comp.jlu.asap.api.RunningStates.*
import static bio.comp.jlu.asap.api.Paths.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class MappingStep extends GenomeStep {

    private static final String MAPPING_SCRIPT_PATH = "${ASAP_HOME}/scripts/asap-mapping.groovy"

    private static final GenomeSteps STEP_DEPENDENCY = GenomeSteps.QC

    private static final String QSUB_SLOTS = '8'

    private final Path mappingsPath


    MappingStep( def config, def genome, boolean localMode ) {

        super( GenomeSteps.MAPPING.getAbbreviation(), config, genome, localMode )

        setName( "Mapping-Step-Thread-${genome.id}" )

        // build necessary paths
        mappingsPath = projectPath.resolve( PROJECT_PATH_MAPPINGS )

    }


    @Override
    boolean isSelected() {

        return genome.stepselection.contains( GenomeSteps.MAPPING.getCharCode() )

    }


    @Override
    boolean check() {

        log.debug( "check: genome.id=${genome.id}" )
        long waitingTime = System.currentTimeMillis()
        while( shouldWait() ) {
            if( System.currentTimeMillis() - waitingTime > MAX_STEP_WAITING_PERIOD ) {
                log.warn( "max waiting period (${MAX_STEP_WAITING_PERIOD} s) exceeded!" )
                return false
            }
            try {
                sleep( 1000 * 60 )
                log.trace( "${GenomeSteps.MAPPING.getName()} step slept for 1 min" )
            }
            catch( Throwable t ) { log.error( 'Error: could not sleep!', t ) }
        }

        // check necessary qc analysis status
        return hasStepFinished( STEP_DEPENDENCY )

    }


    private boolean shouldWait() {

        def status = genome.steps[ STEP_DEPENDENCY.getAbbreviation() ]?.status
        log.trace( "qc step status=${status}" )
        return (status != FINISHED.toString()
            &&  status != SKIPPED.toString()
            &&  status != FAILED.toString())

    }


    @Override
    void setup() throws Throwable {

        log.debug( "setup: genome-id=${genome.id}" )

    }


    @Override
    void runStep() throws Throwable {

        log.debug( "run: genome.id=${genome.id}" )

        // submit mapping job
        ProcessBuilder pb = new ProcessBuilder()
            .directory( projectPath.toFile() )
            .redirectErrorStream( true )


        if( localMode ) {
            pb.redirectOutput( mappingsPath.resolve( "${genomeName}.std.log" ).toFile() )
        } else {
            pb.command( 'qsub',
                '-b', 'y',
                '-sync', 'y',
                '-V', // export all env vars to cluster job
                '-N', 'asap-map',
                '-pe', 'multislot', QSUB_SLOTS,
                '-o', mappingsPath.resolve( "${genomeName}.stdout.log" ).toString(),
                '-e', mappingsPath.resolve( "${genomeName}.stderr.log" ).toString() )
            .redirectOutput( mappingsPath.resolve( "${genomeName}.qsub.log" ).toFile() )
        }


        List<String> cmd = pb.command()
        cmd << GROOVY_PATH
            cmd << MAPPING_SCRIPT_PATH
        cmd << '--project-path'
            cmd << projectPath.toString()
        cmd << '--genome-id'
            cmd << Integer.toString( genome.id )


        // start and wait for process to exit
        log.debug( "genome.id=${genome.id}: exec: ${pb.command()}" )
        Process ps = pb.start()
        setStatus( RUNNING )
        int exitCode = ps.waitFor()


        // check exit code
        if( exitCode != 0 )
            throw new IllegalStateException( "abnormal ${GenomeSteps.MAPPING.getName()} exit code! exitCode=${exitCode}" );


        // check state.failed / state.finished with exponential backoff
        int sec=1
        while( sec < (1<<EXP_BACKOFF_EXP) ) { // wait 1023 s (~ 17 min) in total
            try{
                sleep( sec * 1000 )
            } catch( InterruptedException ie ) {}
            log.debug( "genome.id=${genome.id}: exp backoff=${sec} s" )
            if( Files.exists( mappingsPath.resolve( "${genomeName}.failed" ) ) )
                throw new IllegalStateException( "abnormal ${GenomeSteps.MAPPING.getName()} state: failed" )
            else if( Files.exists( mappingsPath.resolve( "${genomeName}.finished" ) ) )
                break
            sec <<= 1
        }
        if( sec >= (1<<EXP_BACKOFF_EXP)  &&  !Files.exists( mappingsPath.resolve( "${genomeName}.finished" ) ) )
            throw new IllegalStateException( "abnormal ${GenomeSteps.MAPPING.getName()} state: !finished, timeout=${sec} s" )

    }


    @Override
    void clean() throws Throwable  {

        log.debug( "clean: genome.id=${genome.id}" )
        
        mappingsPath.toFile().eachFileMatch( FileType.FILES, ~/${genomeName}\..+\.log/, {
            if( it.getName().endsWith( '.log' )  &&  it.length() == 0 ) {
                try{
                    log.debug( "remove empty log file: ${it}" )
                    Files.delete( it )
                } catch( Exception ex ) {
                    log.warn( "could not delete file: ${it}", ex )
                }
            }
        } )

    }

}

