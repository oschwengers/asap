
package bio.comp.jlu.asap.reports


import java.nio.file.Files
import java.nio.file.Path
import groovy.util.logging.Slf4j
import groovy.json.JsonSlurper
import freemarker.template.Configuration
import freemarker.template.Template
import bio.comp.jlu.asap.api.Paths
import bio.comp.jlu.asap.reports.ReportStep

import static bio.comp.jlu.asap.api.GenomeSteps.MLST
import static bio.comp.jlu.asap.api.RunningStates.*


@Slf4j
class MLSTReportStep extends ReportStep {

    private final Path mlstPath
    private final Path mlstReportPath


    public MLSTReportStep( def config, Configuration templateConfiguration ) {

        super( MLST.getAbbreviation(), config, templateConfiguration )

        mlstPath = projectPath.resolve( Paths.PROJECT_PATH_MLST )
        mlstReportPath = reportsPath.resolve( Paths.PROJECT_PATH_MLST )

    }


    @Override
    boolean isSelected() {

        return true

    }


    @Override
    protected void setup() throws Throwable {

        log.trace( 'setup' )
        Files.createDirectory( mlstReportPath )

    }


    @Override
    protected void runStep() throws Throwable {

        log.trace( 'run' )

        def steps = [
            finished : [],
            skipped : [],
            failed : []
        ]

        def profileSTs = [:] // sequence types
        def profileCCs = [:] // clonal clusters
        def profileLineages = [:] // lineages

        // read info.json on finished jobs
        config.genomes.each( { genome ->

            def stat = [
                genome: [
                    id: genome.id,
                    species: genome.species,
                    strain: genome.strain
                ],
                status: genome.steps[ MLST.getAbbreviation() ]?.status
            ]

            String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
            if( Files.exists( mlstPath.resolve( "${genomeName}.finished" ) ) ) {

                Path infoJsonPath = mlstPath.resolve( "${genomeName}.json" )
                stat << (new JsonSlurper()).parseText( infoJsonPath.text )
                stat.genomeName = genomeName

                // conversions

                // aggregations
                for( p in stat.mlst.perfect ) {
                    if( profileSTs[ p.st ] ) profileSTs[ p.st ] += 1
                    else profileSTs[ p.st ] = 1
                    if( profileCCs[ p.cc ] ) profileCCs[ p.cc ] += 1
                    else profileCCs[ p.cc ] = 1
                    if( profileLineages[ p.lineage ] ) profileLineages[ p.lineage ] += 1
                    else profileLineages[ p.lineage ] = 1
                }


                steps.finished << stat

            } else if( stat.status == SKIPPED.toString()  ||  Files.exists( mlstPath.resolve( "${genomeName}.skipped" ) ) ) {
                steps.skipped << stat
            } else {
                steps.failed << stat
            }

        } )


        // sort and aggregate STs, CCs and lineages
        profileSTs = profileSTs.sort( { a, b -> (b.value as int) <=> (a.value as int) } )
        log.debug( "st: ${profileSTs}" )
        if( profileSTs.keySet().size() > 10 ) {
            int i = 0
            def tmp = [misc:0]
            for( st in profileSTs.keySet() ) {
                if( i < 10 ) tmp[ st ] = profileSTs[ st ]
                else tmp.misc += profileSTs[ st ]
                i++
            }
            log.debug( "st after: ${profileSTs}" )
            profileSTs = tmp
        }
        model.profileSTs = profileSTs

        profileCCs = profileCCs.sort( { a, b -> (b.value as int) <=> (a.value as int) } )
        log.debug( "cc: ${profileCCs}" )
        if( profileCCs.keySet().size() > 10 ) {
            int i = 0
            def tmp = [misc:0]
            for( cc in profileCCs.keySet() ) {
                if( i < 10 ) tmp[ cc ] = profileCCs[ cc ]
                else tmp.misc += profileCCs[ cc ]
                i++
            }
            log.debug( "cc after: ${profileCCs}" )
            profileCCs = tmp
        }
        if( profileCCs[ '-' ] ) {
            profileCCs[ 'Not Available' ] = profileCCs[ '-' ]
            profileCCs.remove( '-' )
        }
        model.profileCCs = profileCCs

        profileLineages = profileLineages.sort( { a, b -> (b.value as int) <=> (a.value as int) } )
        log.debug( "st: ${profileLineages}" )
        if( profileLineages.keySet().size() > 10 ) {
            int i = 0
            def tmp = [misc:0]
            for( ll in profileLineages.keySet() ) {
                if( i < 10 ) tmp[ ll ] = profileLineages[ ll ]
                else tmp.misc += profileLineages[ ll ]
                i++
            }
            log.debug( "ll after: ${profileLineages}" )
            profileLineages = tmp
        }
        if( profileLineages[ '-' ] ) {
            profileLineages[ 'Not Available' ] = profileLineages[ '-' ]
            profileLineages.remove( '-' )
        }
        model.profileLineages = profileLineages


        // build annotations.html
        model.steps = steps
        Template template = templateConfiguration.getTemplate( 'mlst.ftl' )

        Writer writer = new StringWriter()
        template.process( model, writer )
        reportsPath.resolve( "${MLST.getAbbreviation()}.html" ).toFile() << writer.toString()

    }


    @Override
    protected void clean() throws Throwable {

        log.trace( 'clean' )

    }

}