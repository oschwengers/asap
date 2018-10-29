<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta_sub.ftl">

        <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.17/d3.min.js"></script>

        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.11/c3.min.css">
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.11/c3.min.js"></script>

	<link href="../css/datatables.min.css" rel="stylesheet">
        <script src="../js/datatables.min.js"></script>

        <script>
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
                            filename: 'accessory-${project.genus[0]}_${genome.species}_${genome.strain}',
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
                            filename: 'singletons-${project.genus[0]}_${genome.species}_${genome.strain}',
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
        </script>

        <script src="../js/gradient.js" defer></script>

    </head>
    <body>
        <#include "commons/header_sub.ftl">
        <div class="container-fluid">
            <div class="row">
                <#include "commons/menu_sub.ftl">

                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

                    <ol class="breadcrumb">
                        <li><a href="../index.html">Dashboard</a></li>
                        <li><a href="../corepan.html">Core / Pan Genome</a></li>
                        <li class="active dropdown"><a class="dropdown-toggle" data-toggle="dropdown" href="#">${project.genus[0]}. ${genome.species} ${genome.strain} <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                                <li><a href="#aPlots">Distribution Plot</a></li>
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
                            <h2 class="modal-title" id="myModalLabel">ASA&#179;P core and pan genome detail</h2>
                          </div>
                          <div class="modal-body">
                            <h3>Interactive donut chart</h3>

                            <p>The percentage distribution of Core, Accessory and Singletons genes is displayed.</p>

                            <h3>Interactive data tables</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header.
                              Use the <code>Search</code> function (top right of the table) to display only genomes that
                              contain the search term in any of their table fields. The <code>number of entries</code> displayed
                              per page can be chosen on the top left of the table.</p>

                            <h5>Accessory Genome</h5>

                            <p>In this table loci of this particular genome that are present in at least one other analysed
                              genome can be compared. <code>Blue horizontal bar plots</code> are displayed in the Abundance
                              column. They visualize the relative relation of this value compared to the according values
                              of the other loci.</p>

                            <h5>Singletons</h5>

                            <p>In this table loci that have only been found in this particular genome can be observed.</p>

                            <h3>Downloads</h3>

                            <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv
                              button (search and sorting are contained in the downloaded file).</p>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong>Abundance</strong>: Number of locus occurrences in this analysis.</li>

                              <li><strong>Accessory</strong>: Number of genes that are contained in at least one other analysed
                                organism (also known as dispensable genome).</li>

                              <li><strong>Core</strong>: Number of genes contained in all analysed genomes.</li>

                              <li><strong>Locus</strong>: Defined contiguous nucleotide sequence in the genome. The name can
                                refer to a locus tag, gene name, synthetic cluster or group name.</li>

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
                        <div class="col-md-4 col-md-offset-2">
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
                                    }
                                } );
                            </script>
                        </div>
                    </div>

                    <div class="row voffset" id="aAccessory">
                        <div class="col-md-10 col-md-offset-1">
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
                                        <td class="text-center" gradient="1">${gene.abundance?c}</td>
                                        <td class="text-center">${gene.product}</td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="row voffset" id="aSingletons">
                        <div class="col-md-10 col-md-offset-1">
                            <h2><small>Singletons</small></h2>
                            <table id="singletons" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th class="text-center">Locus</th>
                                        <th class="text-center">Product</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#list singletons as gene>
                                    <tr>
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
    </body>
</html>
