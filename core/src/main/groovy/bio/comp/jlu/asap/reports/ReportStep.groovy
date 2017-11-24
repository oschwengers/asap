
package bio.comp.jlu.asap.reports


import groovy.util.logging.Slf4j
import java.nio.file.Path
import freemarker.template.Configuration
import bio.comp.jlu.asap.api.AnalysesSteps
import bio.comp.jlu.asap.api.RunningStates
import bio.comp.jlu.asap.Step

import static bio.comp.jlu.asap.api.AnalysesSteps.*
import static bio.comp.jlu.asap.api.MiscConstants.*
import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
abstract class ReportStep extends Step {

    protected final Path reportsPath
    protected final Configuration templateConfiguration
    protected final def model


    ReportStep( String stepName, def config, Configuration templateConfiguration ) {

        super( stepName, config, true )

        if( templateConfiguration == null )
            throw new NullPointerException( 'templateConfiguration is null!' )


        this.templateConfiguration = templateConfiguration

        if( !config.reports )
            config.reports = [:]

        config.reports[ stepName ] = [
            start: (new Date()).format( DATE_FORMAT ),
            status: INIT.toString()
        ]

        reportsPath = projectPath.resolve( PROJECT_PATH_REPORTS )

        // init model object
        model = [
            project: config.project,
            user: config.user,
            menu: setupMenu( config )
        ]

    }


    @Override
    boolean check() {

        return true

    }


    @Override
    boolean isSelected() {

        return true

    }


    @Override
    public void setStatus( RunningStates status ) {

        config.reports[ stepName ].status = status.toString()

    }


    @Override
    public RunningStates getStatus() {

        return RunningStates.getEnum( config.reports[ stepName ].status )

    }


    @Override
    void run() {

        log.trace( "${stepName} running..." )
        try {

            if( check() ) {

                setStatus( SETUP )
                setup()

                setStatus( RUNNING )
                runStep()

                clean()

                setStatus( FINISHED )
                success = true

            } else {
                log.warn( "skip ${stepName} report step upon failed check!" )
                success = false
                setStatus( SKIPPED )
            }

        } catch( Throwable ex ) {
            log.error( "${stepName} reporting step aborted!", ex )
            success = false
            setStatus( FAILED )
            config.reports[ stepName ].error = ex.getLocalizedMessage()
        }

        config.reports[ stepName ].end = (new Date()).format( DATE_FORMAT )

    }


    private static def setupMenu( def config ) {

        def menu = [:]

        if( config.project.comp ) {
            menu.analyses = [
                CORE_PAN,
                PHYLOGENY
            ].collect( { step ->
                [
                    link: step.getAbbreviation(),
                    name: step.getName()
                ]
            } )
        } else {
            menu.analyses = []
        }

        return menu

    }

}

