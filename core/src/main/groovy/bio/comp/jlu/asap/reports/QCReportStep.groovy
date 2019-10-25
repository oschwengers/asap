
package bio.comp.jlu.asap.reports


import java.nio.file.*
import java.util.regex.Matcher
import groovy.io.FileType
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import freemarker.template.*
import bio.comp.jlu.asap.Misc

import static bio.comp.jlu.asap.api.GenomeSteps.QC
import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class QCReportStep extends ReportStep {

    private final Path qcPath
    private final Path qcReportsPath


    public QCReportStep( def config, Configuration templateConfiguration ) {

        super( QC.getAbbreviation(), config, templateConfiguration )

        qcPath = projectPath.resolve( PROJECT_PATH_READS_QC )
        qcReportsPath = reportsPath.resolve( PROJECT_PATH_READS_QC )

    }


    @Override
    boolean isSelected() {

        // genome specific selection so always return true here
        return true

    }


    @Override
    protected void setup() throws Throwable {

        log.debug( 'setup' )

        // Perform any init logic here
        Files.createDirectory( qcReportsPath )

    }


    @Override
    protected void runStep() throws Throwable {

        log.debug( 'run' )

        def steps = [
            finished : [],
            skipped : [],
            failed : []
        ]

        // build detail pages
        Template detailTemplate = templateConfiguration.getTemplate( 'qc_details.ftl' )
        Path readsFilteredProjectPath = projectPath.resolve( PROJECT_PATH_READS_QC )
        config.genomes.each( { genome ->
            def stat = [
                genome: [
                    id: genome.id,
                    species: genome.species,
                    strain: genome.strain
                ],
                status: genome.steps[ QC.getAbbreviation() ]?.status
            ]
            if( stat.status == FINISHED.toString() ) {

                String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"

                // parse info json
                Path genomePath = qcPath.resolve( genomeName )
                stat << (new JsonSlurper()).parseText( genomePath.resolve( 'info.json' ).toFile().text )
                stat.genomeName = genomeName


                // hard link download files
                Path genomeReportsPath = qcReportsPath.resolve( genomeName )
                Files.createDirectory( genomeReportsPath )

                // link QCed graphic files
                Path qcReadsPath = genomeReportsPath.resolve( 'qc' )
                Files.createDirectory( qcReadsPath )
                stat.qcReads.each {
                    Path fileQcReadsPath = qcReadsPath.resolve( it.fileName )
                    Files.createDirectory( fileQcReadsPath )
                    Paths.get( projectPath.toString(), PROJECT_PATH_READS_QC, genomeName, it.fileName ).eachFile( {
                        Files.createLink( fileQcReadsPath.resolve( it.fileName ), it )
                    } )
                }

                // link raw graphic files
                Path rawReadsPath = genomeReportsPath.resolve( 'raw' )
                Files.createDirectory( rawReadsPath )
                stat.rawReads.each {
                    Path fileRawReadsPath = rawReadsPath.resolve( it.fileName )
                    Files.createDirectory( fileRawReadsPath )
                    Paths.get( projectPath.toString(), PROJECT_PATH_READS_RAW, genomeName, it.fileName ).eachFile( {
                        Files.createLink( fileRawReadsPath.resolve( it.fileName ), it )
                    } )
                }

                stat.contaminations.potentialContaminations *= 100

                def maxCol = 12
                stat.bcol = maxCol
                if( stat.qcReads.size() > 0 ) {
                    stat.bcol = maxCol.intdiv( stat.qcReads.size() )
                    if( stat.bcol < 2 ) stat.bcol = 2
                }

                // write HTML output
                model << stat
                Writer detailWriter = new StringWriter()
                detailTemplate.process( model, detailWriter )
                qcReportsPath.resolve( "${stat.genomeName}.html" ).toFile() << detailWriter.toString()
                steps.finished << stat

            } else if( stat.status == SKIPPED.toString() )
                steps.skipped << stat
            else
                steps.failed << stat
        } )
        model.steps = steps

        Template template = templateConfiguration.getTemplate( 'qc.ftl' )
        Writer overviewWriter = new StringWriter()
        template.process( model, overviewWriter )
        reportsPath.resolve( 'qc.html' ).toFile() << overviewWriter.toString()

    }


    @Override
    protected void clean() throws Throwable {

//        log.debug( 'clean' )

    }

}

