<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta.ftl">

	<link href="css/datatables.min.css" rel="stylesheet">
        <script src="js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {
                $('#mappings').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
                    columnDefs: [
                        { orderable: false, targets: 7 }
                    ],
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'mappings',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3, 4, 5, 6 ]
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
                        <li class="active">Mappings</li>
                        <!-- trigger help-modal -->
                        <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal"></i>
                    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                      <div class="modal-dialog" role="document">
                        <div class="modal-content">
                          <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h2 class="modal-title" id="myModalLabel">ASA&#179;P reference mapping overview</h2>
                          </div>
                          <div class="modal-body">
                            <p>
                              In order to assess an isolate genome size compared to a reference genome and subsequently enable the calling of single nucleotide
                              variants sequenced and quality clipped reads are mapped to the first reference genome as provided in the config sheet.
                              For Illumina and PacBio reads ASA&#179;P uses <code>Bowtie 2</code> and <code>blasr</code>, respectively.
                              For Oxford Nanopore reads ASA&#179;P uses <code>MiniMap2</code>. Finally, generated Sequence alignment/map (<code>SAM</code>)
                              files are converted to ordered binary alignment/map (<code>BAM</code>) files via <code>SAMtools</code>.</p>

                            <h3>Interactive data table</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header.
                              Use the <code>Search</code> function (top right of the table) to display only genomes that
                              contain the search term in any of their table fields. The <code>number of entries</code> displayed
                              per page can be chosen on the top left of the table. Mouse over on underlined term to display
                              further information on it.</p>

                            <h3>Downloads</h3>

                            <p>The table can be saved as a comma separated value (<code>csv</code>) file via a click on the csv
                              button (search and sorting are contained in the downloaded file). To download the <code>bam</code>                              file of a particular genome mapping click on bam in the data table.</p>

                            <h3>Links</h3>

                            <ul>
                                <li><a href="http://www.htslib.org/">SAMtools</a>: Li H., Handsaker B., Wysoker A., Fennell T., Ruan J., Homer N., Marth G., Abecasis G., Durbin R., 1000 Genome Project Data Processing Subgroup
                                    The Sequence alignment/map (SAM) format and SAMtools.
                                    Bioinformatics, 2009, 25, 2078-9.
                                    <a href="https://www.ncbi.nlm.nih.gov/pubmed/19505943">PubMed</a>
                                </li>

                                <li><a href="http://bowtie-bio.sourceforge.net/bowtie2/index.shtml">Bowtie 2</a>: Langmead B, Salzberg S.
                                    Fast gapped-read alignment with Bowtie 2.
                                    Nature Methods, 2012, 9:357-359.
                                    <a href="https://www.ncbi.nlm.nih.gov/pubmed/22388286">PubMed</a>
                                </li>

                              <li><a href="https://github.com/PacificBiosciences/blasr">blasr</a>: The PacBio® long read aligner.</li>

                              <li><a href="https://github.com/lh3/minimap2">Minimap2</a>: Li H.,
                                  Minimap2: pairwise alignment for nucleotide sequences.
                                  Bioinformatics, 2018, 34(18):3094-3100
                                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/29750242">PubMed</a>
                              </li>
                            </ul>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong>Genome</strong>: Name of the processed genome.</li>

                              <li><strong># Multiple</strong>: Number of reads that mapped multiple times.</li>

                              <li><strong>Ratio</strong>: Ratio of total reads that could be mapped to the reference.</li>

                              <li><strong># Reads</strong>: Total number of analysed reads.</li>

                              <li><strong># Unique</strong>: Number of reads that mapped once.</li>

                              <li><strong># Unmapped</strong>: Number of reads that could not be mapped to the reference.</li>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableFailed" href="#">${steps.failed?size} Failed Mapping<#if (steps.failed?size>1)>s</#if></a></h3>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableSkipped" href="#" class="collapsed">${steps.skipped?size} Skipped Mapping<#if (steps.skipped?size>1)>s</#if></a></h3>
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
                            <table id="mappings" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th>Genome</th>
                                        <th class="text-center"><abbr title="Number of total reads"># Reads</abbr></th>
                                        <th class="text-center"><abbr title="Number of uniquely mapped reads"># Unique</abbr></th>
                                        <th class="text-center"><abbr title="Number of reads with multiple mappings"># Multiple</abbr></th>
                                        <th class="text-center"><abbr title="Number of unmapped reads"># Unmapped</abbr></th>
                                        <th class="text-center"><abbr title="Number of mapped reads divided by number of total reads">Ratio</abbr></th>
                                        <th class="text-center">Downloads</th>
                                        </tr>
                                    </thead>
                                <tbody>
                                <#list steps.finished as step>
                                    <tr>
                                        <td>${step.genome.id}</td>
                                        <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                        <td class="text-center" gradient="1">${step.reads}</td>
                                        <td class="text-center" gradient="2">${step.unique}</td>
                                        <td class="text-center" gradient="3">${step.multiple}</td>
                                        <td class="text-center" gradient="4">${step.unmapped}</td>
                                        <td class="text-center" gradient="5">${step.ratio?string["0.0"]}</td>
                                        <td class="text-center"><a href="./mappings/${project.genus}_${step.genome.species}_${step.genome.strain}.bam">bam</a></td>
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
