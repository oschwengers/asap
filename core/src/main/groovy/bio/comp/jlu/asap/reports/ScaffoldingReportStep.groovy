
package bio.comp.jlu.asap.reports


import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import groovy.util.logging.Slf4j
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import freemarker.template.Configuration
import freemarker.template.Template

import static bio.comp.jlu.asap.api.GenomeSteps.SCAFFOLDING
import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class ScaffoldingReportStep extends ReportStep {


    private Path scaffoldingPath
    private Path scaffoldingReportsPath


    public ScaffoldingReportStep( def config, Configuration templateConfiguration ) {

        super( SCAFFOLDING.getAbbreviation(), config, templateConfiguration )

        scaffoldingPath = projectPath.resolve( PROJECT_PATH_SCAFFOLDS )
        scaffoldingReportsPath = reportsPath.resolve( PROJECT_PATH_SCAFFOLDS )

    }


    @Override
    boolean isSelected() {

        // genome specific selection so always return true here
        return true

    }


    @Override
    protected void setup() throws Throwable {

        log.trace( 'setup' )

        // Perform any init logic here
        Files.createDirectory( scaffoldingReportsPath )

    }


    @Override
    protected void runStep() throws Throwable {

        log.trace( 'run' )

        def steps = [
            finished: [],
            skipped: [],
            failed: []
        ]

        // build detail pages
        Template detailTemplate = templateConfiguration.getTemplate( "scaffolds_details.ftl" )
        config.genomes.each( { genome ->

            def stat = [
                genome: [
                    id: genome.id,
                    species: genome.species,
                    strain: genome.strain
                ],
                status: genome.steps[ SCAFFOLDING.getAbbreviation() ]?.status
            ]

            if( stat.status == FINISHED.toString() ) {

                String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
                Path infoJsonPath = Paths.get( scaffoldingPath.toString(), genomeName, 'info.json' )
                stat << (new JsonSlurper()).parseText( infoJsonPath.toFile().text )
                stat.genomeName = genomeName

                // serialize synteny data to JSON
                stat.scaffolds.syntenies.each( { ref ->
                    ref.preJson  = JsonOutput.toJson( ref.pre )
                    ref.postJson = JsonOutput.toJson( ref.post )
                } )

                Path genomePath = scaffoldingPath.resolve( genomeName )
                Path genomeReportsPath = scaffoldingReportsPath.resolve( genomeName )
                Files.createDirectory( genomeReportsPath )
                Files.createLink( genomeReportsPath.resolve( "${genomeName}.fasta" ), genomePath.resolve( "${genomeName}.fasta" ) )
                Files.createLink( genomeReportsPath.resolve( "${genomeName}-pseudo.fasta" ), genomePath.resolve( "${genomeName}-pseudo.fasta" ) )

                // write HTML output
                model << stat
                Writer detailWriter = new StringWriter()
                detailTemplate.process( model, detailWriter )
                scaffoldingReportsPath.resolve( "${genomeName}.html" ).toFile() << detailWriter.toString()
                steps.finished << stat

            } else if( stat.status == SKIPPED.toString() )
                steps.skipped << stat
            else
                steps.failed << stat

        } )

        // build contigs.html
        model.steps = steps

        Template overviewTemplate = templateConfiguration.getTemplate( "scaffolds.ftl" )
        Writer overviewWriter = new StringWriter()
        overviewTemplate.process( model, overviewWriter )
        reportsPath.resolve( 'scaffolds.html' ).toFile() << overviewWriter.toString()

    }


    @Override
    protected void clean() throws Throwable {

        log.trace( 'clean' )

    }

}

