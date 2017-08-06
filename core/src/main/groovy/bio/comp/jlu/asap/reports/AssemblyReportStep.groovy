
package bio.comp.jlu.asap.reports


import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.regex.Matcher
import groovy.util.logging.Slf4j
import groovy.io.FileType
import groovy.json.JsonSlurper
import freemarker.template.Configuration
import freemarker.template.Template

import static bio.comp.jlu.asap.api.GenomeSteps.ASSEMBLY
import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
@Slf4j
class AssemblyReportStep extends ReportStep {

    private final Path assemblyPath
    private final Path assemblyReportsPath


    public AssemblyReportStep( def config, Configuration templateConfiguration ) {

        super( ASSEMBLY.getAbbreviation(), config, templateConfiguration )

        assemblyPath = projectPath.resolve( PROJECT_PATH_ASSEMBLIES )
        assemblyReportsPath = reportsPath.resolve( PROJECT_PATH_ASSEMBLIES )

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
        Files.createDirectory( assemblyReportsPath )

    }


    @Override
    protected void runStep() throws Throwable {

        log.trace( 'run' )

        model.steps = [
            finished: [],
            skipped: [],
            failed: []
        ]

        model.chartData = [
            overview: [
                noContigs: [],
                meanCLengths: [],
                medianCLengths: [],
                genomeSizes: [],
                gcs: [],
                n50s: [],
                n90s: [],
                n50Covs: [],
                n90Covs: []
            ]
        ]

        // build detail pages
        Template detailTemplate = templateConfiguration.getTemplate( 'assemblies_details.ftl' )
        config.genomes.each( { genome ->

            def stat = [
                genome: [
                    id: genome.id,
                    species: genome.species,
                    strain: genome.strain
                ],
                status: genome.steps[ ASSEMBLY.getAbbreviation() ]?.status
            ]

            if( stat.status == FINISHED.toString() ) {

                String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"

                // hard link download files
                Path genomePath = assemblyPath.resolve( genomeName )
                Path genomeReportsPath = assemblyReportsPath.resolve( genomeName )
                Files.createDirectory( genomeReportsPath )
                Files.createLink( genomeReportsPath.resolve( "${genomeName}.fasta" ), genomePath.resolve( "${genomeName}.fasta" ) )
                Files.createLink( genomeReportsPath.resolve( "${genomeName}-discarded.fasta" ), genomePath.resolve( "${genomeName}-discarded.fasta" ) )

                // parse info json
                Path infoJsonPath = genomePath.resolve( 'info.json' )
                stat << (new JsonSlurper()).parseText( infoJsonPath.toFile().text )
                stat.genomeName = genomeName

                // conversions
                stat.gc *= 100
                stat.contigs.each( { it.gc *= 100 } )

                // create data for contig distribution charts
                def chartData = model.chartData
                chartData.contigLength = stat.contigs.collect( { [ name: it.name, length: it.length ] } )
                chartData.contigCoverage = stat.contigs.collect( { [ name: it.name, coverage: it.coverage ] } )
                chartData.contigGC = stat.contigs.collect( { [ name: it.name, gc: it.gc ] } )

                // collect data for assembly overview XY chart
                def overview = model.chartData.overview
                overview.noContigs << stat.noContigs
                overview.meanCLengths << stat.statsLength.mean
                overview.medianCLengths << stat.statsLength.median
                overview.genomeSizes << stat.length
                overview.gcs << stat.gc
                overview.n50s << stat.n50
                overview.n90s << stat.n90
                overview.n50Covs << stat.n50Coverage
                overview.n50Covs << stat.n90Coverage

                model << stat
                Writer detailWriter = new StringWriter()
                detailTemplate.process( model, detailWriter )
                assemblyReportsPath.resolve( "${genomeName}.html" ).toFile() << detailWriter.toString()
                model.steps.finished << stat

            } else if( stat.status == SKIPPED.toString() )
                model.steps.skipped << stat
            else
                model.steps.failed << stat

        } )

        // build assemblies.html
        Template overviewTemplate = templateConfiguration.getTemplate( 'assemblies.ftl' )
        Writer overviewWriter = new StringWriter()
        overviewTemplate.process( model, overviewWriter )
        reportsPath.resolve( 'assemblies.html' ).toFile() << overviewWriter.toString()

    }


    @Override
    protected void clean() throws Throwable {

        log.trace( 'clean' )
    }

}

