<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta.ftl">

	<link href="css/datatables.min.css" rel="stylesheet">
        <script src="js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {
                $('#assemblies').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
                    columnDefs: [
                        { orderable: false, targets: [7,8] }
                    ],
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'assemblies',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3, 4, 5, 6 ]
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
                        <li class="active">Assemblies</li>
                        <!-- trigger help-modal -->
                        <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal"></i>
                    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                      <div class="modal-dialog" role="document">
                        <div class="modal-content">
                          <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h2 class="modal-title" id="myModalLabel">ASAP assembly overview</h2>
                          </div>
                          <div class="modal-body">
                            <p>
                              The reads that pass the quality control are assembled. For long read assemblies the tool <code>HGap4</code> is used. Assemblies of hybrid
                              and short reads are performed with the tool <code>SPAdes</code>. This page provides an overview
                              on assembly key data of all genomes in this analysis.</p>

                            <h3 id="interactivedotplot">Interactive dotplot</h3>

                            <p>Via the radio buttons on the right <code>key data</code> for X and Y axis can be selected. Mouse
                              over a dot of interest to display the according <code>genome name</code> as well as horizontal
                              and vertical value extensions. <code>Zooming</code> can be applied via marking the area of
                              interest with left mouse button down. To reset the view right click.</p>

                            <h3 id="interactivedatatable">Interactive data table</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header.
                              Use the <code>Search</code> function (top right of the table) to display only genomes that
                              contain the search term in any of their table fields. The <code>number of entries</code> displayed
                              per page can be chosen on the top left of the table. <code>Blue horizontal bar plots</code>                              are displayed in most columns containing numeric values. Their data field filling ratio corresponds
                              to the ratio of field value to column maximum. Mouse over on underlined table headers to display
                              further information on it.</p>

                            <h3 id="downloads">Downloads</h3>

                            <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv
                              button (search and sorting are contained in the downloaded file). To download the <code>fasta</code>                              file of a particular genome assembly click on fasta in the data table.</p>

                            <h3 id="links">Links</h3>

                            <ul>
                              <li><code>Details</code> on the assembly of a particular genome can be accessed via click on the
                                magnifying glass in the overview table.</li>

                              <li><a href="https://github.com/PacificBiosciences/Bioinformatics-Training/wiki/HGAP#overview">HGap</a>:
                                Chin, Chen-Shan, et al. "Nonhybrid, finished microbial genome assemblies from long-read SMRT
                                sequencing data." Nature methods 10.6 (2013): 563-569. <a href="https://www.ncbi.nlm.nih.gov/pubmed/23644548">PubMed</a>.</li>

                              <li><a href="http://cab.spbu.ru/software/spades/">SPAdes</a>: Bankevich A., Nurk S., Antipov D.,
                                Gurevich A., Dvorkin M., Kulikov A. S., Lesin V., Nikolenko S., Pham S., Prjibelski A., Pyshkin
                                A., Sirotkin A., Vyahhi N., Tesler G., Alekseyev M. A., Pevzner P. A. SPAdes: A New Genome
                                Assembly Algorithm and Its Applications to Single-Cell Sequencing. Journal of Computational
                                Biology, 2012. <a href="https://www.ncbi.nlm.nih.gov/pubmed/22506599">PubMed</a>.</li>
                            </ul>

                            <h3 id="glossary">Glossary</h3>

                            <ul>
                              <li><strong># Contigs</strong>: Number of contigs (set of overlapping DNA segments).</li>

                              <li><strong>GC</strong>: GC content in percent.</li>

                              <li><strong>Genome</strong>: Name of the processed genome.</li>

                              <li><strong>Genome size</strong>: Genome size in 1000 bases [kb].</li>

                              <li><strong>Mean contig lengths</strong>: Mean contig lengths of this particular genome.</li>

                              <li><strong>Median contig lengths</strong>: Median contig lengths of this particular genome.</li>

                              <li><strong>N50</strong>: Given ordered contigs from longest to smallest, length of the contig
                                at 50% of the genome length.</li>

                              <li><strong>N50 coverage</strong>: Length weighted mean coverage of sequences with N50 length or
                                longer.
                              </li>

                              <li><strong>N90</strong>: Given ordered contigs from longest to smallest, length of the contig
                                at 90% of the genome length.</li>

                              <li><strong>N90 coverage</strong>: Length weighted mean coverage with sequenced reads of N90 contigs.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                    <#-- content start -->

                <#if (steps.finished?size > 2)>
                    <div class="row" id="charts">
                        <div class="col-md-10">

                            <div id="chartAssemblies"></div>

                            <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
                            <script type="text/javascript">

                                // get values from Groovy
                                const genomes = [ <#list steps.finished as step>'${project.genus[0]}. ${step.genome.species} ${step.genome.strain}',</#list> ];
                                const noContigs = [ <#list chartData.overview.noContigs as val>${val?c},</#list> ];
                                const meanCLengths = [ <#list chartData.overview.meanCLengths as val>${val?c},</#list> ];
                                const medianCLengths = [ <#list chartData.overview.medianCLengths as val>${val?c},</#list> ];
                                const genomeSizes = [ <#list chartData.overview.genomeSizes as val>${val?c},</#list> ];
                                const gcs = [ <#list chartData.overview.gcs as val>${val?c},</#list> ];
                                const n50s = [ <#list chartData.overview.n50s as val>${val?c},</#list> ];
                                const n90s = [ <#list chartData.overview.n90s as val>${val?c},</#list> ];
                                const n50Covs = [ <#list chartData.overview.n50Covs as val>${val?c},</#list> ];
                                const n90Covs = [ <#list chartData.overview.n90Covs as val>${val?c},</#list> ];

                                // load google charts
                                google.charts.load( 'current', { 'packages': ['corechart'] } );

                                // preselect the first 2 choices
                                createPlot( "Genome Size", "# Contigs", genomeSizes, noContigs );

                                function updateGraph() {
                                    // getting reference to radio box array
                                    const xboxes = document.forms['menu'].elements[ 'xaxis' ];
                                    const yboxes = document.forms['menu'].elements[ 'yaxis' ];
                                    const ids = [];
                                    const valueArrays = [];
                                    // getting IDs of checked boxes
                                    for( let i=0; i<xboxes.length; i++ ) {
                                        if (xboxes[i].checked){ids.push(xboxes[i].id)}
                                    }
                                    for( let i=0; i<yboxes.length; i++ ) {
                                        if (yboxes[i].checked){ids.push(yboxes[i].id)}
                                    }

                                    // getting arrays of json values for according IDs
                                    for( let i=0; i<2; i++ ){
                                        if (ids[i] == "# Contigs" ) valueArrays.push( noContigs )
                                        else if (ids[i] == "Mean Contig Length" ) valueArrays.push( meanCLengths )
                                        else if (ids[i] == "Median Contig Length" ) valueArrays.push( medianCLengths )
                                        else if (ids[i] == "Genome Size" ) valueArrays.push( genomeSizes )
                                        else if (ids[i] == "GC" ) valueArrays.push( gcs )
                                        else if (ids[i] == "N50" ) valueArrays.push( n50s )
                                        else if (ids[i] == "N90" ) valueArrays.push( n90s )
                                        else if (ids[i] == "N50 Coverage" ) valueArrays.push( n50Covs )
                                        else if (ids[i] == "N90 Coverage" ) valueArrays.push( n90Covs )
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

                                        const chart = new google.visualization.ScatterChart( document.getElementById( 'chartAssemblies' ) );
                                            chart.draw( data, options );

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
                                        <td># Contigs</td>
                                        <td><input type="radio" name="xaxis" value="1" id="# Contigs" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="1" id="# Contigs" checked="true" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td>Mean Contig Lengths</td>
                                        <td><input type="radio" name="xaxis" value="5" id="Mean Contig Length" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="5" id="Mean Contig Length" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td>Median Contig Lengths</td>
                                        <td><input type="radio" name="xaxis" value="5" id="Median Contig Length" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="5" id="Median Contig Length" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td>Genome Size</td>
                                        <td><input type="radio" name="xaxis" value="4" id="Genome Size" checked="true" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="4" id="Genome Size" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td>GC</td>
                                        <td><input type="radio" name="xaxis" value="3" id="GC" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="3" id="GC" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td>N50</td>
                                        <td><input type="radio" name="xaxis" value="0" id="N50" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="0" id="N50" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td>N90</td>
                                        <td><input type="radio" name="xaxis" value="2" id="n90" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="2" id="n90" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td>N50 Coverage</td>
                                        <td><input type="radio" name="xaxis" value="0" id="N50 Coverage" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="0" id="N50 Coverage" onclick=updateGraph()></td>
                                    </tr>
                                    <tr>
                                        <td>N90 Coverage</td>
                                        <td><input type="radio" name="xaxis" value="2" id="N90 Coverage" onclick=updateGraph()></td>
                                        <td><input type="radio" name="yaxis" value="2" id="N90 Coverage" onclick=updateGraph()></td>
                                    </tr>
                                </table>
                            </form>
                        </div>
                    </div>
                </#if>


                    <div class="row voffset" id="warnings">
                <#if steps.failed?has_content >
                        <div class="col-md-4">
                            <div class="panel panel-danger">
                                <div class="panel-heading collapsible">
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableFailed" href="#">${steps.failed?size} Failed <#if (steps.skipped?size==1)>Assembly<#else>Assemblies</#if></a></h3>
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
                </#if>
                <#if steps.skipped?has_content>
                        <div class="col-md-4">
                            <div class="panel panel-warning">
                                <div class="panel-heading collapsible">
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableSkipped" href="#">${steps.skipped?size} Skipped <#if (steps.skipped?size==1)>Assembly<#else>Assemblies</#if></a></h3>
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
                </#if>
                    </div>
                <#if steps.finished?has_content>
                    <div class="row voffset" id="stats">
                        <div class="col-md-12">
                            <table id="assemblies" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th>Genome</th>
                                        <th class="text-center"># Contigs</th>
                                        <th class="text-center"><abbr title="length weighted mean coverage of N50 contigs">N50 Cov</abbr></th>
                                        <th class="text-center">N50</th>
                                        <th class="text-center">Genome Size</th>
                                        <th class="text-center"><abbr title="GC content [%]">GC</abbr></th>
                                        <th class="text-center">Downloads</th>
                                        <th class="text-center">Details</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#list steps.finished as step>
                                    <tr>
                                        <td>${step.genome.id}</td>
                                        <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                        <td class="text-center" gradient="1">${step.noContigs}</td>
                                        <td class="text-center" gradient="2">${step.n50Coverage?round}</td>
                                        <td class="text-center" gradient="3">${step.n50}</td>
                                        <td class="text-center" gradient="4">${step.length}</td>
                                        <td class="text-center">${step.gc?round}</td>
                                        <td class="text-center"><a href="./assemblies/${step.genomeName}/${step.genomeName}.fasta">fasta</a></td>
                                        <td class="text-center"><a href="./assemblies/${step.genomeName}.html"><span class="glyphicon glyphicon-search"></span></a></td>
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
