<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta.ftl">

	<link href="css/datatables.min.css" rel="stylesheet">
        <script src="js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {
                $('.abr-circle').tooltip( { html: true, container: 'body', placement: 'top' } );
                $('.abr-list').popover( { html: true, trigger: "hover", placement: "top"} );
                $('#abr').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
                    columnDefs: [
                        { orderable: false, targets: [6] }
                    ],
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'abr',
                            exportOptions: {
                                columns: [ 0, 1, 4, 5 ]
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

        <!-- ABR profile circles -->
	<script src="https://d3js.org/d3.v4.min.js"></script>
        <script src="js/abrs.js" defer></script>

        <!-- ABR profile tooltips -->
        <style>
            .tooltip-inner {
                white-space:pre;
                max-width:none;
            }
        </style>

    </head>
    <body>
        <#include "commons/header.ftl">
        <div class="container-fluid">
            <div class="row">
                <#include "commons/menu.ftl">

                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

                    <ol class="breadcrumb">
                        <li><a href="index.html">Dashboard</a></li>
                        <li class="active">Antibiotic Resistances</li>
                        <!-- trigger help-modal -->
                        <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal"></i>
                    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                      <div class="modal-dialog" role="document">
                        <div class="modal-content">
                          <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h2 class="modal-title" id="myModalLabel">ASAP antibiotic resistances overview</h2>
                          </div>
                          <div class="modal-body">
                            <p>
                              There are many different molecular mechanisms for ABR posing them as a major bioinformatic challenge. Addressing this issue ASA³P
                              takes advantage of the Comprehensive Antibiotic Resistance Database (<code>CARD</code>) and its
                              corresponding search tool. The database is manually curated and updated on a monthly basis. Additionally,
                              CARD provides its own sophisticated ontology in order to classify detected ABRs. To our best
                              knowledge it’s the only database/tool which can detect, classify and describe several different
                              types of ABR, e.g. gene homology and mutations driven mechanisms.</p>

                            <h3>Interactive data table</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header.
                              Use the <code>Search</code> function (top right of the table) to display only genomes that
                              contain the search term in any of their table fields. The <code>number of entries</code> displayed
                              per page can be chosen on the top left of the table. <code>Blue horizontal bar plots</code>                              are displayed in columns containing numeric values. They visualize the relative relation of
                              this value compared to the according values of the other genomes. The <code>red colored bar plots</code>                              indicates outliners based on Z-score. In the <code>ABR Profile</code> column found antibiotic
                              agent resistances are visualized as colored circles. You can mouse over the circles to display
                              the individual resistances. Mouse over on underlined term to display further information on
                              it.</p>

                            <h3>Downloads</h3>

                            <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv
                              button (search and sorting are contained in the downloaded file).</p>

                            <h3>Links</h3>

                            <ul>
                              <li><code>Details</code> on the resistance of a particular genome can be accessed via click on
                                the magnifying glass in the overview table.</li>

                              <li><a href="https://card.mcmaster.ca/">CARD</a>; Jia et al. 2017. CARD 2017: expansion and model-centric
                                curation of the Comprehensive Antibiotic Resistance Database. Nucleic Acids Research, 45,
                                D566-573.
                                <a href="https://www.ncbi.nlm.nih.gov/pubmed/27789705">PubMed</a>.</li>
                            </ul>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong># ABR Genes</strong>: Number of antibiotic resistance genes found.</li>

                              <li><strong>ABR Profile</strong>: Found antibiotic agent resistances.</li>

                              <li><strong># ABR Target Drugs</strong>: Number of antibiotic agent resistances.</li>

                              <li><strong>Genome</strong>: Name of the processed genome.</li>

                              <li><strong># Potential ABR Genes</strong>: Number of potential antibiotic resistance genes found.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                    <#-- content start -->


                    <div class="row" id="warnings">
                <#if steps.failed?has_content >
                        <div class="col-md-4">
                            <div class="panel panel-danger">
                                <div class="panel-heading collapsible">
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableFailed" href="#">${steps.failed?size} Failed ABR Detection<#if (steps.failed?size>1)>s</#if></a></h3>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableSkipped" href="#" class="collapsed">${steps.skipped?size} Skipped ABR Detection<#if (steps.skipped?size>1)>s</#if></a></h3>
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
                    <div class="row" id="stats">
                        <div class="col-md-12">
                            <table id="abr" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th>Genome</th>
                                        <th class="text-center">ABR Profile</th>
                                        <th class="text-center"># ABR Target Drugs</th>
                                        <th class="text-center"># ABR Genes</th>
                                        <th class="text-center"># Potential ABR Genes</th>
                                        <th class="text-center">Details</th>
                                        </tr>
                                    </thead>
                                <tbody>
                                <#list steps.finished as step>
                                    <tr>
                                        <td>${step.genome.id}</td>
                                        <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                        <td class="text-center"><div class="abrs ${step.abrProfile?join(" ")}"></div></td>
                                        <td class="text-center" gradient="1"><abbr class="abr-list" title="ABR Target Drug List" data-content="${step.targetDrugs?join("<br>")}">${step.targetDrugs?size}</abbr></td>
                                        <td class="text-center" gradient="2">${step.abr.perfect?size}</td>
                                        <td class="text-center" gradient="3">${step.noPotentialResistances}</td>
                                        <td class="text-center"><a href="./abr/${step.genomeName}.html"><span class="glyphicon glyphicon-search"></span></a></td>
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
    </body>
</html>
