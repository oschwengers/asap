<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta_sub.ftl">

	<link href="../css/datatables.min.css" rel="stylesheet">
        <script src="../js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {
                $('#contigTable').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'assembly-${project.genus[0]}_${genome.species}_${genome.strain}',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3, 4 ]
                            }
                        }
                    ],
                    language: {
                        decimal: ",",
                    }
                } );
            } );
        </script>

        <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
        <script type="text/javascript">
            google.charts.load( "current", {packages: ["corechart"]} );
            google.charts.setOnLoadCallback( drawChart );
            function drawChart() {
                // chart data
                const chartDataContigLength = google.visualization.arrayToDataTable( [
                    ['Name', 'Value'],
                    <#list chartData.contigLength as c> ['${c.name}', ${c.length?c}],</#list>
                ]);
                const chartDataContigCoverage = google.visualization.arrayToDataTable( [
                    ['Name', 'Value'],
                    <#list chartData.contigCoverage as c> ['${c.name}', ${c.coverage?round}],</#list>
                ]);
                const chartDataContigGC = google.visualization.arrayToDataTable( [
                    ['Name', 'Value'],
                    <#list chartData.contigGC as c> ['${c.name}', ${c.gc?round}],</#list>
                ]);

                const options = {
                    title: '',
                    width: 400,
                    height: 300,
                    theme: {
                        chartArea: {left: '10%', top: '10%', width: '92%', height: '80%'},
                        legend: {position: 'none'},
                        titlePosition: 'out',
			axisTitlesPosition: 'out',
                        hAxis: {
                            textPosition: 'out',
                            slantedText: false,
                            maxAlternation: 1,
                            maxTextLines: 1,
                            minTextSpacing: 14,
                        },
                        vAxis: {
                            textPosition: 'out',
                        }
                    },
                    colors: ['#428BCA'],
                    fontSize: 14,
                    bar: {
                        gap: 0
                    },
                };

                options.title = 'Contig Lengths';
		options.theme.hAxis.format = 'short'
                options.histogram = {
                    hideBucketItems: false,
                    bucketSize: 10000
                };
                const chartContigLength = new google.visualization.Histogram(document.getElementById( 'chartContigLength' ) );
                chartContigLength.draw( chartDataContigLength, options );

                options.title = 'Contig Coverage';
                options.histogram = {
                    hideBucketItems: false,
                    bucketSize: 10,
                    maxNumBuckets: 100,
                    minValue: 0
                };
                const chartContigCoverage = new google.visualization.Histogram(document.getElementById( 'chartContigCoverage' ) );
                chartContigCoverage.draw( chartDataContigCoverage, options );

                options.title = 'Contig GC Contents';
		options.hAxis = {ticks: [ 20, 40, 60, 80 ] };
                options.histogram = {
                    hideBucketItems: false,
                    bucketSize: 1,
                    minValue: 15,
                    maxValue: 85
                };
                const chartContigGCContent = new google.visualization.Histogram(document.getElementById( 'chartContigGC' ) );
                chartContigGCContent.draw( chartDataContigGC, options );
            }
            </script>

    </head>
    <body>
        <#include "commons/header_sub.ftl">
        <div class="container-fluid">
            <div class="row">
                <#include "commons/menu_sub.ftl">

                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

                    <ol class="breadcrumb">
                        <li><a href="../index.html">Dashboard</a></li>
                        <li><a href="../assemblies.html">Assemblies</a></li>
                        <li class="active">${project.genus[0]}. ${genome.species} ${genome.strain}</li>
                        <!-- trigger help-modal -->
                        <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal"></i>
                    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                      <div class="modal-dialog" role="document">
                        <div class="modal-content">
                          <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h2 class="modal-title" id="myModalLabel">ASA&#179;P assembly detail</h2>
                          </div>
                          <div class="modal-body">
                            <h3>Histograms of contig specifications</h3>

                            <h5>Contig lengths</h5>

                            <p>Histogram of <code>contig length</code> in kb. Via mouse over the number of contigs in each bin
                              is displayed.</p>

                            <h5>Contig coverage</h5>

                            <p>Histogram of the <code>average read coverage</code> per contig. Via mouse over the average coverage
                              of each bin is displayed.</p>

                            <h5>Contig GC contents</h5>

                            <p>Stacked histogram of <code>GC contents</code> per contig. Via mouse over the GC content of each
                              individual contig is displayed.</p>

                            <h3>Basic assembly statistics</h3>

                            <p>Provides information on the assembly in general and on the contig length.</p>

                            <h3>Interactive data table contigs</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header.
                              Use the <code>Search</code> function (top right of the table) to display only genomes that
                              contain the search term in any of their table fields. The <code>number of entries</code> displayed
                              per page can be chosen on the top left of the table. Mose over on underlined table headers
                              to display further information on it.</p>

                            <h3>Downloads</h3>

                            <p>The contigs and scaffolds used in this assembly as well as the ones discarded (not used for assembly)
                              can be downloaded as <code>fasta</code> on the right below the histograms. The table can be
                              saved as a comma separated value (<code>csv</code>) file via a click on the csv button (search
                              and sorting are contained in the downloaded file).</p>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong>Contigs</strong>: Set of overlapping DNA segments (reads).</li>

                              <li><strong>Coverage</strong>: Mean read coverage of this contig.</li>

                              <li><strong># Gaps</strong>: Amount of space (bp) between assembled nucleotides in this contig.</li>

                              <li><strong>GC</strong>: GC content in percent.</li>

                              <li><strong>Length</strong>: Length of the contig in base pairs.</li>

                              <li><strong>N50 length</strong>: Given ordered contigs from longest to smallest, length of the
                                contig at 50% of the genome length.</li>

                              <li><strong>N90 length</strong>: Given ordered contigs from longest to smallest, length of the
                                contig at 90% of the genome length.</li>

                              <li><strong>Name</strong>: Name of this contig.</li>

                              <li><strong>Scaffolds</strong>: Consists of aligned contigs with the sequence 'NNNNNNNNNNCTAGCTAGCTAGCNNNNNNNNNN'
                                in between them.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                    <#-- content start -->

                    <div class="row">
                        <div class="col-lg-4 col-md-4" id="chartContigLength"></div>
                        <div class="col-lg-4 col-md-4" id="chartContigCoverage"></div>
                        <div class="col-lg-4 col-md-4" id="chartContigGC"></div>
                    </div>

                    <!-- Assembly overview -->
                    <div class="row voffset">
                        <div class="col-sm-3 col-md-3 col-md-offset-1">
                            <table class="table table-hover table-condensed">
                                <caption>Genome Assembly</caption>
                                <tbody>
                                    <tr><td>Contigs [#]</td><td class="text-center">${noContigs}</td></tr>
                                    <tr><td>Genome Size [Mb]</td><td class="text-center">${length}</td></tr>
                                    <tr><td>N50 [kb]</td><td class="text-center">${n50}</td></tr>
                                    <tr><td>N90 [kb]</td><td class="text-center">${n90}</td></tr>
                                </tbody>
                            </table>
                        </div>

                        <!-- Contig lengths -->
                        <div class="col-sm-3 col-md-3 col-md-offset-1">
                            <table class="table table-hover table-condensed">
                                <caption>Contig Lengths</caption>
                                <tbody>
                                    <tr><td>Min [bp]</td><td class="text-center">${statsLength.min}</td></tr>
                                    <tr><td>Max [kb]</td><td class="text-center">${statsLength.max}</td></tr>
                                    <tr><td>Mean [kb]</td><td class="text-center">${statsLength.mean?round}</td></tr>
                                    <tr><td>Median [kb]</td><td class="text-center">${statsLength.median}</td></tr>
                                </tbody>
                            </table>
                        </div>

                        <!-- Downloads -->
                        <div id="downloadsTypes" class="col-sm-2 col-md-2 col-md-offset-1">
                            <table class="table table-hover table-condensed">
                                <caption>Downloads</caption>
                                <tbody>
                                    <tr><td>Contigs /<br>Scaffolds</td><td class="text-center"><a href="./${genomeName}/${genomeName}.fasta">fasta</a></td></tr>
                                    <tr><td>Discarded Contigs /<br>Scaffolds</td><td class="text-center"><a href="./${genomeName}/${genomeName}-discarded.fasta">fasta</a></td></tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="row voffset">
                        <!-- Contig Table -->
                        <div class="col-sm-10 col-md-10">
                            <h2><small>Contigs</small></h2>
                            <table id="contigTable" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th class="text-center">Length</th>
                                        <th class="text-center">Coverage</th>
                                        <th class="text-center"><abbr title="GC content [%]">GC</abbr></th>
                                        <th class="text-center"># Gaps</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <#list contigs as c>
                                    <tr>
                                        <td>${c.name}</td>
                                        <td class="text-center">${c.length}</td>
                                        <td class="text-center">${c.coverage?round}</td>
                                        <td class="text-center">${c.gc?round}</td>
                                        <td class="text-center">${c.noNs}</td>
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
    </body>
</html>
