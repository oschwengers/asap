
package bio.comp.jlu.asap.genomes


import groovy.util.logging.Slf4j
import groovy.io.FileType
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import bio.comp.jlu.asap.api.GenomeSteps

import static bio.comp.jlu.asap.ASAPConstants.*
import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class MLSTStep extends GenomeStep {

    private static final String MLST_SCRIPT_PATH = "${ASAP_HOME}/scripts/asap-mlst.groovy"

    private static final GenomeSteps STEP_DEPENDENCY = GenomeSteps.SCAFFOLDING

    private static final String QSUB_FREE_MEM = '2'

    private Path   mlstPath = projectPath.resolve( PROJECT_PATH_MLST )


    MLSTStep( def config, def genome, boolean localMode ) {

        super( GenomeSteps.MLST.getAbbreviation(), config, genome, localMode )

        setName( "MLST-Step-Thread-${genome.id}" )

    }


    @Override
    boolean isSelected() {

        return genome?.stepselection.contains( GenomeSteps.MLST.getCharCode() )

    }


    @Override
    boolean check() {

        log.debug( "check: genome.id=${genome.id}" )
        if( genome?.stepselection.contains( STEP_DEPENDENCY.getCharCode() ) ) {
            // wait for assembly step
            long waitingTime = System.currentTimeMillis()
            while( shouldWait() ) {
                if( System.currentTimeMillis() - waitingTime > MAX_STEP_WAITING_PERIOD ) {
                    log.warn( "max waiting period (${MAX_STEP_WAITING_PERIOD} s) exceeded!" )
                    return false
                }
                try {
                    sleep( 1000 * 60 )
                    log.trace( "${GenomeSteps.MLST.getName()} step slept for 1 min" )
                }
                catch( Throwable t ) { log.error( 'Error: could not sleep!', t ) }
            }

            // check necessary qc analysis status
            return hasStepFinished( STEP_DEPENDENCY )

        } else
            return true

    }


    private boolean shouldWait() {

        def status = genome.steps[ STEP_DEPENDENCY.getAbbreviation() ]?.status
        log.trace( "scaffolding step status=${status}" )
        return (status != FINISHED.toString()
            &&  status != SKIPPED.toString()
            &&  status != FAILED.toString())

    }


    @Override
    void setup() throws Throwable {

//        log.debug( "setup: genome-id=${genome.id}" )

    }


    @Override
    void runStep() throws Throwable {

        log.debug( "run: genome.id=${genome.id}" )

        // build process
        setStatus( SUBMITTING )
        ProcessBuilder pb = new ProcessBuilder()
            .directory( mlstPath.toFile() )
            .redirectErrorStream( true )


        def env = pb.environment() // set Java max heap space to 2 Gb
        String pathEnv = env.get( '' )
        env.put( 'JAVA_OPTS', '-Xmx2G')


        if( localMode ) {
            pb.redirectOutput( mlstPath.resolve( "${genomeName}.std.log" ).toFile() )
        } else {
            pb.command( 'qsub',
                '-b', 'y',
                '-sync', 'y',
                '-V', // export all env vars to cluster job
                '-N', 'asap-mlst',
                '-l', "virtual_free=${QSUB_FREE_MEM}G".toString(),
                '-o', mlstPath.resolve( "${genomeName}.stdout.log" ).toString(),
                '-e', mlstPath.resolve( "${genomeName}.stderr.log" ).toString() )
            .redirectOutput( mlstPath.resolve( "${genomeName}.qsub.log" ).toFile() )
        }


        List<String> cmd = pb.command()
        cmd << GROOVY_PATH
            cmd << MLST_SCRIPT_PATH
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
            throw new IllegalStateException( "abnormal MLST exit code! exitCode=${exitCode}" )


        // check state.failed / state.finished with exponential backoff
        int sec=1
        while( sec < (1<<EXP_BACKOFF_EXP) ) { // wait 1023 s (~ 17 min) in total
            try{
                sleep( sec * 1000 )
            } catch( InterruptedException ie ) {}
            log.debug( "genome.id=${genome.id}: exp backoff=${sec} s" )
            if( Files.exists( mlstPath.resolve( "${genomeName}.failed" ) ) )
                throw new IllegalStateException( "abnormal MLST state: failed" )
            else if( Files.exists( mlstPath.resolve( "${genomeName}.finished" ) ) )
                break
            sec <<= 1
        }
        if( sec >= (1<<EXP_BACKOFF_EXP)  &&  !Files.exists( mlstPath.resolve( "${genomeName}.finished" ) ) )
            throw new IllegalStateException( "abnormal MLST state: !finished, timeout=${sec} s" )

    }


    @Override
    void clean() throws Throwable  {

        log.debug( "clean: genome.id=${genome.id}" )
        
        mlstPath.toFile().eachFileMatch( groovy.io.FileType.FILES, ~/$genomeName\..+\.log/, {
            if( it.length() == 0 ) {
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

