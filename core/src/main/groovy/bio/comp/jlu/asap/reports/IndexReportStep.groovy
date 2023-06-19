
package bio.comp.jlu.asap.reports


import java.nio.file.*
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import freemarker.template.Configuration
import freemarker.template.Template
import bio.comp.jlu.asap.Misc

import static bio.comp.jlu.asap.api.GenomeSteps.*
import static bio.comp.jlu.asap.api.Paths.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class IndexReportStep extends ReportStep {


    public IndexReportStep( def config, Configuration templateConfiguration ) {

        super( 'index', config, templateConfiguration )

    }


    @Override
    boolean isSelected() {

        // is a global step so always return true here
        return true

    }


    @Override
    protected void setup() throws Throwable {

//        log.debug( 'setup' )

    }


    @Override
    protected void runStep() throws Throwable {

        log.debug( 'run' )

        // build index.html
        model.runtime = [
            config: config.dates.config,
            start: config.dates.start,
            end:   config.dates.end
        ]
        model.genomes = []
        config.genomes.each( { genome ->

            String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
            def stat = [
                id: genome.id,
                sampleName: "${config.project.genus.substring( 0, 1 )}. ${genome.species} ${genome.strain}",
                genomeName: genomeName,
                zScores: [:]
            ]

            // parse characterization results
            Path infoJsonPath = Paths.get( projectPath.toString(), PROJECT_PATH_TAXONOMY, "${genomeName}.json" )
            if( Files.exists( infoJsonPath ) ) {
                def taxStats = (new JsonSlurper()).parseText( infoJsonPath.toFile().text )
                stat.ani = !taxStats.kmer.classification.isEmpty() ? taxStats.kmer.classification.classification : '-'
            } else {
                stat.ani = '-'
            }


            // parse assembly results
            infoJsonPath = Paths.get( projectPath.toString(), PROJECT_PATH_ASSEMBLIES, genomeName, 'info.json' )
            if( Files.exists( infoJsonPath ) ) {
                def assemblyStats = (new JsonSlurper()).parseText( infoJsonPath.toFile().text )
                stat.genomeSize = (int)(assemblyStats.length / 1000)
                stat.noContigs = assemblyStats.noContigs
                stat.gc = (assemblyStats.gc as double) * 100
            } else {
                stat.genomeSize = '-'
                stat.noContigs = '-'
                stat.gc = '-'
            }

            // parse annotation results
            infoJsonPath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName, 'info.json' )
            if( Files.exists( infoJsonPath ) ) {
                def annotationStats = (new JsonSlurper()).parseText( infoJsonPath.toFile().text )
                stat.noGenes = annotationStats.noGenes
            } else {
                stat.noGenes = '-'
            }

            // parse ABR results
            infoJsonPath = Paths.get( projectPath.toString(), PROJECT_PATH_ABR, "${genomeName}.json" )
            if( Files.exists( infoJsonPath ) ) {
                def abrStats = (new JsonSlurper()).parseText( infoJsonPath.toFile().text )
                stat.noABRs = abrStats.abr.perfect.size()
            } else {
                stat.noABRs = '-'
            }

            // parse virulence factor results
            infoJsonPath = Paths.get( projectPath.toString(), PROJECT_PATH_VF, "${genomeName}.json" )
            if( Files.exists( infoJsonPath ) ) {
                def vfStats = (new JsonSlurper()).parseText( infoJsonPath.toFile().text )
                stat.noVFs = vfStats.vf.size()
            } else {
                stat.noVFs = '-'
            }

            // parse SNP results
            infoJsonPath = Paths.get( projectPath.toString(), PROJECT_PATH_SNPS, "${genomeName}.json" )
            if( Files.exists( infoJsonPath ) ) {
                def snpStats = (new JsonSlurper()).parseText( infoJsonPath.toFile().text )
                stat.noHISNPs = snpStats.highImpactSNPs.size()
            } else {
                stat.noHISNPs = '-'
            }

            model.genomes << stat

        } )

        // outlier classification
        def values = model.genomes*.genomeSize.findAll( { it != '-' } )
        if( !values.isEmpty() ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.genomeSize = ((it.genomeSize == '-') || (sdev == 0)) ? 0 : Math.abs( it.genomeSize - mean ) / sdev } )
        } else {
            model.genomes.each( { it.zScores.genomeSize = 0 } )
        }

        values = model.genomes*.noContigs.findAll( { it != '-' } )
        if( !values.isEmpty() ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.noContigs = ((it.noContigs == '-') || (sdev == 0)) ? 0 : Math.abs( it.noContigs - mean ) / sdev } )
        } else {
            model.genomes.each( { it.zScores.noContigs = 0 } )
        }

        values = model.genomes*.gc.findAll( { it != '-' } )
        if( !values.isEmpty() ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.gc = ((it.gc == '-') || (sdev == 0)) ? 0 : Math.abs( it.gc - mean ) / sdev } )
        } else {
            model.genomes.each( { it.zScores.gc = 0 } )
        }

        values = model.genomes*.noGenes.findAll( { it != '-' } )
        if( !values.isEmpty() ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.noGenes = ((it.noGenes == '-') || (sdev == 0)) ? 0 : Math.abs( it.noGenes - mean ) / sdev } )
        } else {
            model.genomes.each( { it.zScores.noGenes = 0 } )
        }

        values = model.genomes*.noABRs.findAll( { it != '-' } )
        if( !values.isEmpty() ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.noABRs = ((it.noABRs == '-') || (sdev == 0)) ? 0 : Math.abs( it.noABRs - mean ) / sdev } )
        } else {
            model.genomes.each( { it.zScores.noABRs = 0 } )
        }

        values = model.genomes*.noVFs.findAll( { it != '-' } )
        if( !values.isEmpty() ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.noVFs = ((it.noVFs == '-') || (sdev == 0)) ? 0 : Math.abs( it.noVFs - mean ) / sdev } )
        } else {
            model.genomes.each( { it.zScores.noVFs = 0 } )
        }

        values = model.genomes*.noHISNPs.findAll( { it != '-' } )
        if( !values.isEmpty() ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.noHISNPs = ((it.noHISNPs == '-') || (sdev == 0)) ? 0 : Math.abs( it.noHISNPs - mean ) / sdev } )
        } else {
            model.genomes.each( { it.zScores.noHISNPs = 0 } )
        }


        Template template = templateConfiguration.getTemplate( 'index.ftl' )
        Writer writer = new StringWriter()
        template.process( model, writer )
        reportsPath.resolve( 'index.html' ).toFile() << writer.toString()

    }


    @Override
    protected void clean() throws Throwable {

//        log.debug( 'clean' )

    }


    private double calcSDev( def values ) {

        double mean = values.sum() / values.size()
        double sdev = Math.sqrt( values.collect( { (it - mean)**2 } ).sum() / (values.size()-1) )

    }

}

