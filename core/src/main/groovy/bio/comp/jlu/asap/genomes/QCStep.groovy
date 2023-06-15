
package bio.comp.jlu.asap.genomes


import java.io.IOException
import java.nio.file.*
import groovy.util.logging.Slf4j
//import groovy.io.FileType
import bio.comp.jlu.asap.api.FileType
import bio.comp.jlu.asap.api.DataType
import bio.comp.jlu.asap.api.GenomeSteps

import static bio.comp.jlu.asap.ASAPConstants.*
import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class QCStep extends GenomeStep {

    private static final String QC_SCRIPT_PATH = "${ASAP_HOME}/scripts/asap-qc.groovy"
    private static final String QSUB_SLOTS = '8'
    private static final String QSUB_FREE_MEM = '2' // 10 Gig Memory divided by 8 PE instances, rounded -> 2

    private Path   genomePath


    QCStep( def config, def genome, boolean localMode ) {

        super( GenomeSteps.QC.getAbbreviation(), config, genome, localMode )

        setName( "QC-Step-Thread-${genome.id}" )

    }


    @Override
    boolean isSelected() {

        return genome?.stepselection.contains( GenomeSteps.QC.getCharCode() )

    }


    @Override
    boolean check() {

        log.debug( "check: genome.id=${genome.id}" )
        boolean readsProvided = false
        genome.data.each( { read ->
            if( FileType.getEnum( read.type )?.getDataType() == DataType.READS ) {
                log.debug( "genome.id=${genome.id}: found reads: read.type=${read.type}" )
                readsProvided = true
            }
        } )
        return readsProvided

    }


    @Override
    void setup() throws Throwable {

        log.debug( "setup: genome-id=${genome.id}" )

        // build names, files, directories...
        genomePath = Paths.get( projectPath.toString(), PROJECT_PATH_READS_QC, genomeName )
        try {
            if( Files.exists( genomePath ) ) {
                genomePath.toFile().deleteDir()
                log.debug( "genome.id=${genome.id}: existing dir \"${genomePath}\" deleted!" )
            }
            Files.createDirectory( genomePath )
            log.debug( "genome.id=${genome.id}: dir \"${genomePath}\" created" )
        } catch( IOException ioe ) {
            log.error( "genome.id=${genome.id}: dir \"${genomeName}\" could not be created!" )
            throw ioe
        }

    }


    @Override
    void runStep() throws Throwable {

        log.debug( "run: genome.id=${genome.id}" )

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
                '-N', 'asap-qc',
                '-pe', 'multislot', QSUB_SLOTS,
                '-l', "virtual_free=${QSUB_FREE_MEM}G".toString(),
                '-o', genomePath.resolve( 'stdout.log' ).toString(),
                '-e', genomePath.resolve( 'stderr.log' ).toString() )
            .redirectOutput( genomePath.resolve( 'qsub.log' ).toFile() )
        }


        List<String> cmd = pb.command()
        cmd << GROOVY_PATH
            cmd << QC_SCRIPT_PATH
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
            throw new IllegalStateException( "abnormal ${GenomeSteps.QC.getName()} exit code! exitCode=${exitCode}" );


        // check state.failed / state.finished with exponential backoff
        int sec=1
        while( sec < (1<<EXP_BACKOFF_EXP) ) { // wait 1023 s (~ 17 min) in total
            try{
                sleep( sec * 1000 )
            } catch( InterruptedException ie ) {}
            log.debug( "genome.id=${genome.id}: exp backoff=${sec} s" )
            if( Files.exists( genomePath.resolve( 'state.failed' ) ) )
                throw new IllegalStateException( "abnormal ${GenomeSteps.QC.getName()} state: failed" )
            else if( Files.exists( genomePath.resolve( 'state.finished' ) ) )
                break
            sec <<= 1
        }
        if( sec >= (1<<EXP_BACKOFF_EXP)  &&  !Files.exists( genomePath.resolve( 'state.finished' ) ) )
            throw new IllegalStateException( "abnormal ${GenomeSteps.QC.getName()} state: !finished, timeout=${sec} s" )

    }


    @Override
    void clean() throws Throwable  {

        log.debug( "clean: genome.id=${genome.id}" )
        
        genomePath.eachFile( groovy.io.FileType.FILES, {
            File file = it.toFile ()
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

