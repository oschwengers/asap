
package bio.comp.jlu.asap.genomes


import groovy.util.logging.Slf4j
import groovy.io.FileType
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import bio.comp.jlu.asap.api.FileType
import bio.comp.jlu.asap.api.DataType

import static bio.comp.jlu.asap.ASAPConstants.*
import static bio.comp.jlu.asap.api.GenomeSteps.*
import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class TaxonomyStep extends GenomeStep {

    private static final String TAXONOMY_SCRIPT_PATH = "${ASAP_HOME}/scripts/asap-taxonomy.groovy"
    private static final String QSUB_SLOTS = '5'
    private static final String QSUB_FREE_MEM = '2' // 10 Gig Memory divided by 5 PE instances -> 2

    private Path   taxPath = projectPath.resolve( PROJECT_PATH_TAXONOMY )


    TaxonomyStep( def config, def genome, boolean localMode ) {

        super( TAXONOMY.getAbbreviation(), config, genome, localMode )

        setName( "Taxonomy-Step-Thread-${genome.id}" )

    }


    @Override
    boolean isSelected() {

        return genome?.stepselection.contains( TAXONOMY.getCharCode() )

    }


    @Override
    boolean check() {

        log.trace( "check: genome.id=${genome.id}" )
        if( genome?.stepselection.contains( SCAFFOLDING.getCharCode() ) ) {
            long waitingTime = System.currentTimeMillis()
            while( shouldWait() ) {
                if( System.currentTimeMillis() - waitingTime > MAX_STEP_WAITING_PERIOD ) {
                    log.warn( "max waiting period (${MAX_STEP_WAITING_PERIOD} s) exceeded!" )
                    return false
                }
                try {
                    sleep( 1000 * 60 )
                    log.trace( "${TAXONOMY.getName()} step slept for 1 min" )
                }
                catch( Throwable t ) { log.error( 'Error: could not sleep!', t ) }
            }

            // check necessary scaffolding analysis status
            return hasStepFinished( SCAFFOLDING )

        } else
            return true

    }


    private boolean shouldWait() {

        def status = genome.steps[ SCAFFOLDING.getAbbreviation() ]?.status
        log.trace( "scaffolding step status=${status}" )
        return (status != FINISHED.toString()
            &&  status != SKIPPED.toString()
            &&  status != FAILED.toString())

    }


    @Override
    void setup() throws Throwable {

        log.trace( "setup genome-id=${genome.id}" )

    }


    @Override
    void runStep() throws Throwable {

        log.trace( "genome.id=${genome.id}: run" )

        // build processes
        setStatus( SUBMITTING )
        ProcessBuilder pb = new ProcessBuilder()
            .directory( projectPath.toFile() )
            .redirectErrorStream( true )


        if( localMode ) {
            pb.redirectOutput( taxPath.resolve( "${genomeName}.std.log" ).toFile() )
        } else {
            pb.command( 'qsub',
                '-b', 'y',
                '-sync', 'y',
                '-V', // export all env vars to cluster job
                '-N', 'asap-tax',
                '-pe', 'multislot', QSUB_SLOTS,
                '-l', "virtual_free=${QSUB_FREE_MEM}G".toString(),
                '-o', taxPath.resolve( "${genomeName}.stdout.log" ).toString(),
                '-e', taxPath.resolve( "${genomeName}.stderr.log" ).toString() )
            .redirectOutput( taxPath.resolve( "${genomeName}.qsub.log" ).toFile() )
        }


        List<String> cmd = pb.command()
        cmd << GROOVY_PATH
            cmd << TAXONOMY_SCRIPT_PATH
        cmd << '--project-path'
            cmd << projectPath.toString()
        cmd << '--genome-id'
            cmd << Integer.toString( genome.id )


        // start and wait for process to exit
        log.debug( "genome.id=${genome.id}: exec: ${pb.command()}" )
        Process ps = pb.start()
        setStatus( RUNNING )
        int exitCode= ps.waitFor()


        // check exit code
        if( exitCode!= 0 )
            throw new IllegalStateException( "abnormal taxonomy exit code! exitCode=${exitCode}" );


        // check state.failed / state.finished with exponential backoff
        int sec=1
        while( sec < (1<<EXP_BACKOFF_EXP) ) { // wait 1023 s (~ 17 min) in total
            try{
                sleep( sec * 1000 )
            } catch( InterruptedException ie ) {}
            log.debug( "genome.id=${genome.id}: exp backoff=${sec} s" )
            if( Files.exists( taxPath.resolve( "${genomeName}.failed" ) ) )
                throw new IllegalStateException( "abnormal taxonomy state: failed" )
            else if( Files.exists( taxPath.resolve( "${genomeName}.finished" ) ) )
                break
            sec <<= 1
        }
        if( sec >= (1<<EXP_BACKOFF_EXP)  &&  !Files.exists( taxPath.resolve( "${genomeName}.finished" ) ) )
            throw new IllegalStateException( "abnormal taxonomy state: !finished, timeout=${sec} s" )

    }


    @Override
    void clean() throws Throwable  {

        log.trace( "genome.id=${genome.id}: clean" )
        taxPath.eachFileMatch( groovy.io.FileType.FILES, ~/$genomeName\..+\.log/, {
            if( it.toFile().length() == 0 ) {
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

