
package bio.comp.jlu.asap.reports


import java.nio.file.*
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import freemarker.template.Configuration
import freemarker.template.Template
import bio.comp.jlu.asap.Misc

import static bio.comp.jlu.asap.api.GenomeSteps.*
import static bio.comp.jlu.asap.api.MiscConstants.*
import static bio.comp.jlu.asap.api.Paths.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
@Slf4j
class HelpReportStep extends ReportStep {


    public HelpReportStep( def config, Configuration templateConfiguration ) {

        super( 'help', config, templateConfiguration )

    }


    @Override
    boolean isSelected() {

        // is a global step so always return true here
        return true

    }


    @Override
    protected void setup() throws Throwable {

        log.trace( 'setup' )

        // Perform any init logic here

    }


    @Override
    protected void runStep() throws Throwable {

        log.trace( 'run' )

        Template template = templateConfiguration.getTemplate( 'help.ftl' )
        Writer writer = new StringWriter()
        template.process( model, writer )
        reportsPath.resolve( 'help.html' ).toFile() << writer.toString()

    }


    @Override
    protected void clean() throws Throwable {

        log.trace( 'clean' )

    }

}

