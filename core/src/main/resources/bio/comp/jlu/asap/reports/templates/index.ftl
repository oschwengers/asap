<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta.ftl">

        <link href="css/datatables.min.css" rel="stylesheet">
        <script src="js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {

                $('#genomeStats').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
		    dom:      "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'project-summary',
                        }
                    ],
                    language: {
                        decimal: ",",
                    }
                } );

                // tooltip
                Array.from(d3.selectAll('path')[0]).forEach( e => {

                    $(e).tooltip({
                        title: e.__data__.key,
                        container: 'body'
                    });

                    let xPosition, yPosition;

                    let drawTip = function(event){
                        const tip = $('.tooltip');
                        xPosition = event.pageX - tip.width() / 2;
                        yPosition = event.pageY - tip.height() / 2 - 24;
                        $('.tooltip').css({
                            left: xPosition,
                            top: yPosition,
                            opacity: 1,
                        })
                    }

                    $(e).on( "mouseenter mousemove", (event) => drawTip(event) );
                    $(e).mouseleave( (event) => $('.tooltip').css( "opacity", 0 ) );

                }); // tooltip

            } );
        </script>

        <link href="https://cdn.rawgit.com/novus/nvd3/v1.8.4/build/nv.d3.min.css" rel="stylesheet">
        <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.0/d3.min.js"></script>
	<script src="https://cdn.rawgit.com/novus/nvd3/v1.8.4/build/nv.d3.min.js"></script>

        <script> // Synchronization between parallel coordindates plot and data table
            $(document).ready(function () {
                // lines
                d3.selectAll('path').on('mouseover', function () {
                    d3.select(this).data(this, obj => {
                        Array.from(document.getElementsByTagName('tr')).forEach(other => {
                            if (other.cells[1].innerText == obj.key) {
                                other.style.backgroundColor = '#f2dede';
                            }
                        });
                    });
                    d3.select(this).style("stroke-width", 3);
                });
                d3.selectAll('path').on('mouseout', function () {
                    Array.from(document.getElementsByTagName('tr')).forEach(other => {
                        other.style.backgroundColor = 'transparent';
                    });
                    d3.select(this).style("stroke-width", 1);
                });
            });

            // rows
            function onRow(row) {  // mouseover
                const rowID = row.cells[1].innerHTML;
                Array.from( document.getElementsByTagName('path') ).forEach(other => {
                    d3.select(other).data(this, d => {
                        if (d.key == rowID)
                            d3.select(other).style("stroke-width", 3);
                    });
                });
                row.style.backgroundColor = '#f2dede';
            }
            function offRow(row) {  // mouseout
                const rowID = row.cells[1].innerHTML;
                Array.from( document.getElementsByTagName('path') ).forEach(other => {
                    d3.select(other).data(this, d => {
                        if (d.key == rowID)
                            d3.select(other).style("stroke-width", 1);
                    });
                });
                row.style.backgroundColor = 'transparent';
            }
        </script>

	<style>
            #chartParallelCoordinates, svg {
                margin: 0px;
                padding: 0px;
                height: 300px;
                width: 100%;
                box-sizing: border-box;
            }
	</style>

        <script src="js/gradient.js" defer></script>

    </head>
    <body>
        <#include "commons/header.ftl">
        <div class="container-fluid">
            <div class="row">
                <#include "commons/menu.ftl">

                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

                    <#-- content start -->

                    <div class="row">

                        <!-- Project -->
                        <div class="col-md-5">
                            <div class="panel">
                                <div class="panel-body">
                                    <p><span class="glyphicon glyphicon-home" data-toggle="tooltip" data-original-title="Project name"></span>&nbsp;&nbsp;${project.name}</p>
                                    <p><span class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-original-title="Project description"></span>&nbsp;&nbsp;${project.description}</p>
                                    <p><span class="glyphicon glyphicon-wrench" data-toggle="tooltip" data-original-title="ASA&#179;P version"></span>&nbsp;&nbsp;${project.version}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="glyphicon glyphicon-tree-deciduous" data-toggle="tooltip" data-original-title="Genus of isolates within this project"></span>&nbsp;&nbsp;${project.genus}</p>
                                </div>
                            </div>
                        </div>

                        <!-- User -->
                        <div class="col-md-4">
                            <div class="panel">
                                <div class="panel-body">
                                    <p><span class="glyphicon glyphicon-user" data-toggle="tooltip" data-original-title="Person responsible"></span>&nbsp;&nbsp;${user.name}</p>
                                    <p><span class="glyphicon glyphicon-user" data-toggle="tooltip" data-original-title="Person responsible"></span>&nbsp;&nbsp;${user.surname}</p>
                                    <p><span class="glyphicon glyphicon-envelope" data-toggle="tooltip" data-original-title="Person responsible's email"></span>&nbsp;&nbsp;${user.email}</p>
                                </div>
                            </div>
                        </div>

                        <#-- runtimes -->
                        <div class="col-md-3">
                            <div class="panel">
                                <div class="panel-body">
                                    <p><span class="glyphicon glyphicon-play" data-toggle="tooltip" data-original-title="Start date"></span>&nbsp;&nbsp;${runtime.start}</p>
                                    <p><span class="glyphicon glyphicon-stop" data-toggle="tooltip" data-original-title="Stop date"></span>&nbsp;&nbsp;${runtime.end}</p>
                                    <p><span class="glyphicon glyphicon-refresh" data-toggle="tooltip" data-original-title="Runtime"></span>&nbsp;&nbsp;${runtime.time}</p>
                                </div>
                            </div>
                        </div>

                    </div>

                    <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal" style="position:absolute; right:6px; top:1rem;"></i>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                      <div class="modal-dialog" role="document">
                        <div class="modal-content">
                          <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h2 class="modal-title" id="myModalLabel">ASA&#179;P analysis overview</h2>
                          </div>
                          <div class="modal-body">
                            <h3>General information</h3>

                            <p>On the top of this page project information on <code>Biological background</code>, <code>User account</code>
                                and <code>Runtime statistics</code> are displayed.
                                A general multi analyses comparison of the analysed genomes is visualized as <code>interactive infographics</code>
                                and accessible in the <code>interactive data table</code>.</p>

                            <h3>Interactive infographics</h3>

                            <p>This dynamic visualization provides an overview on <code>key data</code> of the analysed genomes. The vertical black lines
                              display the result range of a particular analysis. The range of each analysis can be limited
                              via drag and drop on the black line. The limited range is visualized as a box. Genome graphs
                              not passing through all set ranges are greyed out. The box itself can also be dragged in position
                              or adjusted in its range via dragging either top or bottom. To remove the range limitation
                              click on the particular black line. Mouse over a genome graph to display its name.</p>

                            <h3>Interactive data table</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header.
                              Use the <code>Search</code> function (top right of the table) to display only genomes that
                              contain the search term in any of their table fields. The <code>number of entries</code> displayed
                              per page can be chosen on the top left of the table. <code>Blue horizontal bar plots</code>                              are displayed in columns containing numeric values. They visualize the relative relation of
                              this value compared to the according values of the other genomes. The <code>yellow and red colored bar plots</code>                              indicates outliners based on Z-score.</p>

                            <h3>Downloads</h3>

                            <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv
                              button (search and sorting are contained in the downloaded file).</p>

                            <h3>Links</h3>

                            <ul>
                              <li><code># ABR</code> values of the data table redirect on click to their antibiotic resistance
                                analysis.
                              </li>

                              <li><code># Contigs</code> values of the data table redirect on click to their assembly.</li>

                              <li><code># Genes</code> values of the data table redirect on click to their annotation.</li>

                              <li><code>GC</code> values of the data table redirect on click to their assembly.</li>

                              <li><code>Genome Size</code> values of the data table redirect on click to their assembly.</li>

                              <li><code># HI SNPs</code> values of the data table redirect on click to their SNP analysis.</li>

                              <li><code>Index page</code> can be accessed from any report page via click on the home button on
                                the top left.</li>

                              <li><code>Particular analysis results</code> can be accessed via the left handed menu.</li>

                              <li><code>Tax Class</code> values of the data table redirect on click to their Taxonomy (kmer based
                                taxonomic classification).</li>
                            </ul>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong># ABRs</strong>: Number of antibiotic resistances found.</li>

                              <li><strong># Contigs</strong>: Number of contigs (set of overlapping DNA segments).</li>

                              <li><strong>GC</strong>: GC content [%]</li>

                              <li><strong># Genes</strong>: Number of genes found.</li>

                              <li><strong>Genome</strong>: Name of the processed genome.</li>

                              <li><strong>Genome Size</strong>: Genome size in 1000 bases [kb].</li>

                              <li><strong># HI SNPs</strong>: Number of hi impact nucleotide polymorphisms found.</li>

                              <li><strong>Input Type</strong>: Format of the provided sequence data.</li>

                              <li><strong># Plasmids</strong>: Number of plasmids found.</li>

                              <li><strong>Tax Class</strong>: Kmer based taxonomic classification.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>


                    <div class="row">
                        <div id="chartParallelCoordinates" class="col-md-10 push-md-1"><svg></svg></div>
                        <script>
                            nv.addGraph(function() {
                                var chart = nv.models.parallelCoordinates()
                                    .dimensionNames( ["Genome Size", "Contigs", "GC", "Genes", "ABRs", "VFs", "HI SNPs"] )
                                    .dimensionFormats( [ d3.format("d"), d3.format("d"), d3.format("%"), d3.format("d"), d3.format("d"), d3.format("d") ] )
                                    .lineTension( 0.9 );
                                d3.select( '#chartParallelCoordinates svg' )
                                    .datum( [
<#list genomes as genome>
{ "name": "${genome.sampleName}", "Genome Size": ${genome.genomeSize?c}, "Contigs": ${genome.noContigs?c}, "GC": ${(genome.gc/100)?c}, "Genes": ${genome.noGenes?c}, "ABRs": ${genome.noABRs?c}, "VFs": ${genome.noVFs?c}, "HI SNPs": ${genome.noHISNPs?c} },
</#list>
                                        ]
                                    )
                                    .call( chart );
                                nv.utils.windowResize(chart.update);
                                return chart;
                            } );
                        </script>
                    </div>

                    <div class="row voffset">
                        <div class="col-md-12">
                            <table id="genomeStats" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th class="text-center">Genome</th>
                                        <th class="text-center"><abbr title="Kmer based taxonomic classification">Tax Class</abbr></th>
                                        <th class="text-center"><abbr title="[kb]">Genome Size</abbr></th>
                                        <th class="text-center"># Contigs</th>
                                        <th class="text-center"><abbr title="[%]">GC</abbr></th>
                                        <th class="text-center"># Genes</th>
                                        <th class="text-center"><abbr title="antibiotic resistances"># ABR</abbr></th>
                                        <th class="text-center"><abbr title="virulence factors"># VF</abbr></th>
                                        <th class="text-center"># <abbr title="hi impact single nucleotide polymorpisms">HI SNPs</<abbr></th>
                                    </tr>
                                </thead>
                                <tbody>
<#list genomes as genome>
<tr onmouseover="onRow(this)" onmouseout="offRow(this)">
<td class="text-center">${genome.id}</td>
<td class="text-center">${genome.sampleName}</td>
<td class="text-center"><a href="./taxonomy/${genome.genomeName}.html">${genome.kmer}</a></td>
<td class="text-center<#if (genome.zScores.genomeSize>2.5)> bg-danger<#elseif (genome.zScores.genomeSize>1.5)> bg-warning</#if>" gradient="1"><a href="./assemblies/${genome.genomeName}.html">${genome.genomeSize}</a></td>
<td class="text-center<#if (genome.zScores.noContigs>2.5)> bg-danger<#elseif (genome.zScores.noContigs>1.5)> bg-warning</#if>" gradient="2"><a href="./assemblies/${genome.genomeName}.html">${genome.noContigs}</a></td>
<td class="text-center<#if (genome.zScores.gc>2.5)> bg-danger<#elseif (genome.zScores.gc>1.5)> bg-warning</#if>"><a href="./assemblies/${genome.genomeName}.html">${genome.gc?round}</a></td>
<td class="text-center<#if (genome.zScores.noGenes>2.5)> bg-danger<#elseif (genome.zScores.noGenes>1.5)> bg-warning</#if>" gradient="3"><a href="./annotations/${genome.genomeName}.html">${genome.noGenes}</a></td>
<td class="text-center<#if (genome.zScores.noABRs>2.5)> bg-danger<#elseif (genome.zScores.noABRs>1.5)> bg-warning</#if>" gradient="4"><a href="./abr/${genome.genomeName}.html">${genome.noABRs}</a></td>
<td class="text-center<#if (genome.zScores.noVFs>2.5)> bg-danger<#elseif (genome.zScores.noVFs>1.5)> bg-warning</#if>" gradient="5"><a href="./vf/${genome.genomeName}.html">${genome.noVFs}</a></td>
<td class="text-center<#if (genome.zScores.noHISNPs>2.5)> bg-danger<#elseif (genome.zScores.noHISNPs>1.5)> bg-warning</#if>" gradient="6"><a href="./snps/${genome.genomeName}.html">${genome.noHISNPs}</a></td>
</tr>
</#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="pull-right"><div class="bg-warning">Z Score > 1,5</div><div class="bg-danger">Z Score > 2,5</div></div>

                    <#-- content end -->

                </div>
            </div>
        </div>
    </body>
</html>
