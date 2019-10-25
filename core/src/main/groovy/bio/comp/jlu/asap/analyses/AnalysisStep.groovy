
package bio.comp.jlu.asap.analyses


import java.time.*
import java.nio.file.Path
import groovy.util.logging.Slf4j
import bio.comp.jlu.asap.api.RunningStates
import bio.comp.jlu.asap.api.AnalysesSteps
import bio.comp.jlu.asap.Step

import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
abstract class AnalysisStep extends Step {


    AnalysisStep( String stepName, def config, boolean localMode ) {

        super( stepName, config, localMode )

        config.analyses[ stepName ] = [
            status: INIT.toString()
        ]

    }


    @Override
    boolean isSelected() {

        return true

    }


    public boolean hasStepFinished( AnalysesSteps step ) {

        return config?.analyses[ step.getAbbreviation() ]?.status == FINISHED.toString()

    }


    @Override
    public void setStatus( RunningStates status ) {

        config.analyses[ stepName ].status = status.toString()

    }


    @Override
    public RunningStates getStatus() {

        return RunningStates.getEnum( config.analyses[ stepName ].status )

    }


    @Override
    void run() {

//        log.debug( "start ${stepName}" )
        try {

            if( check() ) {

                setStatus( SETUP )
                setup()

                setStatus( RUNNING )
                config.analyses[ stepName ].start = OffsetDateTime.now().toString()
                runStep()

                clean()
                config.analyses[ stepName ].end = OffsetDateTime.now().toString()
                setStatus( FINISHED )
                success = true
                log.info( "finished ${stepName} step" )

            } else {
                log.warn( "skip ${stepName} analysis step upon failed check!" )
                success = false
                setStatus( SKIPPED )
            }

        } catch( Throwable ex ) {
            log.error( "${stepName} analysis step aborted!", ex )
            success = false
            setStatus( FAILED )
            config.analyses[ stepName ].error = ex.getLocalizedMessage()
        }

    }

}

