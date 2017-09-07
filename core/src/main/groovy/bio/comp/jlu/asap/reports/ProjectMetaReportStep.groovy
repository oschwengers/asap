
package bio.comp.jlu.asap.reports


import groovy.util.logging.Slf4j
import java.nio.file.Path
import freemarker.template.Configuration
import freemarker.template.Template


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class ProjectMetaReportStep extends ReportStep {


    public ProjectMetaReportStep( def config, Configuration templateConfiguration ) {

        super( 'project', config, templateConfiguration )

    }


    @Override
    boolean isSelected() {

        // is a global step so always return true here
        return true

    }


    @Override
    protected void setup() throws Throwable {

        log.trace( 'setup' )

    }


    @Override
    protected void runStep() throws Throwable {

        log.trace( 'run' )

        // build project.html
        Template template = templateConfiguration.getTemplate( 'project.ftl' )
        Writer writer = new StringWriter()
        template.process( model, writer )
        reportsPath.resolve( 'project.html' ).toFile() << writer.toString()

    }


    @Override
    protected void clean() throws Throwable {

        log.trace( 'clean' )

    }

}

