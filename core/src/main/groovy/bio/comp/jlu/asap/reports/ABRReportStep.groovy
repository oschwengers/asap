
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

import static bio.comp.jlu.asap.api.GenomeSteps.ABR
import static bio.comp.jlu.asap.api.RunningStates.*


@Slf4j
class ABRReportStep extends ReportStep {

    private final Path abrPath
    private final Path abrReportPath

    private final def abrAbbr = [
        'aminoglycoside': 'ab',
        'monobactam': 'mb',
        'penam': 'pn',
        'penem': 'pe',
        'carbapenem': 'cp',
        'cephalosporin': 'cs',
        'cephamycin': 'cm',
        'cycloserine': 'ce',
        'diaminopyrimidine': 'da',
        'elfamycin': 'em',
        'fluoroquinolone': 'fl',
        'fosfomycin': 'fm',
        'glycopeptide': 'gl',
        'lincosamide': 'ls',
        'lipopeptide': 'lp',
        'macrocyclic': 'mc',
        'macrolide': 'ml',
        'nitrofuran': 'nf',
        'nitroimidazole': 'ni',
        'nucleoside': 'nc',
        'nybomycin': 'ny',
        'organoarsenic': 'oa',
        'oxazolidinone': 'oz',
        'peptide': 'pt',
        'phenicol': 'pc',
        'pleuromutilin': 'pm',
        'polyamine': 'pa',
        'polymyxin': 'px',
        'rifamycin': 'ra',
        'streptogramin': 'sg',
        'sulfonamide': 'sa',
        'sulfone': 'sf',
        'tetracycline': 'tc',
        'triclosan': 'ts'
    ]


    public ABRReportStep( def config, Configuration templateConfiguration ) {

        super( ABR.getAbbreviation(), config, templateConfiguration )

        abrPath = projectPath.resolve( Paths.PROJECT_PATH_ABR )
        abrReportPath = reportsPath.resolve( Paths.PROJECT_PATH_ABR )

    }


    @Override
    boolean isSelected() {

        return true

    }


    @Override
    protected void setup() throws Throwable {

        log.debug( "setup" )

        Files.createDirectory( abrReportPath )
        Path jsPath = reportsPath.resolve( 'js' )
        String packagePrefix = '/bio/comp/jlu/asap/reports/templates'
        [
            [ jsPath, 'abrs.js' ]
        ].each( { path, name ->
            InputStream is =  getClass().getResourceAsStream( "${packagePrefix}/abr/${name}" )
            Files.copy( is, path.resolve( name ), StandardCopyOption.REPLACE_EXISTING )
        } )

    }


    @Override
    protected void runStep() throws Throwable {

        log.debug( "run" )

        def steps = [
            finished : [],
            skipped : [],
            failed : []
        ]

        // read info.json on finished jobs
        Template detailTemplate = templateConfiguration.getTemplate( "abr_details.ftl" )
        config.genomes.each( { genome ->

            def stat = [
                genome: [
                    id: genome.id,
                    species: genome.species,
                    strain: genome.strain
                ],
                status: genome.steps[ ABR.getAbbreviation() ]?.status
            ]

            String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
            if( Files.exists( abrPath.resolve( "${genomeName}.finished" ) ) ) {

                Path infoJsonPath = abrPath.resolve( "${genomeName}.json" )
                stat << (new JsonSlurper()).parseText( infoJsonPath.text )
                stat.genomeName = genomeName

                // conversions
                stat.noPotentialResistances = (stat.abr.additional*.orf.start).toUnique().size()
                stat.antibiotics = stat.abr.perfect*.antibiotics.flatten().toUnique().sort()
                stat.abrProfile = stat.abr.perfect*.drugClasses.flatten().toUnique().collect( { abrAbbr[ it ] } ).findAll( { it != null } ).sort()

                def bestAdditinalABRs = [:]
                stat.abr.additional.each( {
                    def abr = bestAdditinalABRs[ it.orf.start ]
                    if( abr == null  ||  it.percentSeqIdentity > abr.percentSeqIdentity )
                        bestAdditinalABRs[ it.orf.start ] = it
                } )
                stat.abr.bestAdditinalABRs = bestAdditinalABRs.values()

                model << stat
                Writer detailWriter = new StringWriter()
                detailTemplate.process( model, detailWriter )
                abrReportPath.resolve( "${genomeName}.html" ).toFile() << detailWriter.toString()

                steps.finished << stat

            } else if( stat.status == SKIPPED.toString()  ||  Files.exists( abrPath.resolve( "${genomeName}.skipped" ) ) ) {
                steps.skipped << stat
            } else {
                steps.failed << stat
            }

        } )


        // build annotations.html
        model.steps = steps
        Template template = templateConfiguration.getTemplate( 'abr.ftl' )

        Writer writer = new StringWriter()
        template.process( model, writer )
        reportsPath.resolve( "${ABR.getAbbreviation()}.html" ).toFile() << writer.toString()

    }


    @Override
    protected void clean() throws Throwable {

//        log.debug( "clean" )

    }

}