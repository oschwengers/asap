
package bio.comp.jlu.asap.genomes


import groovy.util.logging.Slf4j
import java.nio.file.Path
import java.nio.file.Paths
import java.text.DateFormat

import static bio.comp.jlu.asap.api.GenomeSteps.*
import static bio.comp.jlu.asap.api.MiscConstants.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
@Slf4j
class GenomeRunner extends GenomeStep {


    GenomeRunner( def config, def genome, boolean localMode ) {

        super( 'pipeline', config, genome, localMode )

        setName( 'Genome-Runner-Thread' )

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

        log.info( "genome-id=${genome.id}: running..." )

        // create instances of all available analyses steps
        def genomeSteps = [
            new QCStep( config, genome, localMode ),
            new TaxonomyStep( config, genome, localMode ),
            new MappingStep( config, genome, localMode ),
            new SNPDetectionStep( config, genome, localMode ),
            new AssemblyStep( config, genome, localMode ),
            new ScaffoldingStep( config, genome, localMode ),
            new MLSTStep( config, genome, localMode ),
            new ABRDetectionStep( config, genome, localMode ),
            new AnnotationStep( config, genome, localMode ),
            new VFDetectionStep( config, genome, localMode )
        ]


        // discard unselected analyses steps
        genomeSteps.findAll( { !it.isSelected() } ).each( { it.setStatus( SKIPPED ) } )
        genomeSteps = genomeSteps.findAll( { it.isSelected() } )


        // start all analyses steps
        genomeSteps.each( {
            it.start()
        } )


        // wait for analyses steps to finish
        genomeSteps.each( {
            it.waitFor()
        } )

    }


    @Override
    void clean() throws Throwable {

        log.trace( 'clean' )

    }

}

