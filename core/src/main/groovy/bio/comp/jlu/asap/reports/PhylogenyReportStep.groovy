package bio.comp.jlu.asap.reports

import groovy.util.logging.Slf4j
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import freemarker.template.Configuration
import freemarker.template.Template
import bio.comp.jlu.asap.Misc

import static bio.comp.jlu.asap.api.AnalysesSteps.*
import static bio.comp.jlu.asap.api.Paths.PROJECT_PATH_PHYLOGENY
import static bio.comp.jlu.asap.api.RunningStates.*

/**
 * Created by Rolf Hilker on 01.12.2015.
 */
@Slf4j
class PhylogenyReportStep extends ReportStep {

    private final Path phylogenyReportPath


    PhylogenyReportStep( def config, Configuration templateConfiguration ) {

        super( 'Phylogeny', config, templateConfiguration)

        phylogenyReportPath = reportsPath.resolve( PROJECT_PATH_PHYLOGENY )

    }


    @Override
    protected void setup() throws Throwable {

        log.trace( 'setup' )

        if( config?.analyses[ PHYLOGENY.getAbbreviation() ]?.status == FINISHED.toString() ) {

            Files.createDirectory( phylogenyReportPath )

            // copy CSS / JS files
            InputStream is =  getClass().getResourceAsStream( '/bio/comp/jlu/asap/reports/templates/phylogeny/phylocanvas-asap.js' )
            Files.copy( is, Paths.get( reportsPath.toString(), 'js', 'phylocanvas-asap.js' ), StandardCopyOption.REPLACE_EXISTING )

            // hard link download files
            Files.createLink( phylogenyReportPath.resolve( 'tree.nwk' ), Paths.get( projectPath.toString(), PROJECT_PATH_PHYLOGENY, 'tree.nwk' ) )

        }
    }


    @Override
    protected void runStep() throws Throwable {

        log.trace( 'run' )

        // build pyholgeny.html
        model.status = config.analyses[ PHYLOGENY.getAbbreviation() ]?.status

        Template template
        def snpPhyloTreeStep = config.analyses[ PHYLOGENY.getAbbreviation() ]
        if( snpPhyloTreeStep?.status == FINISHED.toString() ) {
            model.runtime = [
                start: snpPhyloTreeStep.start,
                end:   snpPhyloTreeStep.end
            ]

            model.tree = phylogenyReportPath.resolve( 'tree.nwk' ).toFile().text.replaceAll( '\n', '' )

            template = templateConfiguration.getTemplate( 'phylogeny.ftl' )
        } else if( snpPhyloTreeStep?.status == SKIPPED.toString() ) {
            model.report = 'SNP Phylogeny'
            model.step   = PHYLOGENY.getName()
            template = templateConfiguration.getTemplate( 'skipped.ftl' )
        } else {
            model.error  = snpPhyloTreeStep.error
            model.report = 'SNP Phylogeny'
            model.step   = PHYLOGENY.getName()
            template = templateConfiguration.getTemplate( 'error.ftl' )
        }

        Writer writer = new StringWriter()
        template.process( model, writer )
        reportsPath.resolve( "${PHYLOGENY.getAbbreviation()}.html" ).toFile() << writer.toString()

    }


    @Override
    protected void clean() throws Throwable {

        log.trace( 'clean' )

    }

}
