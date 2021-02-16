
package bio.comp.jlu.asap.steps


import java.nio.file.*
import java.time.*
import groovy.util.logging.Slf4j
import bio.comp.jlu.asap.api.RunningStates
import bio.comp.jlu.asap.Step

import static bio.comp.jlu.asap.ASAPConstants.*
import static bio.comp.jlu.asap.api.RunningStates.*
import static bio.comp.jlu.asap.api.Paths.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class MappingIndices extends Step {

    public static final String STEP_ABBR = 'mappingIndices'


    MappingIndices( def config ) {

        super( STEP_ABBR, config, true )

        config.steps[ stepName ] = [
            status: INIT.toString()
        ]

    }


    @Override
    boolean isSelected() {

        return true

    }


    @Override
    public void setStatus( RunningStates status ) {

        config.steps[ stepName ].status = status.toString()

    }


    @Override
    public RunningStates getStatus() {

        return RunningStates.getEnum( config.steps[ stepName ].status )

    }


    @Override
    void run() {

//        log.debug( 'run' )
        config.steps[ stepName ].start = OffsetDateTime.now().toString()


        try {

            if( check() ) {

                setStatus( SETUP )
                setup()

                setStatus( RUNNING )
                runStep()

                clean()

                setStatus( FINISHED )
                success = true
                log.info( "finished ${stepName} step" )

            } else {
                log.warn( "skip ${stepName} step! check failed!" )
                success = false
                setStatus( SKIPPED )
            }

        } catch( Throwable ex ) {
            log.error( "${stepName} step aborted!", ex )
            success = false
            setStatus( FAILED )
            config.steps[ stepName ].error = ex.getLocalizedMessage()
        }

        config.steps[ stepName ].end = OffsetDateTime.now().toString()

    }




    @Override
    boolean check() {

        log.debug( 'check' )
        // wait for mapping step
        long waitingTime = System.currentTimeMillis()
        while( shouldWait() ) {
            if( System.currentTimeMillis() - waitingTime > MAX_STEP_WAITING_PERIOD ) {
                log.warn( "max waiting period (${MAX_STEP_WAITING_PERIOD} s) exceeded!" )
                return false
            }
            try {
                sleep( 1000 * 10 )
                log.trace( "${stepName} step slept for 1 min" )
            }
            catch( Throwable t ) { log.error( 'Error: could not sleep!', t ) }
        }

        // check necessary reference processing status
        return config.steps[ ReferenceProcessings.STEP_ABBR ]?.status == FINISHED.toString()

    }


    private boolean shouldWait() {

        def status = config?.steps[ ReferenceProcessings.STEP_ABBR ]?.status
        log.trace( "reference processing step status=${status}" )
        return (status != FINISHED.toString()
            &&  status != SKIPPED.toString()
            &&  status != FAILED.toString())

    }


    @Override
    void setup() throws Throwable {

//        log.debug( 'setup' )

    }


    @Override
    void runStep() throws Throwable {

        log.debug( 'run' )

        // build bowtie2 indices
        Path referencesPath = projectPath.resolve( PROJECT_PATH_REFERENCES )
        config.references.each( { ref ->

            String fileName = ref.substring( 0, ref.lastIndexOf( '.' ) )
            Path fastaPath  = referencesPath.resolve( "${fileName}.fasta" )
            log.debug( "reference-file: ${ref}, fileName: ${fileName}, fasta: ${fastaPath}" )
            ProcessBuilder pb = new ProcessBuilder( 'bowtie2-build', "${fastaPath}", "${fileName}" )
                pb.directory( referencesPath.toFile() )
            log.debug( "exec: ${pb.command()}" )
            log.debug( '----------------------------------------------------------------------------------------------' )
            int exitCode = pb.start().waitFor()
            if( exitCode != 0 )  throw new IllegalStateException( "exitCode = ${exitCode}" )
            log.debug( '----------------------------------------------------------------------------------------------' )

        } )

    }


    @Override
    void clean() throws Throwable {

//        log.debug( 'clean' )

    }

}

