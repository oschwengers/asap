
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
class CorePanGenomeAnalysis extends AnalysisStep {

    private static final String COREPAN_SCRIPT_PATH = "${ASAP_HOME}/scripts/asap-corepan.groovy"

    private final Path corePanPath


    CorePanGenomeAnalysis( def config, boolean localMode ) {

        super( CORE_PAN.getAbbreviation(), config, localMode )

        setName( 'CorePan-Analysis-Thread' )

        // build necessary paths
        corePanPath = projectPath.resolve( PROJECT_PATH_CORE_PAN )

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
            if( Files.exists( corePanPath ) ) {
                corePanPath.toFile().deleteDir()
                log.debug( "existing dir \"${corePanPath}\" deleted!" )
            }
            Files.createDirectory( corePanPath )
            log.debug( "dir \"${corePanPath}\" created" )
        } catch( IOException ioe ) {
            log.error( "dir \"${corePanPath}\" could not be created!" )
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


        /**
         * Due to a strange bug in Roary
         * cluster submission was disabled.
         * Bug: Ambiguous output redirect.
         * Cant find blast results: /var/scratch/tmp-1489497595629-814/HQnafH6Ntr/0.seq.out00:53:34.891
        */

//        if( localMode ) {
            pb.redirectOutput( corePanPath.resolve( 'std.log' ).toFile() )
//        } else {
//            pb.command( 'qsub',
//                '-b', 'y',
//                '-sync', 'y',
//                '-V', // export all env vars to cluster job
//                '-N', 'asap-phyl',
//                '-o', corePanPath.resolve( 'stdout.log' ).toString(),
//                '-e', corePanPath.resolve( 'stderr.log' ).toString() )
//            .redirectOutput( corePanPath.resolve( 'qsub.log' ).toFile() )
//        }


       List<String> cmd = pb.command()
        cmd << GROOVY_PATH
            cmd << COREPAN_SCRIPT_PATH
        cmd << '--project-path'
            cmd << projectPath.toString()


        // start process
        log.debug( "start core/pan genome process, exec: ${pb.command()}" )
        Process ps = pb.start()


        // waiting for process
        int exitCode = ps.waitFor()
        if( exitCode != 0 )
            throw new IOException( "abnormal core/pan exit code! exitCode=${exitCode}" )

    }


    @Override
    void clean() throws Throwable  {

        log.debug( 'clean' )

        corePanPath.eachFile( FileType.FILES, {
            File file = it.toFile()
            if( file.name.endsWith( '.log' )  &&  file.length() == 0 ) {
                try{
                    Files.delete( it )
                } catch( Exception ex ) {
                    log.warn( "could not delete file: ${file}", ex )
                }
            }
        } )

    }

}

