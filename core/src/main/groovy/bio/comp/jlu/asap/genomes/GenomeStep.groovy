
package bio.comp.jlu.asap.genomes


import groovy.util.logging.Slf4j
import bio.comp.jlu.asap.api.GenomeSteps
import bio.comp.jlu.asap.api.RunningStates
import bio.comp.jlu.asap.Step

import static bio.comp.jlu.asap.api.MiscConstants.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
abstract class GenomeStep extends Step {

    protected final def genome

    protected String genomeName


    GenomeStep( String stepName, def config, def genome, boolean localMode ) {

        super( stepName, config, localMode )

        if( genome == null )
            throw new NullPointerException( 'genome is null!' )

        this.genome = genome

        genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"

        if( genome.steps )
            genome.steps << [ (stepName): [ status: INIT.toString() ] ]
        else {
            genome.steps = [
                (stepName): [
                    status: INIT.toString()
                ]
            ]
        }

    }


    public boolean hasStepFinished( GenomeSteps step ) {

        return genome.steps[ step.getAbbreviation() ]?.status == FINISHED.toString()

    }


    @Override
    public void setStatus( RunningStates status ) {

        genome.steps[ stepName ].status = status.toString()

    }


    @Override
    public RunningStates getStatus() {

        return RunningStates.getEnum( genome.steps[ stepName ].status )

    }


    @Override
    void run() {

        log.trace( "${stepName} running..." )
        try {

            if( check() ) {

                setStatus( SETUP )
                setup()

                setStatus( RUNNING )
                genome.steps[ stepName ].start = (new Date()).format( DATE_FORMAT )
                runStep()

                clean()
                genome.steps[ stepName ].end = (new Date()).format( DATE_FORMAT )
                setStatus( FINISHED )
                success = true

            } else {
                log.warn( "skip ${stepName} genome step for genome id=${genome.id} upon failed check!" )
                success = false
                setStatus( SKIPPED )
            }

        } catch( Throwable ex ) {
            log.error( "${stepName} genome step for genome id=${genome.id} aborted!", ex )
            success = false
            setStatus( FAILED )
            genome.steps[ stepName ].error = ex.getLocalizedMessage()
        }

    }

}

