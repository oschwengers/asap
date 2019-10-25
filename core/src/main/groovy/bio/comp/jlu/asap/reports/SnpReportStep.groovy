package bio.comp.jlu.asap.reports


import java.nio.file.*
import java.util.zip.*
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import freemarker.template.Configuration
import freemarker.template.Template
import bio.comp.jlu.asap.Misc
import bio.comp.jlu.asap.reports.ReportStep

import static bio.comp.jlu.asap.api.GenomeSteps.SNP_DETECTION
import static bio.comp.jlu.asap.api.RunningStates.*
import static bio.comp.jlu.asap.api.Paths.*


@Slf4j
class SnpReportStep extends ReportStep {

    private final int BIN_SIZE = 10000

    private final Path snpDetectionPath
    private final Path snpDetectionReportPath


    public SnpReportStep( def config, Configuration templateConfiguration ) {

        super( SNP_DETECTION.getAbbreviation(), config, templateConfiguration )

        snpDetectionPath = projectPath.resolve( PROJECT_PATH_SNPS )
        snpDetectionReportPath = reportsPath.resolve( PROJECT_PATH_SNPS )

    }


    @Override
    boolean isSelected() {

        return true

    }


    @Override
    protected void setup() throws Throwable {

        log.debug( 'setup' )
        Files.createDirectory( snpDetectionReportPath )

    }


    @Override
    protected void runStep() throws Throwable {

        log.debug( 'run' )

        def steps = [
            finished : [],
            skipped : [],
            failed : []
        ]

        def snpCounts = [:]
        int noGenomes = 0


        // collect SNP counts in order to compute average SNP disitribution
        config.genomes.each( { genome ->

            String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
            if( Files.exists( snpDetectionPath.resolve( "${genomeName}.finished" ) ) ) {
                Path infoJsonPath = snpDetectionPath.resolve( "${genomeName}.json" )
                def stat = (new JsonSlurper()).parseText( infoJsonPath.toFile().text )
                // read SNP positions if noSNPs > 0
                if( stat.noSNPs > 0 ) {
                    noGenomes++
                    def detailSnpCounts = [:]
                    Path vcfPath = snpDetectionPath.resolve( "${genomeName}.vcf.gz" )
                    GZIPInputStream gzip = new GZIPInputStream( new FileInputStream( vcfPath.toString() ) )
                    BufferedReader br = new BufferedReader( new InputStreamReader( gzip ) )
                    String line
                    while( (line = br.readLine()) != null ) {
                        if( !line  ||  line.charAt(0)=='#')
                            continue
                        int snpPos = line.trim().split( '\t' )[1] as int
                        if( !snpCounts[ (snpPos) ] ) snpCounts << [ (snpPos): 1 ]
                        else snpCounts[ (snpPos) ] += 1
                    }
                }
            }
        } )

        // bin counted SNP positions
        def avrgBinnedCounts = [:]
        if( snpCounts ) {
            snpCounts.sort( { k1,k2 -> (k1 as int) <=> (k2 as int)} as Comparator )
            int max = snpCounts.keySet().findAll( { it != null } ).collect( {it as int} ).max()
            int bins = max / BIN_SIZE
            if( max % BIN_SIZE )
                bins++
            for( int b in 0..(bins-1) ) {
                int binCount = 0
                for( int i=b*BIN_SIZE; i<(b+1)*BIN_SIZE; i++ ) {
                    def c = snpCounts[ (i) ]
                    if( c ) binCount += c
                }
                avrgBinnedCounts << [ (b): (binCount / noGenomes) as int ]
            }
            model.avrgBinnedCounts = avrgBinnedCounts.collect( { k,v -> "[${k*10000},${v}]" } ).join(',')
        } else
            model.avrgBinnedCounts = ''


        // read info.json on finished jobs
        Template detailTemplate = templateConfiguration.getTemplate( "snp_details.ftl" )
        config.genomes.each( { genome ->

            def stat = [
                genome: [
                    id: genome.id,
                    species: genome.species,
                    strain: genome.strain
                ],
                status: genome.steps[ SNP_DETECTION.getAbbreviation() ]?.status
            ]

            String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
            if( Files.exists( snpDetectionPath.resolve( "${genomeName}.finished" ) ) ) {
                Path infoJsonPath = snpDetectionPath.resolve( "${genomeName}.json" )
                stat << (new JsonSlurper()).parseText( infoJsonPath.toFile().text )
                stat.genomeName = genomeName

                // create hard links to download files
                [
                    "${genomeName}.vcf.gz"
                ].each( {
                    Files.createLink( snpDetectionReportPath.resolve( it ), snpDetectionPath.resolve( it ) )
                } )

                // read SNP positions if noSNPs > 0
                if( stat.noSNPs > 0 ) {

                    def detailSnpCounts = [:]
                    Path vcfPath = snpDetectionPath.resolve( "${genomeName}.vcf.gz" )
                    GZIPInputStream gzip = new GZIPInputStream( new FileInputStream( vcfPath.toString() ) )
                    BufferedReader br = new BufferedReader( new InputStreamReader( gzip ) )
                    String line
                    while( (line = br.readLine()) != null ) {
                        if( !line  ||  line.charAt(0)=='#')
                            continue
                        int snpPos = line.trim().split( '\t' )[1] as int
                        if( !detailSnpCounts[ (snpPos) ] ) detailSnpCounts << [ (snpPos): 1 ]
                        else detailSnpCounts[ (snpPos) ] += 1
                    }

                    detailSnpCounts.sort( { k1,k2 -> (k1 as int) <=> (k2 as int)} as Comparator )
                    int max = detailSnpCounts.keySet().collect( {it as int} ).max()
                    int bins = max / BIN_SIZE
                    if( max % BIN_SIZE ) bins++

                    def detailBinnedCounts = [:]
                    for( int b in 0..(bins-1) ) {
                        int binCount = 0
                        for( int i=b*BIN_SIZE; i<(b+1)*BIN_SIZE; i++ ) {
                            def c = detailSnpCounts[ (i) ]
                            if( c ) binCount += c
                        }
                        detailBinnedCounts << [ (b): binCount ]
                    }
                    stat.detailBinnedCounts = detailBinnedCounts.collect( { k,v -> "[${k*10000},${avrgBinnedCounts[k]},${v}]" } ).join(',')

                    // conversions
                    model << stat
                    Writer detailWriter = new StringWriter()
                    detailTemplate.process( model, detailWriter )
                    snpDetectionReportPath.resolve( "${genomeName}.html" ).toFile() << detailWriter.toString()

                    // remove unnecessary memory payload
                    stat.noHighImpactSNPs = stat.highImpactSNPs.size()
                    stat.remove( 'highImpactSNPs' )
                    stat.remove( 'detailBinnedCounts' )
                    stat.remove( 'impacts' )
                    stat.remove( 'classes' )
                    stat.remove( 'effects' )
                    stat.remove( 'region' )
                    stat.remove( 'baseChanges' )
                    stat.remove( 'snpCoverage' )

                } else
                    stat.noHighImpactSNPs = 0

                steps.finished << stat

            } else if( stat.status == SKIPPED.toString()  ||  Files.exists( snpDetectionPath.resolve( "${genomeName}.skipped" ) ) ) {
                steps.skipped << stat
            } else {
                steps.failed << stat
            }

        } )
        model.steps = steps

        Writer writer = new StringWriter()
        templateConfiguration.getTemplate( 'snp.ftl' ).process( model, writer )
        reportsPath.resolve( "${SNP_DETECTION.getAbbreviation()}.html" ).toFile() << writer.toString()

    }


    @Override
    protected void clean() throws Throwable {

//        log.debug( 'clean' )

    }

}
