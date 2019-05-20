<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta.ftl">

	<link href="css/datatables.min.css" rel="stylesheet">
        <script src="js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {
                $('#scaffolds').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
                    columnDefs: [
                        { orderable: false, targets: [5,6] }
                    ],
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'scaffolds',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3, 4 ]
                            }
                        }
                    ]
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
                        <li class="active">Scaffolds</li>
                        <!-- trigger help-modal -->
                        <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal"></i>
                    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                      <div class="modal-dialog" role="document">
                        <div class="modal-content">
                          <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h2 class="modal-title" id="myModalLabel">ASA&#179;P scaffolds overview</h2>
                          </div>
                          <div class="modal-body">
                            <p>
                              Orders and orientations of assembled contigs are somewhat arbitrary. During a scaffolding step ASA&#179;P tries to map those contigs
                              onto a set of closely related (user provided) reference genomes in order to order and rearrange them. With this additional information
                              scaffolders can fix the order and orientation and merge multiple contigs into scaffolds. As a modern multi-reference scaffolder
                              ASA&#179;P internally takes advantage of the tool <code>MeDuSa</code>. As joined contigs pose an artificial bridge an artificial
                              six frame stop codon sequence is used to mark such positions 'NNNNNNNNNNCTAGCTAGCTAGCNNNNNNNNNN'. By using this sequence
                              to link all scaffolds and contigs ASA&#179;P also provides <code>pseudo genomes</code>. Finally, raw contigs as well as oriented
                              and linked scaffolds are mapped onto all provided reference genomes in order to compare the results of this step.</p>

                            <h3>Interactive data table</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>  function (top right of the table) to display only genomes that contain the search term in any of their table fields. The
                              <code>number of entries</code> displayed per page can be chosen on the top left of the table. <code>Blue horizontal bar plots</code>  are displayed in columns containing numeric values. Their data field filling ratio corresponds to the ratio of field value
                              to column maximum. Mouse over on underlined table headers to display further information on it.</p>

                            <h3>Downloads</h3>

                            <p>The table can be saved as a comma separated value (<code>csv</code>) file via a click on the csv button (search and sorting are
                              contained in the downloaded file). To download a <code>fasta</code> file containing the Scaffolds or the generated <code>Pseudo genome</code>  click on the according name in the data table.</p>

                            <h3>Links</h3>

                            <ul>
                                <li><code>Details</code> on the contig layout of a particular genome can be accessed via click on the magnifying glass in the
                                    overview table.</li>

                                <li><a href="http://combo.dbe.unifi.it/medusa">MeDuSa</a>: E Bosi, B Donati, M Galardini, S Brunetti, MF Sagot, P Lió, P Crescenzi, R Fani, and M Fondi.
                                    MeDuSa: a multi-draft based scaffolder.
                                    Bioinformatics, 2015, btv171.
                                    <a href="https://www.ncbi.nlm.nih.gov/pubmed/25810435">PubMed</a>
                                </li>

                                <li><a href="http://mummer.sourceforge.net/">MUMmer/Nucmer</a>: S. Kurtz, A. Phillippy, A.L. Delcher, M. Smoot, M. Shumway, C. Antonescu, and S.L. Salzberg
                                    Versatile and open software for comparing large genomes.
                                    Genome Biology, 2004, 5:R12.
                                    <a href="https://www.ncbi.nlm.nih.gov/pubmed/14759262">PubMed</a>
                                </li>
                            </ul>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong># Contigs</strong>: Number of contigs (set of overlapping DNA segments).</li>

                              <li><strong>Genome</strong>: Name of the processed genome.</li>

                              <li><strong>N50</strong>: Given ordered contigs from longest to smallest, length of the contig at 50% of the genome length.</li>

                              <li><strong>Pseudo genome</strong>: Genome generated via joining all sequence elements after scaffolding with the sequence
                                'NNNNNNNNNNCTAGCTAGCTAGCNNNNNNNNNN'.</li>

                              <li><strong># Scaffolds</strong>: Number of scaffolds (joined, aligned and assigned contigs) after polishing. Joined with the
                                sequence 'NNNNNNNNNNCTAGCTAGCTAGCNNNNNNNNNN'.</li>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableFailed" href="#">${steps.failed?size} Failed Scaffolding Step<#if (steps.failed?size>1)>s</#if></a></h3>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableSkipped" href="#">${steps.skipped?size} Skipped Scaffolding Step<#if (steps.skipped?size>1)>s</#if></a></h3>
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
                        <div class="col-md-10">
                            <table id="scaffolds" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th>Genome</th>
                                        <th class="text-center"># Scaffolds</th>
                                        <th class="text-center"># Contigs</th>
                                        <th class="text-center">N50</th>
                                        <th class="text-center">Downloads</th>
                                        <th class="text-center">Details</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#list steps.finished as step>
                                    <tr>
                                        <td>${step.genome.id}</td>
                                        <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                        <td class="text-center" gradient="1">${step.scaffolds.noScaffolds}</td>
                                        <td class="text-center" gradient="2">${step.scaffolds.noContigs}</td>
                                        <td class="text-center" gradient="3">${step.scaffolds.n50}</td>
                                        <td class="text-center">
                                            <a href="scaffolds/${step.genomeName}/${step.genomeName}.fasta">Scaffolds</a><br>
                                            <a href="scaffolds/${step.genomeName}/${step.genomeName}-pseudo.fasta">Pseudo Genome</a>
                                        </td>
                                        <td class="text-center"><a href="scaffolds/${step.genomeName}.html"><span class="glyphicon glyphicon-search"></span></a></td>
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
