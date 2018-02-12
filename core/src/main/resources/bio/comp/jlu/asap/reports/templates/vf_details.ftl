<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta_sub.ftl">

	<link href="../css/datatables.min.css" rel="stylesheet">
        <script src="../js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {
                $('#vf').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
                    columnDefs: [
                        { orderable: false, targets: [] }
                    ],
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'vf-${project.genus[0]}_${genome.species}_${genome.strain}',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3, 4, 5 ]
                            }
                        }
                    ],
                    language: {
                        decimal: ",",
                    }
                } );
            } );
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
                        <li><a href="../vf.html">Virulence Factors</a></li>
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
                            <h2 class="modal-title" id="myModalLabel">ASAP virulence factors detail</h2>
                          </div>
                          <div class="modal-body">
                            <h3 id="interactivedatatable">Interactive data table</h3>

                            <p>
                              <code>Individual sorting</code> can be applied via clicking on the respective column header.
                              Use the <code>Search</code> function (top right of the table) to display only genomes that
                              contain the search term in any of their table fields. The <code>number of entries</code> displayed
                              per page can be chosen on the top left of the table.</p>

                            <h3>Downloads</h3>

                            <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv
                              button (search and sorting are contained in the downloaded file).</p>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong>Category</strong>: Virulence factor category designation according to its function.</li>

                              <li><strong>Coverage</strong>: Sequence coverage of this data base hit in percent.</li>

                              <li><strong>eValue</strong>: Expected number of virulence factors in the database used with a score
                                equivalent or higher than this match.</li>

                              <li><strong>Gene</strong>: Gene name in case it is provided by the virulence factor data base.</li>

                              <li><strong>Locus</strong>: Designation of the annotated genomic region.</li>

                              <li><strong>Product</strong>: Short description of the product associated with the locus.</li>

                              <li><strong># VFs</strong>: Number of assigned virulence factors hits per genome.</li>

                              <li><strong># VF categories</strong>: Number of virulence factor categories per genome.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                    <#-- content start -->

                    <div class="row">
                        <div class="col-md-12">
                            <h2><small>Virulence Factors</small></h2>
                            <table id="vf" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th class="text-center">Locus</th>
                                        <th class="text-center">Gene</th>
                                        <th class="text-center">Product</th>
                                        <th class="text-center">Category</th>
                                        <th class="text-center">Coverage [%]</th>
                                        <th class="text-center">Identity [%]</th>
                                        </tr>
                                    </thead>
                                <tbody>
                                    <#list vf as item>
                                    <tr>
                                        <td class="text-center">${item.locus}</td>
                                        <td class="text-center">${item.gene}</td>
                                        <td class="text-center">${item.product}</td>
                                        <td class="text-center">${item.catName} (${item.catId})</td>
                                        <td class="text-center">${item.coverage?round}</td>
                                        <td class="text-center">${item.pIdent?round}</td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
