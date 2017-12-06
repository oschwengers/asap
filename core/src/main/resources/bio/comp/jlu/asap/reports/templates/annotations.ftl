<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta.ftl">

	<link href="css/datatables.min.css" rel="stylesheet">
        <script src="js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {
                $('#annotations').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
                    columnDefs: [
                        { orderable: false, targets: [9,10] }
                    ],
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'annotations',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3, 4, 5, 6, 7, 8 ]
                            }
                        }
                    ],
                    language: {
                        decimal: ",",
                    }
                } );
            } );
        </script>

        <script src="js/gradient.js" defer></script>

    </head>
    <body>
        <#include "commons/header.ftl">
        <div class="container-fluid">
            <div class="row">
                <#include "commons/menu.ftl">

                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

                    <ol class="breadcrumb">
                        <li><a href="index.html">Dashboard</a></li>
                        <li class="active">Annotations</li>
                        <!-- trigger help-modal -->
                        <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal"></i>
                    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                      <div class="modal-dialog" role="document">
                        <div class="modal-content">
                          <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h2 class="modal-title" id="myModalLabel">ASAP annotation overview</h2>
                          </div>
                          <div class="modal-body">
                            <p>
                              To annotate contigs and scaffolds ASA³P internally uses <code>Prokka</code> and <code>Barrnap</code>. For high quality annotation genus
                              specific information is used. Therefore, ASA³P uses genus specific Blast databases comprising
                              all <code>RefSeq</code> genome annotations related to a certain genus. In order to further increase
                              annotation quality ASA³P uses a combination of smaller high quality databases such as <code>ResFinder</code>                            for antimicrobial resistance genes and <code>VFDB</code> for virulence factors.</p>

                            <h3>Interactive dotplot</h3>

                            <p>Via the radio buttons on the right <code>key data</code> for X and Y axis can be selected. Mouse
                              over a dot of interest to display the according <code>genome name</code> as well as horizontal
                              and vertical value extensions. <code>Zooming</code> can be applied via marking the area of
                              interest with left mouse button down. To reset the view right click.</p>

                            <h3>Interactive data table</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header.
                              Use the <code>Search</code> function (top right of the table) to display only genomes that
                              contain the search term in any of their table fields. The <code>number of entries</code> displayed
                              per page can be chosen on the top left of the table. <code>Blue horizontal bar plots</code>                              are displayed in columns containing numeric values. They visualize the relative relation of
                              this value compared to the according values of the other genomes.</p>

                            <h3>Downloads</h3>

                            <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv
                              button (search and sorting are contained in the downloaded file). To download the GenBank (<code>gbk</code>)
                              or General Feature Format (<code>gff</code>) file of a particular genome assembly click on
                              gbk or gff in the data table.</p>

                            <h3>Links</h3>

                            <ul>
                              <li><a href="http://www.vicbioinformatics.com/software.barrnap.shtml">Barrnap</a>; Barrnap predicts
                                the location of ribosomal RNA genes in genomes. It supports bacteria (5S,23S,16S), archaea
                                (5S,5.8S,23S,16S), mitochondria (12S,16S) and eukaryotes (5S,5.8S,28S,18S). <a href="https://github.com/tseemann/barrnap">GitHub</a>.</li>

                              <li><code>Details</code> on the annotation of a particular genome can be accessed via click on
                                the magnifying glass in the overview table.</li>

                              <li><a href="http://www.vicbioinformatics.com/software.prokka.shtml">Prokka</a>: Seemann T. Prokka:
                                rapid prokaryotic genome annotation. Bioinformatics. 2014 Jul 15;30(14):2068-9. PMID:24642063
                                <a href="https://www.ncbi.nlm.nih.gov/pubmed/24642063">PubMed</a>.</li>

                              <li><a href="https://www.ncbi.nlm.nih.gov/refseq/">RefSeq</a>: O'Leary, Nuala A., et al. "Reference
                                sequence (RefSeq) database at NCBI: current status, taxonomic expansion, and functional annotation."
                                Nucleic acids research (2015): gkv1189. <a href="https://www.ncbi.nlm.nih.gov/pubmed/26553804">PubMed</a>.</li>

                              <li><a href="https://cge.cbs.dtu.dk/services/ResFinder/">ResFinder</a>: Identification of acquired
                                antimicrobial resistance genes. Zankari E, Hasman H, Cosentino S, Vestergaard M, Rasmussen
                                S, Lund O, Aarestrup FM, Larsen MV. J Antimicrob Chemother. 2012 Jul 10. <a href="https://www.ncbi.nlm.nih.gov/pubmed/22782487">PubMed</a>.</li>

                              <li><a href="http://www.mgc.ac.cn/VFs/main.htm">VFDB</a>: Chen LH, Zheng DD, Liu B, Yang J and
                                Jin Q, 2016. VFDB 2016: hierarchical and refined dataset for big data analysis-10 years on.
                                Nucleic Acids Res. 44(Database issue):D694-D697. <a href="https://www.ncbi.nlm.nih.gov/pubmed/26578559">PubMed</a>.</li>
                            </ul>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong># CDS</strong>: Number of coding DNA sequences found.</li>

                              <li><strong># CRISPR/CAS</strong>: Number of CRISPR cassettes found.</li>

                              <li><strong># Genes</strong>: Number of genes found.</li>

                              <li><strong>Genome</strong>: Name of the processed genome.</li>

                              <li><strong># Hyp. Proteins</strong>: Number of hypothetical protein coding genes found.</li>

                              <li><strong># ncRNA</strong>: Number of non coding RNA genes found.</li>

                              <li><strong># rRNA</strong>: Number of ribosomal RNA genes found.</li>

                              <li><strong># tRNA</strong>: Number of transfer RNA genes found.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                    <#-- content start -->
                <#if (steps.finished?size > 2)>
                    <div class="row" id="charts">
                        <div class="col-md-10">

                            <div id="chartAnnotations"></div>

                            <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
                            <script type="text/javascript">

                                // get values from Groovy
                                const genomes = [ <#list steps.finished as step>'${project.genus[0]}. ${step.genome.species} ${step.genome.strain}',</#list> ];
                                const genomeSize = [ <#list chartData.genomeSize as val>${val?c},</#list> ];
                                const noGenes = [ <#list chartData.noGenes as val>${val?c},</#list> ];
                                const noCds = [ <#list chartData.noCds as val>${val?c},</#list> ];
                                const noHypProt = [ <#list chartData.noHypProt as val>${val?c},</#list> ];
                                const noNcRna = [ <#list chartData.noNcRna as val>${val?c},</#list> ];
                                const noCRISPR = [ <#list chartData.noCRISPR as val>${val?c},</#list> ];
                                const noRRna = [ <#list chartData.noRRna as val>${val?c},</#list> ];
                                const noTRna = [ <#list chartData.noTRna as val>${val?c},</#list> ];

                                // load google charts
                                google.charts.load( 'current', { 'packages': ['corechart'] } );

                                // preselect the first 2 choices
                                createPlot( "Genome Size", "# Genes", genomeSize, noGenes );

                                function updateGraph() {
                                    // getting reference to radio box array
                                    const xboxes = document.forms['menu'].elements[ 'xaxis' ];
                                    const yboxes = document.forms['menu'].elements[ 'yaxis' ];
                                    const ids = [];
                                    const valueArrays = [];
                                    // getting IDs of checked boxes
                                    for (let i=0, len=xboxes.length; i<len; i++) {
                                        if (xboxes[i].checked){ids.push(xboxes[i].id)}
                                    }
                                    for (let i=0, len=yboxes.length; i<len; i++) {
                                        if (yboxes[i].checked){ids.push(yboxes[i].id)}
                                    }

                                    // getting arrays of json values for according IDs
                                    for (let i=0; i<2; i++){
                                        if (ids[i] == "Genome Size" ) valueArrays.push( genomeSize )
                                        else if (ids[i] == "# Genes" ) valueArrays.push( noGenes )
                                        else if (ids[i] == "# Annotations" ) valueArrays.push( noAnnotations )
                                        else if (ids[i] == "# CDS" ) valueArrays.push( noCds )
                                        else if (ids[i] == "# Hyp. Proteins" ) valueArrays.push( noHypProt )
                                        else if (ids[i] == "# ncRNA" ) valueArrays.push( noNcRna )
                                        else if (ids[i] == "# CRISPR/CAS" ) valueArrays.push( noCRISPR )
                                        else if (ids[i] == "# rRNA" ) valueArrays.push( noRRna )
                                        else if (ids[i] == "# tRNA" ) valueArrays.push( noTRna )
                                    }
                                    // passing axis names (from IDs) and arrays with json values to createPlot function
                                    createPlot( ids[0], ids[1], valueArrays[0], valueArrays[1] );
                                }

                                // function to draw the graph with desired values. The name for x- and y-axis as well as
                                // the two arrays containing the values are passed as arguments
                                function createPlot( x_name, y_name, x_array, y_array ) {

                                    google.charts.setOnLoadCallback( function() {

                                        const data = new google.visualization.DataTable();

                                        data.addColumn( 'number', x_name );
                                        data.addColumn( 'number', y_name );
                                        data.addColumn({type: 'string', role: 'tooltip', 'p': {'html': true}});

                                        for( let i = 0; i < x_array.length; i++ ){
                                            while(genomes[i].indexOf(' ') !== -1)
                                                genomes[i] = genomes[i].replace(" ", "&nbsp")
                                            data.addRow( [ { v: x_array[i], f: genomes[i] }, y_array[i],
                                                `
                                                    <div style="padding:1.3rem;">
                                                        <p style="margin:0;"><b>Genome</b>&nbsp` + genomes[i] + `</p>
                                                        <hr style="height: 1px; border: 0; border-top: 1px solid #ccc; margin: 10px 0;">
                                                        <p style="margin:0;"><b>`+y_name+`</b></p>
                                                        <p style="margin:0; margin-bottom:5px;">`+ y_array[i].toLocaleString() + `</p>
                                                        <p style="margin:0;"><b>`+x_name+`</b></p>
                                                        <p style="margin:0;">`+x_array[i].toLocaleString()+`</p>
                                                    </div>
                                                `
                                            ] );
                                        }

                                        const options = {
                                            tooltip: {isHtml: true},
                                            hAxis: {title: x_name, minValue: Math.min(x_array), maxValue: Math.max(x_array)},
                                            vAxis: {title: y_name, minValue: Math.min(y_array), maxValue: Math.max(y_array)},
                                            chartArea:{left:100,top:10,width:"90%",height:"80%"},
                                            height: 500,
                                            pointSize: 3,
                                            axisTitlesPosition: 'out',
                                            crosshair: { trigger: 'focus' },
                                            explorer: {
                                                actions: ['dragToZoom', 'rightClickToReset'],
                                                // keepInBounds: true,
                                            },
                                            legend: 'none'
                                        };

                                        const chart = new google.visualization.ScatterChart( document.getElementById( 'chartAnnotations' ) );
                                            chart.draw( data, options );

                                        document.getElementById('chartAnnotations').addEventListener('contextmenu', function (ev) {
                                            var chart = new google.visualization.ScatterChart(document.getElementById('chartAnnotations'));
                                            chart.draw(data, options);
                                        });

                                    } )

                                }
                            </script>
                        </div>
                        <div class="col-md-2">
                            <form name="menu">
                                <table class="table table-condensed table-borderless">
                                    <tr>
                                        <th></th>
                                        <th>X</th>
                                        <th>Y</th>
                                    </tr>
                                    <tr>
                                        <td>Genome Size</td>
                                        <td><input type="radio" name="xaxis" value="1" id="Genome Size" checked="true" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="1" id="Genome Size" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td># Genes</td>
                                        <td><input type="radio" name="xaxis" value="1" id="# Genes" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="1" id="# Genes" checked="true" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td># CDS</td>
                                        <td><input type="radio" name="xaxis" value="5" id="# CDS" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="5" id="# CDS" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td># Hyp. Proteins</td>
                                        <td><input type="radio" name="xaxis" value="4" id="# Hyp. Proteins" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="4" id="# Hyp. Proteins" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td># ncRNA</td>
                                        <td><input type="radio" name="xaxis" value="3" id="# ncRNA" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="3" id="# ncRNA" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td># CRISPR/CAS</td>
                                        <td><input type="radio" name="xaxis" value="3" id="# CRISPR/CAS" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="3" id="# CRISPR/CAS" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td># rRNA</td>
                                        <td><input type="radio" name="xaxis" value="3" id="# rRNA" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="3" id="# rRNA" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td># tRNA</td>
                                        <td><input type="radio" name="xaxis" value="3" id="# tRNA" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="3" id="# tRNA" onclick=updateGraph()></td>
                                    </tr>
                                </table>
                            </form>
                        </div>
                    </div>
                </#if>


                <#if steps.failed?has_content >
                    <div class="row voffset" id="warnings">
                        <div class="col-md-4">
                            <div class="panel panel-danger">
                                <div class="panel-heading collapsible">
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableFailed" href="#">${steps.failed?size} Failed Annotation<#if (steps.failed?size>1)>s</#if></a></h3>
                                </div>
                                <div id="stepTableFailed" class="panel-body panel-collapse collapse in">
                                    <table class="table table-hover table-condensed">
                                        <thead>
                                            <tr>
                                                <th><span class="glyphicon glyphicon-barcode"></span></th>
                                                <th>Genome</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                        <#list steps.failed as step>
                                            <tr>
                                                <td>${step.genome.id}</td>
                                                <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                            </tr>
                                        </#list>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </#if>
                <#if steps.skipped?has_content>
                    <div class="row voffset" id="warnings">
                        <div class="col-md-4">
                            <div class="panel panel-warning">
                                <div class="panel-heading collapsible">
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableSkipped" href="#">${steps.skipped?size} Skipped Annotation<#if (steps.skipped?size>1)>s</#if></a></h3>
                                </div>
                                <div id="stepTableSkipped" class="panel-body panel-collapse collapse in">
                                    <table class="table table-hover table-condensed">
                                        <thead>
                                            <tr>
                                                <th><span class="glyphicon glyphicon-barcode"></span></th>
                                                <th>Genome</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <#list steps.skipped as step>
                                            <tr>
                                                <td>${step.genome.id}</td>
                                                <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                            </tr>
                                            </#list>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </#if>

                <#if steps.finished?has_content>
                    <div class="row voffset" id="stats">
                        <div class="col-md-12">
                            <table id="annotations" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th>Genome</th>
                                        <th class="text-center"># Genes</th>
                                        <th class="text-center"># CDS</th>
                                        <th class="text-center"># Hyp. Proteins</th>
                                        <th class="text-center"># ncRNA</th>
                                        <th class="text-center"># CRISPR/CAS</th>
                                        <th class="text-center"># rRNA</th>
                                        <th class="text-center"># tRNA</th>
                                        <th class="text-center">Downloads</th>
                                        <th class="text-center">Details</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <#list steps.finished as step>
                                    <tr>
                                        <td>${step.genome.id}</td>
                                        <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                        <td class="text-center" gradient="1">${step.noGenes}</td>
                                        <td class="text-center" gradient="3">${step.noCds}</td>
                                        <td class="text-center" gradient="4">${step.noHypProt}</td>
                                        <td class="text-center" gradient="5">${step.noNcRna}</td>
                                        <td class="text-center" gradient="6">${step.noCRISPR}</td>
                                        <td class="text-center" gradient="7">${step.noRRna}</td>
                                        <td class="text-center" gradient="8">${step.noTRna}</td>
                                        <td class="text-center">
                                            <a href="./annotations/${step.genomeName}/${step.genomeName}.gbk">gbk</a>
                                            <a href="./annotations/${step.genomeName}/${step.genomeName}.gff">gff</a>
                                        </td>
                                        <td class="text-center"><a href="./annotations/${step.genomeName}.html"><span class="glyphicon glyphicon-search"></span></a></td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </#if>

                    <#-- content end -->

                </div>
            </div>
        </div>
        <#include "commons/footer.ftl">
    </body>
</html>
