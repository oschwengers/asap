<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta_sub.ftl">

	<link href="../css/datatables.min.css" rel="stylesheet">
        <script src="../js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {
                $('#annotationTable').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'annotation-${project.genus[0]}_${genome.species}_${genome.strain}',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3, 4, 5, 6, 7, 8 ]
                            }
                        }
                    ],
                    language: {
                        decimal: ",",
                    },
                    order: [[ 2, "asc" ],[ 1, "asc" ]]
                } );
            } );
        </script>

        <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.0/d3.min.js"></script>

        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.11/c3.min.css">
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.11/c3.min.js"></script>

    <#if (noGenes>0)>
        <!-- circ plot dependencies -->
        <style>
            .lableBox {
                float: left;
                width: 20px;
                height: 5px;
                margin: 1px;
                border: 1px solid rgba(0, 0, 0, .2);
                top : 3px;
                position: relative;
            }
            .innerLegend{
                font-size : 65%;
            }
        </style>
        <script src="../js/browser-detection.js"></script>
        <script>
            var browserScaling = 1.6;
            if (isFirefox)
                browserScaling = 1.8;
            if (isBlink)
                browserScaling = 1.4;

            function saveSvg () {
                const svgToSave = document.getElementById( 'biocircos' ).innerHTML;
                if( svgToSave ){
                    const pom = document.createElement( 'a' );
                        pom.setAttribute( 'href', 'data:text/xml;charset=utf-8,' + window.encodeURIComponent( svgToSave ) );
                        pom.setAttribute( 'download', 'asap-snp-tree.svg' );
                    const event = document.createEvent( 'MouseEvents' );
                        event.initEvent( 'click', true, true );
                    pom.dispatchEvent(event);
                }
            };

        </script>
        <script src="../js/biocircos-1.1.1-own.js"></script>
    </#if>

    </head>
    <body>
        <#include "commons/header_sub.ftl">
        <div class="container-fluid">
            <div class="row">
                <#include "commons/menu_sub.ftl">

                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

                    <ol class="breadcrumb">
                        <li><a href="../index.html">Dashboard</a></li>
                        <li><a href="../annotations.html">Annotations</a></li>
                        <li class="active dropdown"><a class="dropdown-toggle" data-toggle="dropdown" href="#">${project.genus[0]}. ${genome.species} ${genome.strain} <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                            <#if (noGenes>0)><li><a href="#plotCirc">Circular Genome Plot</a></li></#if>
                                <li><a href="#downloads">Downloads</a></li>
                                <li><a href="#plotAnnotationSuccess">Annotation Success Plot</a></li>
                                <li><a href="#annotationTable">Annotation Table</a></li>
                            </ul>
                        </li>
                        <!-- trigger help-modal -->
                        <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal"></i>
                    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                      <div class="modal-dialog" role="document">
                        <div class="modal-content">
                          <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h2 class="modal-title" id="myModalLabel">ASAP annotation detail</h2>
                          </div>
                          <div class="modal-body">
                            <h3>Interactive genome plot</h3>

                            <p>The circular genome plot is generated utilising the <code>BioCircos.js</code> library. The most
                              outer circle displays the position <code>reference in million base pairs</code>. The most outer
                              <code>gene feature circles</code> display all annotated gene features from forward and reverse
                              strand. Mouse over the <code>gene features</code> to show feature start, end, type, gene name
                              and product. The <code>CDSs</code> are displayed in greyscale, <code>RNAs</code> in green and
                              <code>misc features</code> in orange. The outer circular boxplot visualizes the <code>GC content</code>                              of 1 kb bins. GC contents above the genome mean are colored in green and the ones below are
                              colored in red. The inner circular boxplot visualizes the <code>GC Skew</code> of 1 kb bins.
                              GC Skews above the genome mean are colored in purple and the ones below are colored in neon
                              green. <code>Positioning</code> of the whole genome plot can be applied via drag and drop and
                              <code>Zooming</code> can be applied via mouse wheel.</p>

                            <h3>Basic annotation statistics</h3>

                            <p>Abundance of the annotated feature types found in this genome. Visualization of the annotation
                              prediction rate.</p>

                            <h3>Interactive data table Features</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header.
                              Use the <code>Search</code> function (top right of the table) to display only genomes that
                              contain the search term in any of their table fields. The <code>number of entries</code> displayed
                              per page can be chosen on the top left of the table.</p>

                            <h3>Downloads</h3>

                            <p>Several annotation based files can be downloaded, including the genome as <code>gbk</code>, annotations
                              as <code>gff</code>, gene sequences as <code>ffn</code>, coding sequences as <code>faa</code>                              and the circular genome plot as <code>svg</code> file. The features table can be saved as comma
                              separated value (<code>csv</code>) file via click on the csv button (search and sorting are
                              contained in the downloaded file).</p>

                            <h3>Links</h3>

                            <ul>
                              <li><a href="http://bioinfo.ibp.ac.cn/biocircos/">BioCircos.js</a>; BioCircos.js: an Interactive
                                Circos JavaScript Library for Biological Data Visualization on Web Applications. Cui, Y.,
                                et al. Bioinformatics. (2016). <a href="https://www.ncbi.nlm.nih.gov/pubmed/26819473">PubMed</a>.</li>
                            </ul>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong>End</strong>: End position of the feature in base pairs.</li>

                              <li><strong>Gene</strong>: Gene name in case it is provided by the feature reference.</li>

                              <li><strong>Inference</strong>: Source the feature prediction is based on.</li>

                              <li><strong>Locus</strong>: Designation of the annotated genomic region.</li>

                              <li><strong>misc features</strong>: Miscellaneous feature an annotated genomic area that is neither
                                CDS nor RNA.</li>

                              <li><strong>Product</strong>: Short description of the product associated with the feature.</li>

                              <li><strong>Start</strong>: Start position of the feature in base pairs.</li>

                              <li><strong>Strand</strong>: The forward/plus strand is marked via '+' and the reverse/minus strand
                                is marked with '-'.</li>

                              <li><strong>Type</strong>: Designated group of this gene feature.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                    <#-- content start -->

                    <div class="row">

                    <#if (noGenes>0)>
                        <!-- circ genome plot -->
                        <div id="plotCirc" class="col-sm-8 col-md-6">
                            <div id="biocircos"></div>
                            <script src="./${genomeName}/data.js"></script>
                            <script>
                                const organismNameTextGenusSpecies = ["TEXT01", {
                                        x: -${circPlot.moveToMiddleOrganismNameDisplay?c} * browserScaling,
                                        y: -25,
                                        textSize: 18,
                                        textStyle: "italic",
                                        textWeight: "bold", //normal,bold,bolder,lighter,100,200,300,400,500,600,700,800,900
                                        textColor: "black",
                                        textOpacity: 1.0,
                                        text: "${circPlot.organismNameDisplayGenusSpecies}"
                                    }];
                                const organismNameTextStrandDetail = ["TEXT02", {
                                        x: -${circPlot.moveToMiddleOrganismNameDisplay?c} * browserScaling,
                                        y: 0,
                                        textSize: 18,
                                        textWeight: "bold", //normal,bold,bolder,lighter,100,200,300,400,500,600,700,800,900
                                        textColor: "black",
                                        textOpacity: 1.0,
                                        text: "${circPlot.organismNameDisplayStrandDetail}"
                                    }];
                                const organismSizeTextMiddle = ["TEXT03", {
                                        x: -${circPlot.moveToMiddleOrganismNameDisplay?c} * browserScaling,
                                        y: 25,
                                        textSize: 18,
                                        textWeight: "bold", //normal,bold,bolder,lighter,100,200,300,400,500,600,700,800,900
                                        textColor: "black",
                                        textOpacity: 1.0,
                                        text: "${circPlot.genomeSize} bp"
                                    }];

                                <!-- Genome configuration -->
                                const bioCircosGenome = [// Configure your own genome here.
                                    ["", ${circPlot.genomeSize?c}], // optional $organismName in first ""
                                ];
                                const bioCircos = new BioCircos( organismNameTextGenusSpecies, organismNameTextStrandDetail, organismSizeTextMiddle,
                                    strandPlus, strandMinus, gcContentPos, gcContentNeg,
                                    gcSkewPos, gcSkewNeg, bioCircosGenome,
                                    {// Initialize BioCircos.js with "BioCircosGenome" and Main configuration
                                        target: "biocircos",
                                        svgWidth:  580,
                                        svgHeight: 580,
                                        innerRadius: 300 * ${circPlot.plotScalingFactor?c},
                                        outerRadius: 300 * ${circPlot.plotScalingFactor?c},
                                        zoom: true,
                                        genomeFillColor: ["#999999"],
                                        ticks: {
                                            display: true,
                                            len: 5,
                                            color: "#000",
                                            textSize: 15,
                                            textColor: "#000",
                                            scale: 1000000 // factor genome size is devided for display outer ring
                                        },
                                        genomeLabel: {
                                            display: false
                                        },
                                        // Main configuration - Customize events - ARC module
                                        ARCMouseEvent: true,
                                        ARCMouseOutDisplay: true,
                                        ARCMouseOutAnimationTime: 500,
                                        ARCMouseOutColor: "none",
                                        ARCMouseOutArcOpacity: 1.0,
                                        ARCMouseOutArcStrokeColor: "none",
                                        ARCMouseOutArcStrokeWidth: 0,
                                        ARCMouseOverDisplay: true,
                                        ARCMouseOverColor: "red",
                                        ARCMouseOverArcOpacity: 1.0,
                                        ARCMouseOverArcStrokeColor: "none",
                                        ARCMouseOverArcStrokeWidth: 3,
                                        // Main configuration - Customize Tooltips - ARC module
                                        ARCMouseOverTooltipsHtml01: "",
                                        ARCMouseOverTooltipsHtml02: "Start : ",
                                        ARCMouseOverTooltipsHtml03: "<br>End : ",
                                        ARCMouseOverTooltipsHtml04: "<br>",
                                        ARCMouseOverTooltipsPosition: "absolute",
                                        ARCMouseOverTooltipsBackgroundColor: "white",
                                        ARCMouseOverTooltipsBorderStyle: "solid",
                                        ARCMouseOverTooltipsBorderWidth: 0,
                                        ARCMouseOverTooltipsPadding: "3px",
                                        ARCMouseOverTooltipsBorderRadius: "3px",
                                        ARCMouseOverTooltipsOpacity: 0.8,
                                    });
                                bioCircos.draw_genome( bioCircos.genomeLength );  // BioCircos.js callback
                            </script>
                            <div class="innerLegend" style="float : left;">
                                <div style="float: left;">
                                    <div><strong>Features on +/- strand:</strong></div>
                                    <br>
                                    <div style="position: relative; left: 20px;">
                                        <div style="float: left;">CDS: </div>
                                        <div class="lableBox" style="background-color: #000000;" ></div>
                                        <div class="lableBox" style="background-color: #d3d3d3;" ></div>
                                        <div class="lableBox" style="background-color: #a9a9a9;" ></div>
                                        <div class="lableBox" style="background-color: #808080;" ></div>
                                        <br>
                                        <div style="float: left;">RNA: </div>
                                        <div class="lableBox" style="background-color: #008000; left : 1px" ></div>
                                        <br>
                                        <div style="float: left;">Miscellaneous: </div>
                                        <div class="lableBox" style="background-color: #f4b400;" ></div>
                                    </div>
                                </div>
                                <div style="float: left; position: relative; left: 50px;">
                                    <div style="float: left;"><strong>GC-content (1.000 bp window):</strong></div>
                                    <br>
                                    <div style="position: relative; left: 20px;">
                                        <div style="float: left;">above mean:</div>
                                        <div class="lableBox" style="background-color: #30bb71;" ></div>
                                        <br>
                                        <div style="float: left;">below mean:</div>
                                        <div class="lableBox" style="background-color: #cd2d50;" ></div>
                                    </div>
                                </div>
                                <div style="float: left; position: relative; left: 100px;">
                                    <div style="float: left;"><strong>GC Skew [(G - C)/(G + C)] (1.000 bp window):</strong></div>
                                    <br>
                                    <div style="position: relative; left: 20px;">
                                        <div style="float: left;">above mean:</div>
                                        <div class="lableBox" style="background-color: DarkMagenta;" ></div>
                                        <br>
                                        <div style="float: left;">below mean:</div>
                                        <div class="lableBox" style="background-color: #C0FF3E;" ></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </#if>

                        <!-- Annotated Feature Type Overview -->
                        <div class="col-md-3 col-sm-3">
                            <table class="table table-hover table-condensed">
                                <caption>Annotated Feature Types</caption>
                                <tbody>
                                    <tr>
                                        <td>genes</td>
                                        <td class="text-center">${noGenes}</td>
                                    </tr>
                                    <tr>
                                        <td><abbr title="coding sequence">CDS</abbr>s</td>
                                        <td class="text-center">${noCds}</td>
                                    </tr>
                                    <tr>
                                        <td><abbr title="hypothetical">hyp.</abbr> proteins</td>
                                        <td class="text-center">${noHypProt}</td>
                                    </tr>
                                    <tr>
                                        <td><abbr title="non-coding RNA">ncRNA</abbr>s</td>
                                        <td class="text-center">${noNcRna}</td>
                                    </tr>
                                    <tr>
                                        <td>CRISPR/CAS</td>
                                        <td class="text-center">${noCRISPR}</td>
                                    </tr>
                                    <tr>
                                        <td><abbr title="ribosomal RNA">rRNA</abbr>s</td>
                                        <td class="text-center">${noRRna}</td>
                                    </tr>
                                    <tr>
                                        <td><abbr title="transfer RNA">tRNA</abbr>s</td>
                                        <td class="text-center">${noTRna}</td>
                                    </tr>
                                    <tr>
                                        <td><abbr title="trans-membrane RNA">tmRNA</abbr>s</td>
                                        <td class="text-center">${noTmRna}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                        <!-- downloads -->
                        <div id="downloads" class="col-md-3 col-sm-3">
                            <table class="table table-hover table-condensed">
                                <caption>Downloads</caption>
                                <tbody>
                                    <tr>
                                        <td>Genome</td>
                                        <td class="text-center"><a href="./${genomeName}/${genomeName}.gbk">gbk</a></td>
                                    </tr>
                                    <tr>
                                        <td>Annotations</td>
                                        <td class="text-center"><a href="./${genomeName}/${genomeName}.gff">gff</a></td>
                                    </tr>
                                    <tr>
                                        <td>Gene Sequences</td>
                                        <td class="text-center"><a href="./${genomeName}/${genomeName}.ffn">ffn</a></td>
                                    </tr>
                                    <tr>
                                        <td>Coding Sequences</td>
                                        <td class="text-center"><a href="./${genomeName}/${genomeName}.faa">faa</a></td>
                                    </tr>
                                    </tr>
                                    <tr>
                                        <td>Circular Genome Plot</td>
                                        <td class="text-center"><a href="javascript:saveSvg()">svg</a></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                            <!-- Gene Prediction Gauge Plot -->
                        <div id="plotAnnotationSuccess" class="col-md-3 col-sm-4">
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h3 class="panel-title">Annotations / Predictions</h3>
                                </div>
                                <div id='chartGauge'></div>
                                <script type="text/javascript">
                                    var chart = c3.generate( {
                                        bindto: '#chartGauge',
                                        data: {
                                            columns: [
                                                [ 'annotated', ${noAnnotations?c} ]
                                            ],
                                            type: 'gauge'
                                        },
                                        gauge: {
                                            label: {
                                                show: true
                                            },
                                            min: 0,
                                            max: ${noGenes?c}
                                        },
                                        color: {
                                            pattern: ['#FF0000', '#F97600', '#F6C600', '#008000'], // color levels for the percentage values
                                            threshold: {
                                                values: [60, 80, 90, 100]
                                            }
                                        },
                                        size: {
                                            height: 180
                                        }
                                    } );
                                </script>
                            </div>
                        </div>
                    </div>

                    <div class="row voffset">
                        <!-- Contig Table -->
                        <div class="col">
                            <h2><small>Features</small></h2>
                            <table id="annotationTable" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th>Locus</th>
                                        <th class="text-center">Type</th>
                                        <th class="text-center">Start</th>
                                        <th class="text-center">End</th>
                                        <th class="text-center">Strand</th>
                                        <th class="text-center">Gene</th>
                                        <th class="text-center">Product</th>
                                        <th class="text-center">EC</th>
                                        <th class="text-center">Inference</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <#list features as f>
                                    <tr>
                                        <td>${f.locusTag}</td>
                                        <td class="text-center">${f.type}</td>
                                        <td class="text-center">${f.start}</td>
                                        <td class="text-center">${f.end}</td>
                                        <td class="text-center">${f.strand}</td>
                                        <td class="text-center">${f.gene}</td>
                                        <td class="text-center">${f.product}</td>
                                        <td class="text-center"><#if f.ec?has_content>${f.ec}</#if></td>
                                        <td class="text-center">${f.inference}</td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <#-- content end -->

                </div>
            </div>
        </div>
        <#include "commons/footer_sub.ftl">
    </body>
</html>
