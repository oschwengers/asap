
package bio.comp.jlu.asap.analyses


import groovy.util.logging.Slf4j
import java.nio.file.Files
import bio.comp.jlu.asap.api.RunningStates
import bio.comp.jlu.asap.Step

import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class AnalysesRunner extends AnalysisStep {


    AnalysesRunner( def config, boolean localMode ) {

        super( 'pipeline', config, localMode )

        setName( 'Analyses-Runner-Thread' )

    }


    @Override
    boolean isSelected() {

        // global step so always return true
        return true

    }


    @Override
    boolean check() {

        return true

    }


    @Override
    void setup() throws Throwable {

        log.trace( 'setup' )

    }


    @Override
    void runStep() throws Throwable {

        log.trace( 'run' )

        // create instances of all available analyses steps
        def analysesSteps = [
            new PhylogenyAnalysis( config, localMode ),
            new CorePanGenomeAnalysis( config, localMode )
        ]


        // start all analyses steps
        analysesSteps.each( {
            it.start()
        } )


        // wait for analyses steps to finish
        analysesSteps.each( {
            it.waitFor()
        } )

    }


    @Override
    void clean() throws Throwable {

        log.trace( 'clean' )

    }

}

