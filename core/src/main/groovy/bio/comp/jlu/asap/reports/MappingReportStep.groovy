
package bio.comp.jlu.asap.reports


import java.nio.file.Files
import java.nio.file.Path
import groovy.util.logging.Slf4j
import groovy.json.JsonSlurper
import freemarker.template.Configuration
import freemarker.template.Template
import bio.comp.jlu.asap.api.Paths
import bio.comp.jlu.asap.reports.ReportStep

import static bio.comp.jlu.asap.api.GenomeSteps.MAPPING
import static bio.comp.jlu.asap.api.RunningStates.*


@Slf4j
class MappingReportStep extends ReportStep {

    private final Path mappingsPath
    private final Path mappingsReportPath


    public MappingReportStep( def config, Configuration templateConfiguration ) {

        super( MAPPING.getAbbreviation(), config, templateConfiguration )

        mappingsPath = projectPath.resolve( Paths.PROJECT_PATH_MAPPINGS )
        mappingsReportPath = reportsPath.resolve( Paths.PROJECT_PATH_MAPPINGS )

    }


    @Override
    boolean isSelected() {

        return true

    }


    @Override
    protected void setup() throws Throwable {

        log.trace( 'setup' )
        Files.createDirectory( mappingsReportPath )

    }


    @Override
    protected void runStep() throws Throwable {

        log.trace( 'run' )

        def steps = [
            finished : [],
            skipped : [],
            failed : []
        ]

        // read info.json on finished jobs
        config.genomes.each( { genome ->

            def stat = [
                genome: [
                    id: genome.id,
                    species: genome.species,
                    strain: genome.strain
                ],
                status: genome.steps[ MAPPING.getAbbreviation() ]?.status
            ]

            String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
            if( Files.exists( mappingsPath.resolve( "${genomeName}.finished" ) ) ) {

                Path infoJsonPath = mappingsPath.resolve( "${genomeName}.json" )
                stat << (new JsonSlurper()).parseText( infoJsonPath.toFile().text )

                // create hard links to download files
                Files.createLink( mappingsReportPath.resolve( "${genomeName}.bam" ), mappingsPath.resolve( "${genomeName}.bam" ) )

                // conversions
                stat.ratio = (stat.ratio as double) * 100

                steps.finished << stat
            } else if( stat.status == SKIPPED.toString()  ||  Files.exists( mappingsPath.resolve( "${genomeName}.skipped" ) ) ) {
                steps.skipped << stat
            } else {
                steps.failed << stat
            }

        } )


        // build annotations.html
        model.steps = steps
        Template template = templateConfiguration.getTemplate( 'mappings.ftl' )

        Writer writer = new StringWriter()
        template.process( model, writer )
        reportsPath.resolve( "${MAPPING.getAbbreviation()}.html" ).toFile() << writer.toString()

    }


    @Override
    protected void clean() throws Throwable {

        log.trace( 'clean' )

    }

}