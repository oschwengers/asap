
package bio.comp.jlu.asap.genomes


import groovy.io.FileType
import groovy.util.logging.Slf4j
import java.io.IOException
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
class SNPDetectionStep extends GenomeStep {

    private static final String SNP_DETECTION_SCRIPT_PATH = "${ASAP_HOME}/scripts/asap-snp.groovy"

    private static final GenomeSteps STEP_DEPENDENCY = GenomeSteps.MAPPING

    private final Path snpDetectionPath


    SNPDetectionStep( def config, def genome, boolean localMode ) {

        super( GenomeSteps.SNP_DETECTION.getAbbreviation(), config, genome, localMode )

        setName( "SNPDetection-Step-Thread-${genome.id}" )

        // build necessary paths
        snpDetectionPath = projectPath.resolve( PROJECT_PATH_SNPS )

    }


    @Override
    boolean isSelected() {

        return genome?.stepselection.contains( GenomeSteps.SNP_DETECTION.getCharCode() )

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
                log.trace( "${GenomeSteps.SNP_DETECTION.getName()} step slept for 1 min" )
            }
            catch( Throwable t ) { log.error( 'Error: could not sleep!', t ) }
        }

        // check necessary mapping analysis status
        return hasStepFinished( STEP_DEPENDENCY )

    }


    private boolean shouldWait() {

        def status = genome.steps[ STEP_DEPENDENCY.getAbbreviation() ]?.status
        log.trace( "SNP detection step status=${status}" )
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

        // submit SNP detection job
        ProcessBuilder pb = new ProcessBuilder()
            .directory( projectPath.toFile() )
            .redirectErrorStream( true )


        if( localMode ) {
            pb.redirectOutput( snpDetectionPath.resolve( "${genomeName}.std.log" ).toFile() )
        } else {
            pb.command( 'qsub',
                '-b', 'y',
                '-sync', 'y',
                '-V', // export all env vars to cluster job
                '-N', 'asap-snp',
                '-o', snpDetectionPath.resolve( "${genomeName}.stdout.log" ).toString(),
                '-e', snpDetectionPath.resolve( "${genomeName}.stderr.log" ).toString() )
            .redirectOutput( snpDetectionPath.resolve( "${genomeName}.qsub.log" ).toFile() )
        }


        List<String> cmd = pb.command()
        cmd << GROOVY_PATH
            cmd << SNP_DETECTION_SCRIPT_PATH
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
            throw new IllegalStateException( "abnormal ${GenomeSteps.SNP_DETECTION.getName()} exit code! exitCode=${exitCode}" );


        // check state.failed / state.finished with exponential backoff
        int sec=1
        while( sec < (1<<EXP_BACKOFF_EXP) ) { // wait 1023 s (~ 17 min) in total
            try{
                sleep( sec * 1000 )
            } catch( InterruptedException ie ) {}
            log.debug( "genome.id=${genome.id}: exp backoff=${sec} s" )
            if( Files.exists( snpDetectionPath.resolve( "${genomeName}.failed" ) ) )
                throw new IllegalStateException( "abnormal ${MAPPING.getName()} state: failed" )
            else if( Files.exists( snpDetectionPath.resolve( "${genomeName}.finished" ) ) )
                break
            sec <<= 1
        }
        if( sec >= (1<<EXP_BACKOFF_EXP)  &&  !Files.exists( snpDetectionPath.resolve( "${genomeName}.finished" ) ) )
            throw new IllegalStateException( "abnormal ${GenomeSteps.SNP_DETECTION.getName()} state: !finished, timeout=${sec} s" )

    }


    @Override
    void clean() throws Throwable  {

        log.debug( "clean: genome.id=${genome.id}" )
        
        snpDetectionPath.eachFileMatch( FileType.FILES, ~/${genomeName}\..+\.log/, {
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

