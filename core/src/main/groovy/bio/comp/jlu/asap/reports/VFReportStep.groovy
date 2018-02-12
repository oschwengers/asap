
package bio.comp.jlu.asap.reports


import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import groovy.util.logging.Slf4j
import groovy.json.JsonSlurper
import freemarker.template.Configuration
import freemarker.template.Template
import bio.comp.jlu.asap.api.Paths
import bio.comp.jlu.asap.reports.ReportStep

import static bio.comp.jlu.asap.api.GenomeSteps.VF
import static bio.comp.jlu.asap.api.RunningStates.*


@Slf4j
class VFReportStep extends ReportStep {

    private final Path vfPath
    private final Path vfReportPath


    public VFReportStep( def config, Configuration templateConfiguration ) {

        super( VF.getAbbreviation(), config, templateConfiguration )

        vfPath = projectPath.resolve( Paths.PROJECT_PATH_VF )
        vfReportPath = reportsPath.resolve( Paths.PROJECT_PATH_VF )

    }


    @Override
    boolean isSelected() {

        return true

    }


    @Override
    protected void setup() throws Throwable {

        log.trace( 'setup' )
        Files.createDirectory( vfReportPath )

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
        Template detailTemplate = templateConfiguration.getTemplate( "vf_details.ftl" )
        config.genomes.each( { genome ->

            def stat = [
                genome: [
                    id: genome.id,
                    species: genome.species,
                    strain: genome.strain
                ],
                status: genome.steps[ VF.getAbbreviation() ]?.status
            ]

            String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
            if( Files.exists( vfPath.resolve( "${genomeName}.finished" ) ) ) {

                Path infoJsonPath = vfPath.resolve( "${genomeName}.json" )
                stat << (new JsonSlurper()).parseText( infoJsonPath.text )
                stat.genomeName = genomeName

                // aggregations
                stat.noDistinctCategories = stat.vf*.catId.unique().size()
                stat.noVFs = stat.vf.size()

                // transformation
                stat.vf.each( {
                    it.coverage *= 100
                    it.pIdent *= 100
                } )

                model << stat
                Writer detailWriter = new StringWriter()
                detailTemplate.process( model, detailWriter )
                vfReportPath.resolve( "${genomeName}.html" ).toFile() << detailWriter.toString()

                stat.remove( 'vf' )

                steps.finished << stat

            } else if( stat.status == SKIPPED.toString()  ||  Files.exists( vfPath.resolve( "${genomeName}.skipped" ) ) ) {
                steps.skipped << stat
            } else {
                steps.failed << stat
            }

        } )


        // build annotations.html
        model.steps = steps
        Template template = templateConfiguration.getTemplate( 'vf.ftl' )

        Writer writer = new StringWriter()
        template.process( model, writer )
        reportsPath.resolve( "${VF.getAbbreviation()}.html" ).toFile() << writer.toString()

    }


    @Override
    protected void clean() throws Throwable {

        log.trace( 'clean' )

    }

}