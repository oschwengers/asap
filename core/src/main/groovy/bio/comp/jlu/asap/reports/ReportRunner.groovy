
package bio.comp.jlu.asap.reports


import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import groovy.util.logging.Slf4j
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler

import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class ReportRunner extends ReportStep {

    private static final String        PACKAGE_PREFIX = '/bio/comp/jlu/asap/reports'
    private static final Configuration CONFIGURATION = new Configuration( Configuration.VERSION_2_3_25 )

    static {
        CONFIGURATION.setClassLoaderForTemplateLoading( getClass().getClassLoader(), "${PACKAGE_PREFIX}/templates" )
        CONFIGURATION.setDefaultEncoding( 'UTF-8' )
        CONFIGURATION.setTemplateExceptionHandler( TemplateExceptionHandler.HTML_DEBUG_HANDLER )
        CONFIGURATION.setLocale( java.util.Locale.ENGLISH )
    }


    ReportRunner( def config ) {

        super( 'pipeline', config, CONFIGURATION )

        setName( "Report-Runner-Thread" )

    }


    @Override
    boolean isSelected() {

        // is a global step so always return true here
        return true

    }


    @Override
    void setup() throws Throwable {

        log.debug( 'setup' )

        // delete & recreate reports dir (deleting all prior content!
        boolean deleted = new File( reportsPath.toString() ).deleteDir()
        Files.createDirectory( reportsPath )
        log.debug( "${deleted?'re':''}created reports folder: ${reportsPath}" )


        // create subfolders
        [
            'css', 'js', 'fonts', 'img'
        ].each{ String name ->
            Path path = reportsPath.resolve( name )
            Files.createDirectory( path )
        }


        // copy HTML statics
        [
            [ 'css', 'asap.css' ],
            [ 'css', 'dashboard.css' ],
            [ 'css', 'datatables.min.css' ],
            [ 'js', 'datatables.min.js' ],
            [ 'js', 'back-to-top.js' ],
            [ 'js', 'gradient.js' ],
            [ 'js', 'synteny.js' ],
            [ 'js', 'time.js' ],
            [ 'fonts', 'glyphicons-halflings-regular.ttf' ]
        ].each{ String pathName , String name ->
            InputStream is =  getClass().getResourceAsStream( "${PACKAGE_PREFIX}/statics/${name}" )
            Path path = reportsPath.resolve( pathName )
            Files.copy( is, path.resolve( name ), StandardCopyOption.REPLACE_EXISTING )
        }

    }


    void runStep() throws Throwable {

        log.debug( 'run' )

        // create reporting steps
        def reportSteps = [
            new IndexReportStep( config, templateConfiguration ),
            new HelpReportStep( config, templateConfiguration ),
            new QCReportStep( config, templateConfiguration ),
            new TaxClassificationReportStep( config, templateConfiguration ),
            new AssemblyReportStep( config, templateConfiguration ),
            new ScaffoldingReportStep( config, templateConfiguration ),
            new MLSTReportStep( config, templateConfiguration ),
            new AnnotationReportStep( config, templateConfiguration ),
            new ABRReportStep( config, templateConfiguration ),
            new VFReportStep( config, templateConfiguration ),
            new MappingReportStep( config, templateConfiguration ),
            new SnpReportStep( config, templateConfiguration )
        ]

        if( config.project.comp ) {
            reportSteps << new CorePanReportStep( config, templateConfiguration )
            reportSteps << new PhylogenyReportStep( config, templateConfiguration )
        }


        // start reporting steps
        reportSteps.each( {
            it.start()
        } )


        // wait for reporting steps
        reportSteps.each( {
            it.waitFor()
        } )

    }


    @Override
    void clean() throws Throwable {

        log.debug( 'clean' )

    }

}

