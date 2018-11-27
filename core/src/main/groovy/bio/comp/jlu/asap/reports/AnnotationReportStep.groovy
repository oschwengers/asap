
package bio.comp.jlu.asap.reports

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import groovy.util.logging.Slf4j
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import freemarker.template.Configuration
import freemarker.template.Template
import bio.comp.jlu.asap.api.FileType
import bio.comp.jlu.asap.reports.ReportStep

import static bio.comp.jlu.asap.api.GenomeSteps.ANNOTATION
import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 * Creates an HTML report page for assemblies.
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class AnnotationReportStep extends ReportStep {

    private final Path contigsAnnotatedPath
    private final Path contigsAnnotatedReportsPath


    public AnnotationReportStep( def config, Configuration templateConfiguration ) {

        super( ANNOTATION.getAbbreviation(), config, templateConfiguration )

        contigsAnnotatedPath = projectPath.resolve( PROJECT_PATH_ANNOTATIONS )
        contigsAnnotatedReportsPath = reportsPath.resolve( PROJECT_PATH_ANNOTATIONS )

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
        Files.createDirectory( contigsAnnotatedReportsPath )
        Path jsPath = reportsPath.resolve( 'js' )
        String packagePrefix = '/bio/comp/jlu/asap/reports/templates'
        [
            [ jsPath, 'plotly-1.1.0.min.js' ],
            [ jsPath, 'biocircos-1.1.1-own.js' ],
            [ jsPath, 'browser-detection.js' ]
        ].each( { path, name ->
            InputStream is =  getClass().getResourceAsStream( "${packagePrefix}/annotations/${name}" )
            Files.copy( is, path.resolve( name ), StandardCopyOption.REPLACE_EXISTING )
        } )

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
            genomeSize: [],
            noGenes: [],
            noCds: [],
            noHypProt: [],
            noNcRna: [],
            noCRISPR: [],
            noRRna: [],
            noTRna: [],
        ]

        model.nanopore = false

        // build detail pages
        Template detailTemplate = templateConfiguration.getTemplate( "annotations_details.ftl" )
        config.genomes.each( { genome ->

            def stat = [
                genome: [
                    id: genome.id,
                    species: genome.species,
                    strain: genome.strain
                ],
                status: genome.steps[ ANNOTATION.getAbbreviation() ]?.status
            ]

            if( stat.status == FINISHED.toString() ) {

                String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
                Path infoJsonPath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName, 'info.json' )
                stat << (new JsonSlurper()).parseText( infoJsonPath.toFile().text )
                stat.genomeName = genomeName
                stat.features = stat.features.sort( { a,b -> a.start <=> b.start } )

                // add warning for nanopore only assemblies
                stat.nanopore = ( genome.data.size() == 1  &&  FileType.getEnum( genome.data[0].type ) == FileType.READS_NANOPORE )
                if( stat.nanopore ) {
                    model.nanopore = true
                }

                Path contigsAnnotatedGenomeReportsPath = contigsAnnotatedReportsPath.resolve( genomeName )
                Files.createDirectory( contigsAnnotatedGenomeReportsPath )


                // create hard links to download files
                [
                    "${genomeName}.gbk",
                    "${genomeName}.gff",
                    "${genomeName}.ffn",
                    "${genomeName}.faa"
                ].each( {
                    Files.createLink( contigsAnnotatedGenomeReportsPath.resolve( it ), Paths.get ( contigsAnnotatedPath.toString(), genomeName, it ) )
                } )


                // setup circular genome plot
                if( stat.noGenes > 0 ) {
                    Path genomeGffPath = Paths.get ( contigsAnnotatedPath.toString(), genomeName, "${genomeName}.gff" )
                    stat.circPlot = setupCircularGenomePlot( genomeName, stat, genomeGffPath, contigsAnnotatedReportsPath.resolve( genomeName ) )
                }

                // collect data for assembly chart
                model.chartData.genomeSize << stat.genomeSize
                model.chartData.noGenes << stat.noGenes
                model.chartData.noCds << stat.noCds
                model.chartData.noHypProt << stat.noHypProt
                model.chartData.noNcRna << stat.noNcRna
                model.chartData.noCRISPR << stat.noCRISPR
                model.chartData.noRRna << stat.noRRna
                model.chartData.noTRna << stat.noTRna

                model << stat
                Writer detailWriter = new StringWriter()
                detailTemplate.process( model, detailWriter )
                contigsAnnotatedReportsPath.resolve( "${genomeName}.html" ).toFile() << detailWriter.toString()

                // remove unnecessary memory payload
                stat.remove( 'features' )
                stat.remove( 'circPlot' )

                model.steps.finished << stat

            } else if( stat.status == SKIPPED.toString() )
                model.steps.skipped << stat
            else
                model.steps.failed << stat

        } )


        // build annotations.html
        def genomeSizes = (model.steps.finished*.genomeSize).findAll( { it != null } ).collect { (it as double) / 1000000 }
        def noGenes = (model.steps.finished*.noGenes).findAll( { it != null } )
        def noHypGenes = (model.steps.finished*.noHypProt).findAll( { it != null } )
        log.debug( "genomeSizes: ${genomeSizes}" )
        log.debug( "noGenes: ${noGenes}" )
        log.debug( "noHypGenes: ${noHypGenes}" )
        assert genomeSizes.size() == noGenes.size()
        assert noGenes.size() == noHypGenes.size()
        def sumAnnotations = []
        noGenes.eachWithIndex( { it, i -> sumAnnotations << (it as long) - (noHypGenes[i] as long) } )
        log.debug( "sumAnnotation: ${sumAnnotations}" )
        model << [
            genomeSizes: genomeSizes.join( ',' ),
            noPredictions: noGenes.join( ',' ),
            noAnnotations: sumAnnotations.join( ',' )
        ]

        Template template = templateConfiguration.getTemplate( 'annotations.ftl' )
        Writer writer = new StringWriter()
        template.process( model, writer )
        reportsPath.resolve( 'annotations.html' ).toFile() << writer.toString()

    }


    @Override
    protected void clean() throws Throwable {

        log.trace( 'clean' )

    }


    private def setupCircularGenomePlot( String genomeName, def stat, Path genomeGffPath, Path genomeReportPath ) {

        /***************************
         * scaling factor **********
         ***************************/
        double PLOT_SCALING_FACTOR = 0.9

        def plusStrand  = []
        def minusStrand = []
        int changeColorsPlus  = 0
        int changeColorsMinus = 0
        def switchColorGene = [ '#000000', '#d3d3d3', '#a9a9a9', '#808080' ]
        stat.features.each( { feat ->
            def gene = [
                chr: '', //optional for matching genomeName
                start: feat.start,
                end: feat.end,
                des: "Type: ${feat.type}" // makes sure that only individual available feature information is shown
            ]
            if( feat.type == 'CDS' ) { // color is set for CDS features independent roatating through the switchColorGene list for plus and minus strand
                gene.color =  switchColorGene[ (feat.strand == '+' ? changeColorsPlus++ : changeColorsMinus++) % 4 ]
            } else if( feat.type == 'repeat_region' ) { // color is set for CRISPR features
                gene.color = '#f4b400'
            } else {
                gene.color = '#008000'
            }
            if( feat.gene != '' ) gene.des += "<br>Gene: ${feat.gene}"
            if( feat.product != '' ) gene.des += "<br>Product: ${feat.product}"
            if( feat.ec  &&  feat.ec != '' ) gene.des += "<br>EC: ${feat.ec}"
            if ( feat.strand == '+' ) // sorting in plus and minus strand
                plusStrand << gene
            else
                minusStrand << gene
        } )

        // create JS data file
        File jsDataFile = genomeReportPath.resolve( 'data.js' ).toFile()
        jsDataFile.text = "//genome: ${genomeName}\n"

        //generate ARC strandPlus var
        def plusStrandARC = [ 'ARC01', [innerRadius : -28* PLOT_SCALING_FACTOR, outerRadius : -5* PLOT_SCALING_FACTOR], plusStrand ]
        jsDataFile << 'var strandPlus = ' + JsonOutput.toJson( plusStrandARC ) + '\n'

        //generate ARC strandMinus var
        def minusStrandARC = [ 'ARC02', [innerRadius : -55* PLOT_SCALING_FACTOR, outerRadius : -32* PLOT_SCALING_FACTOR], minusStrand ]
        jsDataFile << 'var strandMinus = ' + JsonOutput.toJson( minusStrandARC ) + '\n'


        // generate HISTOGRAM String for GC content & skew

        // get genome sequence
        StringBuilder sb = new StringBuilder( 10000000 )
        boolean isSequencePart = false
        genomeGffPath.eachLine( { line ->
            char firstChar = line.charAt( 0 )
            if( firstChar == '>' ) {
                isSequencePart = true
            } else if( isSequencePart ) {
                sb.append( line.trim().replaceAll( '\n', '' ) )
            }
        } )

        String genomeSequence =  sb.toString()
        def numPartsForGC = 1000 // determines the number calculated parts
        def gcPartSize = stat.genomeSize / numPartsForGC

        def partOfGenomeGCValue = []
        def partOfGenomeGCValueSkew = []
        numPartsForGC.times( {

            int start = Math.round( it * gcPartSize )
            int end   = Math.round( ( it+1 ) * gcPartSize ) - 1
            String subsequence = genomeSequence.substring( start, end )
            int numOfC = subsequence.count( 'C' )
            int numOfG = subsequence.count( 'G' )
            int numOfA = subsequence.count( 'A' )
            int numOfT = subsequence.count( 'T' )

            int sum = numOfC+numOfG+numOfA+numOfT
            def partOfGenomeGCMainData = [ // same for GC content and skew
                chr : '', //optional for matching genomeName
                name : '',
                start : start,
                end : end,
                value : ( sum ) > 0 ? ( numOfC+numOfG ).div( sum ) : 0
            ]
            partOfGenomeGCValue << partOfGenomeGCMainData

            def copy = partOfGenomeGCMainData.clone()
            copy.value = (numOfG+numOfC) > 0 ? ( numOfG-numOfC ) / ( numOfG+numOfC ) : 0 // set the value for GC skew
            partOfGenomeGCValueSkew << copy

        } )

        // HISTOGRAM scaling element for GC content & skew
        def scalingMainData = [ // general data for the scaling element
            chr: '', //optional for matching genomeName
            start: 0,
            end: 0,
            name: 'scale'
        ]

        double averageGC = partOfGenomeGCValue*.value.sum() / partOfGenomeGCValue.size() // average GC value
        partOfGenomeGCValue.each( {it.value -= averageGC } ) // subtracting average GC value for splitting values in two even group (positve / negative)
        def partOfGenomeGCValuePos = []
        def partOfGenomeGCValueNeg = []
        partOfGenomeGCValue.each( { // splitting positive and negative values
            if ( it.value < 0 ) partOfGenomeGCValueNeg << it
            else                partOfGenomeGCValuePos << it
        } )
        partOfGenomeGCValueNeg.each( {it.value = -it.value} ) // changing value to positive for display
        scalingMainData.value = Math.max( Math.abs( partOfGenomeGCValuePos*.value.max() ), Math.abs( partOfGenomeGCValueNeg*.value.min() ) ) // determining max value for even display of positive and negative values
        partOfGenomeGCValuePos << scalingMainData // adding non displayed scaling element for same scaling of positive and negative values
        partOfGenomeGCValueNeg << scalingMainData // adding non displayed scaling element for same scaling of positive and negative values

        double averageGCSkew = partOfGenomeGCValueSkew*.value.sum() / partOfGenomeGCValueSkew.size() //average GC skew value
        partOfGenomeGCValueSkew.each( { it.value -= averageGCSkew } ) // subtracting average GC skew value for splitting values in two even group (positve / negative )
        def partOfGenomeGCValueSkewPos = []
        def partOfGenomeGCValueSkewNeg = []
        partOfGenomeGCValueSkew.each( { // splitting positive and negative values
            if( it.value < 0 )  partOfGenomeGCValueSkewNeg << it
            else                partOfGenomeGCValueSkewPos << it
        } )
        partOfGenomeGCValueSkewNeg.each( { it.value = -it.value } ) // changing value to positive for display

        def scalingMainDataCopy = scalingMainData.clone()
        scalingMainDataCopy.value = Math.max( Math.abs( partOfGenomeGCValueSkewPos*.value.max() ), Math.abs( partOfGenomeGCValueSkewNeg*.value.min() ) ) // determining max value for even display of positive and negative values
        partOfGenomeGCValueSkewPos << scalingMainDataCopy // adding non displayed scaling element for same scaling of positive and negative values
        partOfGenomeGCValueSkewNeg << scalingMainDataCopy // adding non displayed scaling element for same scaling of positive and negative values


        // generate HISTOGRAM vars for GC content & skew
        def GCContentPos = [ 'HISTOGRAM11', [ maxRadius: 235* PLOT_SCALING_FACTOR, minRadius: 200* PLOT_SCALING_FACTOR, histogramFillColor: '#30bb71' ], partOfGenomeGCValuePos ]
        jsDataFile << 'var gcContentPos = ' + JsonOutput.toJson( GCContentPos ) + '\n'

        def GCContentNeg = [ 'HISTOGRAM12', [ maxRadius: 165* PLOT_SCALING_FACTOR, minRadius: 200* PLOT_SCALING_FACTOR, histogramFillColor: '#cd2d50' ], partOfGenomeGCValueNeg ]
        jsDataFile << 'var gcContentNeg = ' + JsonOutput.toJson( GCContentNeg ) + '\n'

        def GCSkewPos = [ 'HISTOGRAM21', [ maxRadius: 165* PLOT_SCALING_FACTOR, minRadius: 130* PLOT_SCALING_FACTOR, histogramFillColor: 'DarkMagenta' ], partOfGenomeGCValueSkewPos ]
        jsDataFile << 'var gcSkewPos = ' + JsonOutput.toJson( GCSkewPos ) + '\n'

        def GCSkewNeg = [ 'HISTOGRAM22', [ maxRadius: 95* PLOT_SCALING_FACTOR, minRadius: 130* PLOT_SCALING_FACTOR, histogramFillColor: '#C0FF3E' ], partOfGenomeGCValueSkewNeg ]
        jsDataFile << 'var gcSkewNeg = ' + JsonOutput.toJson( GCSkewNeg ) + '\n'


        // positioning and resizing of organism Name and genome size
        def organismNameDisplay = genomeName.replaceAll( /_/, ' ' )
        def moveToMiddleGenomeSize = 3 * ( stat.genomeSize as String ).size() // factor empirically determined might vary with different char sequences
        def moveToMiddleOrganismNameDisplay = 2 * organismNameDisplay.size()
        def splittedName = organismNameDisplay.split( ' ' )
        def organismNameDisplayStrandDetail = ' ' + splittedName[2]
        def organismNameDisplayGenusSpecies
        if( genomeName.length() > 32 ) { // if the organism name is too long for proper display the first word is abbreviated with 'first letter'.
            organismNameDisplayGenusSpecies = splittedName[0].substring( 0,1 ) + '. ' + splittedName[1]
            moveToMiddleOrganismNameDisplay = ( organismNameDisplayGenusSpecies.size()+ organismNameDisplayStrandDetail.size() )* 1.4
        } else{
            organismNameDisplayGenusSpecies = splittedName[0] + ' ' + splittedName[1]
            moveToMiddleOrganismNameDisplay = ( organismNameDisplayGenusSpecies.size()+ organismNameDisplayStrandDetail.size() )* 1.4
        }


        // return object with plot parameter
        return [
            'moveToMiddleOrganismNameDisplay' : moveToMiddleOrganismNameDisplay,
            'organismNameDisplayGenusSpecies' : organismNameDisplayGenusSpecies,
            'organismNameDisplayStrandDetail' : organismNameDisplayStrandDetail,
            'plotScalingFactor' :  PLOT_SCALING_FACTOR,
            'genomeSize' : stat.genomeSize,
            'organismName' : genomeName
        ]

    }

}

