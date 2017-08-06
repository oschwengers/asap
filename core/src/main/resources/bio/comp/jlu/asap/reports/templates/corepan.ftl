<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta.ftl">

        <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.17/d3.min.js"></script>

        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.11/c3.min.css">
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.11/c3.min.js"></script>

	<link href="css/datatables.min.css" rel="stylesheet">
        <script src="js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {
                $('#overview').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
                    columnDefs: [
                        { orderable: false, targets: [4] }
                    ],
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'corepan',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3 ]
                            }
                        }
                    ],
                    language: {
                        decimal: ",",
                    }
                } );
            } );
            $(document).ready(function() {
                $('#core').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'core',
                            exportOptions: {
                                columns: [ 0, 1 ]
                            }
                        }
                    ],
                    language: {
                        decimal: ",",
                    }
                } );
            } );
            $(document).ready(function() {
                $('#accessory').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'accessory',
                            exportOptions: {
                                columns: [ 0, 1, 2 ]
                            }
                        }
                    ],
                    language: {
                        decimal: ",",
                    }
                } );
            } );
            $(document).ready(function() {
                $('#singletons').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'singletons',
                            exportOptions: {
                                columns: [ 0, 1, 2 ]
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
                        <li class="active dropdown"><a class="dropdown-toggle" data-toggle="dropdown" href="#">Core / Pan Genome <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                                <li><a href="#aPlots">Distribution / Development Plots</a></li>
                                <li><a href="#aOverview">Overview Genomes</a></li>
                                <li><a href="#aCore">Core Genome</a></li>
                                <li><a href="#aAccessory">Accessory Genome</a></li>
                                <li><a href="#aSingletons">Singleton Genes</a></li>
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
                            <h2 class="modal-title" id="myModalLabel">ASAP core and pan genome overview</h2>
                          </div>
                          <div class="modal-body">

                            <p>Coding sequences (CDS) of the analysed genomes get clustered and assigned to gene abundance groups via <code>Roary</code>.
                                These groups consist of genes present in all analysed genomes (<code>core</code>), genes present at least in one other
                                analysed genome (<code>accessory</code>) and genes unique to one a single genome (<code>singletons</code>).
                                Internally, <code>Roary</code> uses <code>CD-HIT</code> and <code>BLAST+</code> and is provided with <code>.gff</code> files resulting from prior annotation.</p>
                            

                            <h3 id="interactivedonutchart">Interactive donut chart</h3>

                            <p>The percentage distribution of Core, Accessory and Singleton genes is displayed.</p>

                            <h3 id="genenumbers">Gene Numbers</h3>

                            <p>Provides absolute numbers on Core, Pan, Accessory and Singleton genes.</p>

                            <h3 id="interactivepancoresingletondevelopmentchart">Interactive PAN / Core / Singleton Development chart</h3>

                            <p>Displays changes in number of CDS (loci) in <code>Pan</code>, <code>Core</code> and <code>Singletons</code>                              with increasing number of genomes included in comparison (x-axis). For each comparisons amount
                              the number of genomes is picked randomly ten times and the average values are displayed. <code>Pan</code>                              and <code>Core</code> genome size is referenced by the left y-axis. The number of <code>Singletons</code>                              is referenced by the right y-axis. Highlighting of an individual graph can be done via clicking
                              on the graph or the according legend. Individual values on the graphs can be accessed via mouse
                              over. Individual data points can be highlighted via clicking on them.</p>

                            <h3 id="skippedgenome">Skipped Genome</h3>

                            <p>In case a sequenced genome could not be analysed this frame is displayed and shows the affected
                              genomes.</p>

                            <h3 id="interactivedatatables">Interactive data tables</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header.
                              Use the <code>Search</code> function (top right of the table) to display only genomes that
                              contain the search term in any of their table fields. The
                              <code>number of entries</code> displayed per page can be chosen on the top left of the table.
                              <code>Blue horizontal bar plots</code> are displayed in columns containing numeric values.
                              They visualize the relative relation of this value compared to the according values of the
                              other genomes.</p>

                            <h5 id="overview">Overview</h5>

                            <p>Provides information on the <code># Accessory</code> and <code># Singletons</code> gene loci
                              of each genome.</p>

                            <h5 id="coregenome">Core Genome</h5>

                            <p>Provides information on the <code>Product</code>(function) for each loci of the core genome.</p>

                            <h5 id="accessorygenome">Accessory Genome</h5>

                            <p>Provides information on the <code>Product</code>(function) and the <code>Abundance</code> for
                              each loci of the accessory genome.
                            </p>

                            <h5 id="singletons">Singletons</h5>

                            <p>Provides information on each <code>Locus</code>, its <code>Product</code>(function) and the genome
                              it was found.</p>

                            <h3 id="downloads">Downloads</h3>

                            <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv
                              button (search and sorting are contained in the downloaded file). A <code>fasta</code> file
                              with all core gene sequences and a file with all the pan gene sequences can be downloaded.
                              The matrix maps which gene is present in which sequenced organism (present = 1, absent = 0)
                              can be downloaded as tab separated value 'tsv' file.</p>

                            <h3 id="links">Links</h3>

                            <ul>
                              <li><code>Details</code> on the core and pan genome distribution of a particular genome can be
                                accessed via click on the magnifying glass in the overview table.</li>

                              <li><a href="https://sanger-pathogens.github.io/Roary/">Roary</a>; "Roary: Rapid large-scale prokaryote
                                pan genome analysis", Andrew J. Page, Carla A. Cummins, Martin Hunt, Vanessa K. Wong, Sandra
                                Reuter, Matthew T. G. Holden, Maria Fookes, Daniel Falush, Jacqueline A. Keane, Julian Parkhill,
                                Bioinformatics, (2015). <a href="https://www.ncbi.nlm.nih.gov/pubmed/26198102">PubMed</a>.</li>
                            </ul>

                            <h3 id="glossary">Glossary</h3>

                            <ul>
                              <li><strong>Abundance</strong>: Number of locus occurrence in this analysis.</li>

                              <li><strong>Accessory</strong>: Number of genes that are contained in at least one other analysed
                                organism (also known as dispensable genome).
                              </li>

                              <li><strong>Core</strong>: Number of genes contained in all analysed genomes.</li>

                              <li><strong>Genome</strong>: Name of the processed genome.</li>

                              <li><strong>Locus</strong>: Defined contiguous nucleotide sequence in the genome.</li>

                              <li><strong>Pan</strong>: Total number of individual genes in this analysis.</li>

                              <li><strong>Pan Genome Matrix</strong>: The matrix maps which gene is present in which sequenced
                                organism (present = 1, absent = 0).</li>

                              <li><strong>Product</strong>: Functional information on the associated locus.</li>

                              <li><strong>Singletons</strong>: Number of genes contained only in this genome out of the analysed
                                set.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                    <#-- content start -->

                    <div class="row" id="aPlots">
                        <div class="col-md-4 col-md-offset-1">
                            <div id="casChart"></div>
                            <script>
                                var chart = c3.generate( {
                                    bindto: '#casChart',
                                    data: {
                                        columns: [
                                            [ 'Core', ${chartData.donut.core?c} ],
                                            [ 'Accessory', ${chartData.donut.accessory?c} ],
                                            [ 'Singletons', ${chartData.donut.singletons?c} ]
                                        ],
                                        type : 'donut'
                                    },
                                    donut: {
                                        title: "Composition"
                                    },
                                    color: { pattern: ['#428bca', '#f4b400', '#cd2d50'] }
                                } );
                            </script>
                        </div>

                        <div class="col-md-2 col-md-offset-1">
                            <table class="table table-hover table-condensed">
                                <caption>Gene Numbers</caption>
                                <tbody>
                                    <tr><td># Core</td><td class="text-center">${noCore}</tr>
                                    <tr><td># Pan</td><td class="text-center">${noPan}</tr>
                                    <tr><td># Accessory</td><td class="text-center">${noAccessory}</tr>
                                    <tr><td># Singletons</td><td class="text-center">${noSingletons}</tr>
                                </tbody>
                            </table>
                        </div>

                        <div class="col-md-2 col-md-offset-1">
                            <table class="table table-hover table-condensed">
                                <caption>Downloads</caption>
                                <tbody>
                                    <tr><td>Core Genome</td><td class="text-center"><a href="./corepan/core.fasta">fasta</a></td></tr>
                                    <tr><td>Pan Genome</td><td class="text-center"><a href="./corepan/pan.fasta">fasta</a></td></tr>
                                    <tr><td>Pan Genome Matrix</td><td class="text-center"><a href="./corepan/pan-matrix.tsv">tsv</a></td></tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="row voffset" id="dPlots">
                        <div class="col-md-10 col-md-offset-1">

                            <div id="pcsDevelopment"></div>

                            <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
                            <script type="text/javascript">
                                google.charts.load('current', {'packages':['line']});
                                google.charts.setOnLoadCallback(drawChart);

                                function drawChart() {
                                    const data = new google.visualization.DataTable();
                                    data.addColumn('number', 'Pan Genome Size');
                                    data.addColumn('number', 'Pan');
                                    data.addColumn('number', 'Core');
                                    data.addColumn('number', 'Singletons');

                                    data.addRows( [
                                        ${chartData.line?join(',')}
                                    ] );

                                    const options = {
                                        chart: {
                                            title: 'Pan / Core / Singleton Development',
                                        },
                                        chartArea: { left:100, top:10, width:"90%", height:"80%" },
                                        height: 400,
                                        pointSize: 3,
                                        axisTitlesPosition: 'out',
                                        crosshair: { trigger: 'focus' },
                                        explorer: { keepInBounds: true },
					colors: ['#c7c7c7', '#428bca', '#cd2d50'],
					series: {
                                            0: {axis: 'cp'},
                                            1: {axis: 'cp'},
                                            2: {axis: 's'}
                                        },
                                        axes: {
                                            y:{
                                                'cp': {label: '# Core / Pan'},
                                                's': {label: '# Singletons'}
                                            }
                                        }
                                    };
                                    const chart = new google.charts.Line ( document.getElementById( 'pcsDevelopment' ) );
                                    chart.draw( data, options );
                                }
                            </script>
                        </div>
                    </div>


                <#if steps.skipped?has_content>
                    <div class="row voffset" id="warnings">
                        <div class="col-md-4">
                            <div class="panel panel-warning">
                                <div class="panel-heading collapsible">
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableSkipped" href="#">${steps.skipped?size} Skipped Genome<#if (steps.skipped?size>1)>s</#if></a></h3>
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

                    <div class="row voffset" id="aOverview">
                        <div class="col-md-8 col-md-offset-2">
                            <h2><small>Overview</small></h2>
                            <table id="overview" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th>Genome</th>
                                        <th class="text-center"># Accessory</th>
                                        <th class="text-center"># Singletons</th>
                                        <th class="text-center">Details</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#list steps.finished as step>
                                    <tr>
                                        <td>${step.genome.id}</td>
                                        <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                        <td class="text-center" gradient="1">${step.accessory}</td>
                                        <td class="text-center" gradient="2">${step.singletons}</td>
                                        <td class="text-center"><a href="./corepan/${step.genomeName}.html"><span class="glyphicon glyphicon-search"></span></a></td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="row voffset" id="aCore">
                        <div class="col-md-8 col-md-offset-2">
                            <h2><small>Core Genome</small></h2>
                            <table id="core" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th class="text-center">Locus</th>
                                        <th class="text-center">Product</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#list core as gene>
                                    <tr>
                                        <td class="text-center">${gene.name}</td>
                                        <td class="text-center">${gene.product}</td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="row voffset" id="aAccessory">
                        <div class="col-md-8 col-md-offset-2">
                            <h2><small>Accessory Genome</small></h2>
                            <table id="accessory" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th class="text-center">Locus</th>
                                        <th class="text-center">Abundance</th>
                                        <th class="text-center">Product</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#list accessory as gene>
                                    <tr>
                                        <td class="text-center">${gene.name}</td>
                                        <td class="text-center" gradient="3">${gene.abundance?c}</td>
                                        <td class="text-center">${gene.product}</td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="row voffset" id="aSingletons">
                        <div class="col-md-8 col-md-offset-2">
                            <h2><small>Singletons</small></h2>
                            <table id="singletons" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th class="text-center">Genome</th>
                                        <th class="text-center">Locus</th>
                                        <th class="text-center">Product</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#list singletons as gene>
                                    <tr>
                                        <td class="text-center">${gene.source}</td>
                                        <td class="text-center">${gene.name}</td>
                                        <td class="text-center">${gene.product}</td>
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
        <#include "commons/footer.ftl">
    </body>
</html>
