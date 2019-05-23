
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
class ScaffoldingStep extends GenomeStep {

    private static final String SCAFFOLDING_PATH = "${ASAP_HOME}/scripts/asap-scaffolding.groovy"

    private static final GenomeSteps STEP_DEPENDENCY = GenomeSteps.ASSEMBLY

    private Path   genomePath


    ScaffoldingStep( def config, def genome, boolean localMode ) {

        super( GenomeSteps.SCAFFOLDING.getAbbreviation(), config, genome, localMode )

        setName( "Scaffolding-Step-Thread-${genome.id}" )

    }


    @Override
    boolean isSelected() {

        return genome?.stepselection.contains( GenomeSteps.SCAFFOLDING.getCharCode() )

    }


    @Override
    boolean check() {

        log.trace( "check: genome.id=${genome.id}" )
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
                    log.trace( "${GenomeSteps.SCAFFOLDING.getName()} step slept for 1 min" )
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
        log.trace( "assembly step status=${status}" )
        return (status != FINISHED.toString()
            &&  status != SKIPPED.toString()
            &&  status != FAILED.toString())

    }


    @Override
    void setup() throws Throwable {

        log.trace( "setup genome-id=${genome.id}" )

        // build names, files, directories...
        genomePath = Paths.get( projectPath.toString(), PROJECT_PATH_SCAFFOLDS, genomeName )
        try {
            if( Files.exists( genomePath ) ) {
                genomePath.toFile().deleteDir()
                log.debug( "run ${genome.id}: existing dir \"${genomePath}\" deleted!" )
            }
            Files.createDirectory( genomePath )
            log.debug( "run ${genome.id}: dir \"${genomePath}\" created" )
        } catch( IOException ioe ) {
            log.error( "run ${genome.id}: dir \"${genomeName}\" could not be created!" )
            throw ioe
        }

    }


    @Override
    void runStep() throws Throwable {

        log.trace( "run genome-id=${genome.id}" )

        // build process
        setStatus( SUBMITTING )
        ProcessBuilder pb = new ProcessBuilder()
            .directory( projectPath.toFile() )
            .redirectErrorStream( true )


        if( localMode ) {
            pb.redirectOutput( genomePath.resolve( 'std.log' ).toFile() )
        } else {
            pb.command( 'qsub',
                '-b', 'y',
                '-sync', 'y',
                '-V', // export all env vars to cluster job
                '-N', 'asap-scaf',
                '-o', genomePath.resolve( 'stdout.log' ).toString(),
                '-e', genomePath.resolve( 'stderr.log' ).toString() )
            .redirectOutput( genomePath.resolve( 'qsub.log' ).toFile() )
        }


        List<String> cmd = pb.command()
        cmd << GROOVY_PATH
            cmd << SCAFFOLDING_PATH
        cmd << '--project-path'
            cmd << projectPath.toString()
        cmd << '--genome-id'
            cmd << Integer.toString( genome.id )


        // start and wait for process to exit
        log.debug( "genome.id=${genome.id}: exec=${pb.command()}" )
        Process ps = pb.start()
        setStatus( RUNNING )
        int exitCode = ps.waitFor()


        // check exit code
        if( exitCode != 0 )
            throw new IllegalStateException( "abnormal ${GenomeSteps.SCAFFOLDING.getName()} exit code! exitCode=${exitCode}" )


        // check state.failed / state.finished with exponential backoff
        int sec=1
        while( sec < (1<<EXP_BACKOFF_EXP) ) { // wait 1023 s (~ 17 min) in total
            try{
                sleep( sec * 1000 )
            } catch( InterruptedException ie ) {}
            log.debug( "genome.id=${genome.id}: exp backoff=${sec} s" )
            if( Files.exists( genomePath.resolve( 'state.failed' ) ) )
                throw new IllegalStateException( "abnormal ${GenomeSteps.SCAFFOLDING.getName()} state: failed" )
            else if( Files.exists( genomePath.resolve( 'state.finished' ) ) )
                break
            sec <<= 1
        }
        if( sec >= (1<<EXP_BACKOFF_EXP)  &&  !Files.exists( genomePath.resolve( 'state.finished' ) ) )
            throw new IllegalStateException( "abnormal ${GenomeSteps.SCAFFOLDING.getName()} state: !finished, timeout=${sec} s" )

    }


    @Override
    void clean() throws Throwable  {

        log.trace( "genome.id=${genome.id}: clean" )
        genomePath.eachFile( groovy.io.FileType.FILES, {
            File file = it.toFile()
            if( file.name.endsWith( '.log' )  &&  file.length() == 0 ) {
                log.debug( "remove empty log file: ${file}" )
                try{
                    Files.delete( it )
                } catch( Exception ex ) {
                    log.warn( "could not delete file: ${file}", ex )
                }
            }
        } )

        if( success ) {
            try{
                Files.delete( genomePath.resolve( 'stdout.qsub.log' ) )
            } catch( Exception ex ) {
                log.warn( 'could not delete file: stdout.qsub.log', ex )
            }
        }

    }

}

