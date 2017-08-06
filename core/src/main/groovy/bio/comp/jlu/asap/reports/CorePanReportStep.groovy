
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

import static bio.comp.jlu.asap.api.AnalysesSteps.CORE_PAN
import static bio.comp.jlu.asap.api.Paths.PROJECT_PATH_CORE_PAN
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
@Slf4j
class CorePanReportStep extends ReportStep {

    private final Path corePanPath
    private final Path corePanReportsPath


    public CorePanReportStep( def config, Configuration templateConfiguration ) {

        super( CORE_PAN.getAbbreviation(), config, templateConfiguration )

        corePanPath = projectPath.resolve( PROJECT_PATH_CORE_PAN )
        corePanReportsPath = reportsPath.resolve( PROJECT_PATH_CORE_PAN )

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
        Files.createDirectory( corePanReportsPath )

        // hard link download files
        Files.createLink( corePanReportsPath.resolve( 'core.fasta' ), corePanPath.resolve( 'core.fasta' ) )
        Files.createLink( corePanReportsPath.resolve( 'pan.fasta' ), corePanPath.resolve( 'pan.fasta' ) )
        Files.createLink( corePanReportsPath.resolve( 'pan-matrix.tsv' ), corePanPath.resolve( 'pan-matrix.tsv' ) )

    }


    @Override
    protected void runStep() throws Throwable {

        log.trace( 'run' )

        model.status = config.analyses[ CORE_PAN.getAbbreviation() ]?.status

        Template template
        def corePanStep = config.analyses[ CORE_PAN.getAbbreviation() ]
        if( corePanStep?.status == FINISHED.toString() ) {

            // parse info json
            Path infoJsonPath = corePanPath.resolve( 'info.json' )
            def info = (new JsonSlurper()).parseText( infoJsonPath.toFile().text )

            // build detail pages
            model.steps = [
                finished: [],
                skipped: []
            ]

            Template detailTemplate = templateConfiguration.getTemplate( 'corepan_details.ftl' )
            info.corepan.includedGenomes.each( { genomeName ->

                Path genomeInfoJsonPath = corePanPath.resolve( "${genomeName}.json" )
                def genomeInfo = (new JsonSlurper()).parseText( genomeInfoJsonPath.toFile().text )
                model.genome = genomeInfo.genome
                model.chartData = [
                    donut: [
                        core:       genomeInfo.corepan.core.size(),
                        accessory:  genomeInfo.corepan.accessory.size(),
                        singletons: genomeInfo.corepan.singletons.size()
                    ]
                ]

                model.steps.finished << [
                    genome: genomeInfo.genome,
                    genomeName: genomeName,
                    accessory:  genomeInfo.corepan.accessory.size(),
                    singletons: genomeInfo.corepan.singletons.size()
                ]

                model.core       = genomeInfo.corepan.core
                model.accessory  = genomeInfo.corepan.accessory
                model.singletons = genomeInfo.corepan.singletons

                Writer detailWriter = new StringWriter()
                detailTemplate.process( model, detailWriter )
                corePanReportsPath.resolve( "${genomeName}.html" ).toFile() << detailWriter.toString()

            } )

            info.corepan.excludedGenomes.each( { genomeName ->
                def genome = config.genomes.find( { "${config.project.genus}_${it.species}_${it.strain}" == genomeName } )
                assert genome != null
                model.steps.skipped << [
                    genome: [
                        id: genome.id,
                        species: genome.species,
                        strain: genome.strain
                    ]
                ]
            } )

            assert info.corepan.plots.pan.size() == info.corepan.plots.core.size()
            assert info.corepan.plots.pan.size() == info.corepan.plots.singletons.size()
            def devPlot = []
            for( int i=1; i<=info.corepan.plots.pan.size(); i++ ) {
                devPlot << "[${i},${info.corepan.plots.pan[i]},${info.corepan.plots.core[i]},${info.corepan.plots.singletons[i]}]"
            }

            model.chartData = [
                donut: [
                    core: info.corepan.core.size(),
                    accessory: info.corepan.accessory.size(),
                    singletons: info.corepan.singletons.size()
                ],
                line: devPlot
            ]

            model.noPan  = info.corepan.noPan
            model.noCore = info.corepan.noCore
            model.noAccessory  = info.corepan.noAccessory
            model.noSingletons = info.corepan.noSingletons

            model.core       = info.corepan.core
            model.accessory  = info.corepan.accessory
            model.singletons = info.corepan.singletons

            // build assemblies.html
            template = templateConfiguration.getTemplate( 'corepan.ftl' )

        } else if( corePanStep?.status == SKIPPED.toString() ) {
            model.report = 'Core/Pan Genome'
            model.step   = CORE_PAN.getName()
            template = templateConfiguration.getTemplate( 'skipped.ftl' )
        } else {
            model.error  = corePanStep.error
            model.report = 'Core/Pan Genome'
            model.step   = CORE_PAN.getName()
            template = templateConfiguration.getTemplate( 'error.ftl' )
        }

        Writer writer = new StringWriter()
        template.process( model, writer )
        reportsPath.resolve( "${CORE_PAN.getAbbreviation()}.html" ).toFile() << writer.toString()

    }


    @Override
    protected void clean() throws Throwable {

        log.trace( 'clean' )
    }

}

