
package bio.comp.jlu.asap.analyses


import java.io.IOException
import java.nio.file.*
import groovy.util.logging.Slf4j
import groovy.io.FileType

import static bio.comp.jlu.asap.ASAPConstants.*
import static bio.comp.jlu.asap.api.AnalysesSteps.*
import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class PhylogenyAnalysis extends AnalysisStep {

    private static final String PHYLOGENY_SCRIPT_PATH = "${ASAP_HOME}/scripts/asap-phylogeny.groovy"
    private static final String QSUB_FREE_MEM = '10'

    private final Path phylogenyPath


    PhylogenyAnalysis( def config, boolean localMode ) {

        super( PHYLOGENY.getAbbreviation(), config, localMode )

        setName( 'Phylogeny-Analysis-Thread' )

        // build necessary paths
        phylogenyPath = projectPath.resolve( PROJECT_PATH_PHYLOGENY )

    }


    @Override
    boolean check() {

//        log.debug( 'check' )

        return true

    }


    @Override
    void setup() throws Throwable {

        log.debug( 'setup' )

        // check analyes/phylotree directory
        try {
            if( Files.exists( phylogenyPath ) ) {
                phylogenyPath.toFile().deleteDir()
                log.debug( "existing dir \"${phylogenyPath}\" deleted!" )
            }
            Files.createDirectory( phylogenyPath )
            log.debug( "dir \"${phylogenyPath}\" created" )
        } catch( IOException ioe ) {
            log.error( "dir \"${phylogenyPath}\" could not be created!" )
            throw ioe
        }

    }


    @Override
    void runStep() throws Throwable {

        log.debug( 'run' )

        // build process
        ProcessBuilder pb = new ProcessBuilder()
            .directory( projectPath.toFile() )
            .redirectErrorStream( true )


        if( localMode ) {
            pb.redirectOutput( phylogenyPath.resolve( 'std.log' ).toFile() )
        } else {
            pb.command( 'qsub',
                '-b', 'y',
                '-sync', 'y',
                '-V', // export all env vars to cluster job
                '-N', 'asap-phyl',
                '-l', "virtual_free=${QSUB_FREE_MEM}G".toString(),
                '-o', phylogenyPath.resolve( 'stdout.log' ).toString(),
                '-e', phylogenyPath.resolve( 'stderr.log' ).toString() )
            .redirectOutput( phylogenyPath.resolve( 'qsub.log' ).toFile() )
        }


       List<String> cmd = pb.command()
        cmd << GROOVY_PATH
            cmd << PHYLOGENY_SCRIPT_PATH
        cmd << '--project-path'
            cmd << projectPath.toString()


        // start process
        log.debug( "start phylo tree process, exec: ${pb.command()}" )
        Process ps = pb.start()


        // waiting for process
        int exitCode = ps.waitFor()
        if( exitCode != 0 )
            throw new IOException( "abnormal phylo tree exit code! exitCode=${exitCode}" )

    }


    @Override
    void clean() throws Throwable  {

        log.debug( 'clean' )
        
        phylogenyPath.toFile().eachFile( FileType.FILES, {
            if( it.getName().endsWith( '.log' )  &&  it.length() == 0 ) {
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

