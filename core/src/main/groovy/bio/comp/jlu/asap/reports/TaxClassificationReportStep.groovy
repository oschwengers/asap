
package bio.comp.jlu.asap.reports


import java.nio.file.Files
import java.nio.file.Path
import groovy.util.logging.Slf4j
import groovy.json.JsonSlurper
import freemarker.template.Configuration
import freemarker.template.Template
import bio.comp.jlu.asap.api.Paths
import bio.comp.jlu.asap.reports.ReportStep

import static bio.comp.jlu.asap.api.GenomeSteps.TAXONOMY
import static bio.comp.jlu.asap.api.RunningStates.*


@Slf4j
class TaxClassificationReportStep extends ReportStep {

    private final Path taxPath
    private final Path taxReportPath


    public TaxClassificationReportStep( def config, Configuration templateConfiguration ) {

        super( TAXONOMY.getAbbreviation(), config, templateConfiguration )

        taxPath = projectPath.resolve( Paths.PROJECT_PATH_TAXONOMY )
        taxReportPath = reportsPath.resolve( Paths.PROJECT_PATH_TAXONOMY )

    }


    @Override
    boolean isSelected() {

        return true

    }


    @Override
    protected void setup() throws Throwable {

        log.debug( 'setup' )
        Files.createDirectory( taxReportPath )

    }


    @Override
    protected void runStep() throws Throwable {

        log.debug( 'run' )

        def steps = [
            finished : [],
            skipped : [],
            failed : []
        ]

        def taxonCounts = [:]

        // read info.json on finished jobs
        Template detailTemplate = templateConfiguration.getTemplate( "taxonomy_details.ftl" )
        config.genomes.each( { genome ->

            def stat = [
                genome: [
                    id: genome.id,
                    species: genome.species,
                    strain: genome.strain
                ],
                status: genome.steps[ TAXONOMY.getAbbreviation() ]?.status
            ]

            String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
            if( Files.exists( taxPath.resolve( "${genomeName}.finished" ) ) ) {

                Path infoJsonPath = taxPath.resolve( "${genomeName}.json" )
                stat << (new JsonSlurper()).parseText( infoJsonPath.text )
                stat.genomeName = genomeName

                // sankey diagram computations
                stat.plots = [:]
                def linkCounts = [:] // rRna sankey
                stat.rrna.lineages.each( {
                    def lin = it.lineage
                    for( int i=0; i<lin.size()-1; i++ ) {
                        if( lin[i] != lin[i+1] ) {
                            String link = lin[i] + '-' + lin[i+1]
                            if( linkCounts.containsKey( link ) )
                                linkCounts[ (link) ] += it.freq
                            else
                                linkCounts[ (link) ] = it.freq
                        }
                    }
                } )
                stat.plots.sankeyRrna = linkCounts.collect( { k,v ->
                    def split = k.split( '-' )
                    return [ from: split[0], to: split[1], weight: v ]
                } )
                [ stat.rrna.lineages ].flatten().each( {
                    if( taxonCounts.containsKey( it.classification ) )
                        taxonCounts[ (it.classification) ] += it.freq
                    else
                        taxonCounts[ (it.classification) ] = it.freq
                } )

                model << stat
                Writer detailWriter = new StringWriter()
                detailTemplate.process( model, detailWriter )
                taxReportPath.resolve( "${genomeName}.html" ).toFile() << detailWriter.toString()

                steps.finished << stat

            } else if( stat.status == SKIPPED.toString()  ||  Files.exists( taxPath.resolve( "${genomeName}.skipped" ) ) ) {
                steps.skipped << stat
            } else {
                steps.failed << stat
            }

        } )

        // build annotations.html
        model.steps = steps
        Template template = templateConfiguration.getTemplate( 'taxonomy.ftl' )

        Writer writer = new StringWriter()
        template.process( model, writer )
        reportsPath.resolve( "${TAXONOMY.getAbbreviation()}.html" ).toFile() << writer.toString()

    }


    @Override
    protected void clean() throws Throwable {

//        log.debug( 'clean' )

    }

}