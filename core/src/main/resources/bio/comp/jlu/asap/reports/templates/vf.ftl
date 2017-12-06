<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta.ftl">

	<link href="css/datatables.min.css" rel="stylesheet">
        <script src="js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {
                $('#vf').DataTable( {
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
                            filename: 'vf',
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
                        <li class="active">Virulence Factors</li>
                        <!-- trigger help-modal -->
                        <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal"></i>
                    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                      <div class="modal-dialog" role="document">
                        <div class="modal-content">
                          <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h2 class="modal-title" id="myModalLabel">ASAP virulence factors overview</h2>
                          </div>
                          <div class="modal-body">
                            <p>
                              As VF have a major impact on whether a bacterial strain is harmless or a severe pathogen ASA³P provides a detection of potential
                              VFs. Therefore, the pipeline identifies VFs via a <code>BLASTn</code> search against the virulence factor database (<code>VFDB</code>).
                              Hits with a coverage of at least 80 % and a percent identity of 90 % or higher are taken into account. The corresponding
                              loci are only assigned with their highest scoring hit. </p>

                            <h3>Interactive data table</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>  function (top right of the table) to display only genomes that contain the search term in any of their table fields. The
                              <code>number of entries</code> displayed per page can be chosen on the top left of the table. <code>Blue horizontal bar plots</code>  are displayed in columns containing numeric values. They visualize the relative relation of this value compared to the
                              according values of the other genomes. Mouse over on underlined term to display further information on it.</p>

                            <h3>Downloads</h3>

                            <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search and sorting are
                              contained in the downloaded file).</p>

                            <h3>Links</h3>

                            <ul>
                              <li><code>Details</code> on the virulence factors of a particular genome can be accessed via click on the magnifying glass
                                in the overview table.</li>

                              <li><a href="http://www.mgc.ac.cn/VFs/main.htm">VFDB</a>: Chen LH, Zheng DD, Liu B, Yang J and Jin Q, 2016. VFDB 2016: hierarchical
                                and refined dataset for big data analysis-10 years on. Nucleic Acids Res. 44(Database issue):D694-D697. <a href="https://www.ncbi.nlm.nih.gov/pubmed/26578559">PubMed</a>.</li>
                            </ul>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong>Genome</strong>: Name of the processed genome.</li>

                              <li><strong>Locus</strong>: Designation of the annotated genomic region.</li>

                              <li><strong># VFs</strong>: Number of assigned virulence factors hits per genome.</li>

                              <li><strong># VF categories</strong>: Number of virulence factor categories per genome.</li>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableFailed" href="#">${steps.failed?size} Failed VF Detection<#if (steps.failed?size>1)>s</#if></a></h3>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableSkipped" href="#" class="collapsed">${steps.skipped?size} Skipped VF Detection<#if (steps.skipped?size>1)>s</#if></a></h3>
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
                            <table id="vf" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th>Genome</th>
                                        <th class="text-center"># <abbr title="Number of detected virulence genes">VF</abbr>s</th>
                                        <th class="text-center"># <abbr title="Number of distinct virulence gene categories">VF</abbr> Categories</th>
                                        <th class="text-center">Details</th>
                                        </tr>
                                    </thead>
                                <tbody>
                                <#list steps.finished as step>
                                    <tr>
                                        <td>${step.genome.id}</td>
                                        <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                        <td class="text-center" gradient="1">${step.noVFs}</td>
                                        <td class="text-center" gradient="2">${step.noDistinctCategories}</td>
                                        <td class="text-center"><a href="./vf/${step.genomeName}.html"><span class="glyphicon glyphicon-search"></span></a></td>
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
