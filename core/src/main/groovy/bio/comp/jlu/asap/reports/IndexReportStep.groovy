
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

        log.trace( 'setup' )

        // Perform any init logic here

    }


    @Override
    protected void runStep() throws Throwable {

        log.trace( 'run' )

        // build index.html
        Date startTime = Date.parse( DATE_FORMAT, config.dates.start )
        Date endTime   = Date.parse( DATE_FORMAT, config.dates.end )
        model.runtime = [
            config: Date.parse( DATE_FORMAT, config.dates.config ).format( DATE_FORMAT_HUMAN_READABLE ),
            start: startTime.format( DATE_FORMAT_HUMAN_READABLE ),
            end:   endTime.format( DATE_FORMAT_HUMAN_READABLE ),
            time:  Misc.formatRuntimes( (int)(endTime.getTime() - startTime.getTime()) )
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
                def taxStats = (new JsonSlurper()).parseText( infoJsonPath.text )
                stat.kmer = taxStats.kmer.lineages.size() > 0 ? taxStats.kmer.classification.classification : '-'
                stat.kmerScore = taxStats.kmer.lineages.size() > 0 ? taxStats.kmer.classification.freq / taxStats.kmer.hits : -1
            } else {
                stat.kmer = '-'
                stat.kmerScore = -1
            }


            // parse assembly results
            infoJsonPath = Paths.get( projectPath.toString(), PROJECT_PATH_ASSEMBLIES, genomeName, 'info.json' )
            if( Files.exists( infoJsonPath ) ) {
                def assemblyStats = (new JsonSlurper()).parseText( infoJsonPath.text )
                stat.genomeSize = (int)(assemblyStats.length / 1000)
                stat.noContigs = assemblyStats.noContigs
                stat.gc = (assemblyStats.gc as double) * 100
            } else {
                stat.genomeSize = -1
                stat.noContigs = -1
                stat.gc = -1
            }

            // parse annotation results
            infoJsonPath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName, 'info.json' )
            if( Files.exists( infoJsonPath ) ) {
                def annotationStats = (new JsonSlurper()).parseText( infoJsonPath.text )
                stat.noGenes = annotationStats.noGenes
            } else {
                stat.noGenes = -1
            }

            // parse ABR results
            infoJsonPath = Paths.get( projectPath.toString(), PROJECT_PATH_ABR, "${genomeName}.json" )
            if( Files.exists( infoJsonPath ) ) {
                def abrStats = (new JsonSlurper()).parseText( infoJsonPath.text )
                stat.noABRs = abrStats.abr.perfect.size()
            } else {
                stat.noABRs = -1
            }

            // parse virulence factor results
            infoJsonPath = Paths.get( projectPath.toString(), PROJECT_PATH_VF, "${genomeName}.json" )
            if( Files.exists( infoJsonPath ) ) {
                def vfStats = (new JsonSlurper()).parseText( infoJsonPath.text )
                stat.noVFs = vfStats.vf.size()
            } else {
                stat.noVFs = -1
            }

            // parse SNP results
            infoJsonPath = Paths.get( projectPath.toString(), PROJECT_PATH_SNPS, "${genomeName}.json" )
            if( Files.exists( infoJsonPath ) ) {
                def snpStats = (new JsonSlurper()).parseText( infoJsonPath.text )
                stat.noHISNPs = snpStats.highImpactSNPs.size()
            } else {
                stat.noHISNPs = -1
            }

            model.genomes << stat

        } )

        // outlier classification
        def values = model.genomes*.genomeSize.findAll( { it != -1 } )
        if( values.size() > 1 ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.genomeSize = ((it.genomeSize == -1) || (sdev == 0)) ? 0 : Math.abs( it.genomeSize - mean ) / sdev } )
        } else {
            model.genomes.each( { it.zScores.genomeSize = 0 } )
        }

        values = model.genomes*.noContigs.findAll( { it != -1 } )
        if( values.size() > 1 ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.noContigs = ((it.noContigs == -1) || (sdev == 0)) ? 0 : Math.abs( it.noContigs - mean ) / sdev } )
        } else {
            model.genomes.each( { it.zScores.noContigs = 0 } )
        }

        values = model.genomes*.gc.findAll( { it != -1 } )
        if( values.size() > 1 ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.gc = ((it.gc == -1) || (sdev == 0)) ? 0 : Math.abs( it.gc - mean ) / sdev } )
        } else {
            model.genomes.each( { it.zScores.gc = 0 } )
        }

        values = model.genomes*.noGenes.findAll( { it != -1 } )
        if( values.size() > 1 ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.noGenes = ((it.noGenes == -1) || (sdev == 0)) ? 0 : Math.abs( it.noGenes - mean ) / sdev } )
        } else {
            model.genomes.each( { it.zScores.noGenes = 0 } )
        }

        values = model.genomes*.noABRs.findAll( { it != -1 } )
        if( values.size() > 1 ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.noABRs = ((it.noABRs == -1) || (sdev == 0)) ? 0 : Math.abs( it.noABRs - mean ) / sdev } )
        } else {
            model.genomes.each( { it.zScores.noABRs = 0 } )
        }

        values = model.genomes*.noVFs.findAll( { it != -1 } )
        if( values.size() > 1 ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.noVFs = ((it.noVFs == -1) || (sdev == 0)) ? 0 : Math.abs( it.noVFs - mean ) / sdev } )
        } else {
            model.genomes.each( { it.zScores.noVFs = 0 } )
        }

        values = model.genomes*.noHISNPs.findAll( { it != -1 } )
        if( values.size() > 1 ) {
            def mean = values.sum() / values.size()
            def sdev = calcSDev( values )
            model.genomes.each( { it.zScores.noHISNPs = ((it.noHISNPs == -1) || (sdev == 0)) ? 0 : Math.abs( it.noHISNPs - mean ) / sdev } )
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

        log.trace( 'clean' )

    }


    private double calcSDev( def values ) {

        double mean = values.sum() / values.size()
        double sdev = Math.sqrt( values.collect( { (it - mean)**2 } ).sum() / (values.size()-1) )

    }

}

