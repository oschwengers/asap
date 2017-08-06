<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta.ftl">

	<link href="css/datatables.min.css" rel="stylesheet">
        <script src="js/datatables.min.js"></script>

        <script src="js/wordcloud2.js"></script>

        <script>
            $(document).ready(function() {
                $('#refFree').DataTable( {
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
                            filename: 'taxonomy-reference-free',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3 ]
                            }
                        }
                    ],
                    language: {
                        decimal: ",",
                    }
                } );
                $('#refBased').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
                    columnDefs: [
                        { orderable: false, targets: [5] }
                    ],
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'taxonomy-ani',
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

    </head>
    <body>
        <#include "commons/header.ftl">
        <div class="container-fluid">
            <div class="row">
                <#include "commons/menu.ftl">

                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

                    <ol class="breadcrumb">
                        <li><a href="index.html">Dashboard</a></li>
                        <li class="active">Taxonomy</li>
                        <!-- trigger help-modal -->
                        <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal"></i>
                    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                      <div class="modal-dialog" role="document">
                        <div class="modal-content">
                          <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h2 class="modal-title" id="myModalLabel">ASAP taxonomy overview</h2>
                          </div>
                          <div class="modal-body">
                            <p>
                              For the taxonomic classification of bacterial isolates ASA³P uses three distinct methods:</p>

                            <ul>
                              <li>Kmer profiles</li>

                              <li>16S sequence homology</li>

                              <li>Comparison of average nucleotide identities (<code>ANI</code>)</li>
                            </ul>

                            <p>The first two are reference free solutions where the last one is reference based approach. Kmer profiles are analyzed via
                              the <code>Kraken</code> tool and subsequent kmer profile hits are extracted from a custom <code>RefSeq</code> based database.
                              In order to search for 16S homology the pipeline uses <code>Infernal</code> to extract the best scoring 16S sequence and
                              subsequently queries it against the <code>RDP</code> 16S database.</p>

                            <p>Finally, the pipeline uses a proprietary ANI implementation based on <code>Nucmer</code> to calculate whole genome sequence
                              identity as a reference based solution.</p>

                            <h3 id="interactivedatatables">Interactive data tables</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>  function (top right of the table) to display only genomes that contain the search term in any of their table fields. The
                              <code>number of entries</code> displayed per page can be chosen on the top left of the table. Mouse over on underlined
                              table headers to display further information on it.</p>

                            <h5 id="referencefreeclassifications">Reference Free Classifications</h5>

                            <p>The results from Kraken and Infernal are displayed.</p>

                            <h5 id="highestreferenceanis">Highest Reference ANIs</h5>

                            <p>The results from Nucmer based ANI classification are displayed.</p>

                            <h3 id="downloads">Downloads</h3>

                            <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search and sorting are
                              contained in the downloaded file).</p>

                            <h3 id="links">Links</h3>

                            <ul>
                              <li><a href="https://www.ncbi.nlm.nih.gov/pubmed/17220447">ANI</a>: Goris, Johan, et al. "DNA–DNA hybridization values and
                                their relationship to whole-genome sequence similarities." International journal of systematic and evolutionary microbiology
                                57.1 (2007): 81-91. <a href="https://www.ncbi.nlm.nih.gov/pubmed/17220447">PubMed</a>.</li>

                              <li><code>Details</code> on the taxonomy of a particular genome can be accessed via click on the magnifying glass in the overview
                                table.</li>

                              <li><code>kmer</code> column value redirects to kmer taxonomic classification in the ncbi Taxonomy Browser.</li>

                              <li><code>16S rRNA</code> column value redirects to 16S rRNA taxonomic classification in the ncbi Taxonomy Browser.</li>

                              <li><a href="https://ccb.jhu.edu/software/kraken/">Kraken</a>: Wood DE, Salzberg SL: Kraken: ultrafast metagenomic sequence
                                classification using exact alignments. Genome Biology 2014, 15:R46. <a href="https://www.ncbi.nlm.nih.gov/pubmed/24580807">PubMed</a>.</li>

                              <li><a href="http://eddylab.org/infernal/">Infernal</a>: E. P. Nawrocki and S. R. Eddy, Infernal 1.1: 100-fold faster RNA homology
                                searches, Bioinformatics 29:2933-2935 (2013). <a href="https://www.ncbi.nlm.nih.gov/pubmed/24008419">PubMed</a>.</li>

                              <li><a href="http://mummer.sourceforge.net/">MUMmer/Nucmer</a>: Open source MUMmer 3.0 is described in "Versatile and open
                                software for comparing large genomes." S. Kurtz, A. Phillippy, A.L. Delcher, M. Smoot, M. Shumway, C. Antonescu, and
                                S.L. Salzberg, Genome Biology (2004), 5:R12. <a href="https://www.ncbi.nlm.nih.gov/pubmed/14759262">PubMed</a>.</li>

                              <li><a href="https://rdp.cme.msu.edu/">RDP</a>: Cole, J. R., Q. Wang, J. A. Fish, B. Chai, D. M. McGarrell, Y. Sun, C. T. Brown,
                                A. Porras-Alfaro, C. R. Kuske, and J. M. Tiedje. 2014. Ribosomal Database Project: data and tools for high throughput
                                rRNA analysis Nucl. Acids Res. 42(Database issue):D633-D642; doi: 10.1093/nar/gkt1244. <a href="https://www.ncbi.nlm.nih.gov/pubmed/24288368">PubMed</a>.</li>
                            </ul>

                            <h3 id="glossary">Glossary</h3>

                            <ul>
                              <li><strong>16S Classification</strong>: Rfam 16S based taxonomic classification via Infernal.</li>

                              <li><strong>ANI [%]</strong>: Percent average nucleotide identity. Based on the ANI publication the sequenced genome is split
                                into 1020 bp fragments which are compared against the reference (in our approach Nucmer was used instead of blastN).
                                For the calculation the length of the fragments with less than 30% non identities and an alignment length higher than
                                70% are summed and divided by the total length of the sequenced genome.</li>

                              <li><strong>Conserved DNA [%]</strong>: Percent conserved DNA. Based on the ANI publication the sequenced genome is split into
                                1020 bp fragments which are compared against the reference (in our approach Nucmer was used instead of blastN). For the
                                calculation the length of the fragments that matched with 90% sequence identity or higher are summed and divided by the
                                total length of the sequenced genome.</li>

                              <li><strong>Genome</strong>: Name of the processed genome.</li>

                              <li><strong>Kmer Classification</strong>: Kmer based taxonomic classification via Kraken.</li>

                              <li><strong>Reference</strong>: ID of the reference genome used for taconomic classification.</li>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableFailed" href="#">${steps.failed?size} Failed Taxonomy Classification<#if (steps.failed?size>1)>s</#if></a></h3>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableSkipped" href="#" class="collapsed">${steps.skipped?size} Skipped Taxonomy Classification<#if (steps.skipped?size>1)>s</#if></a></h3>
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
                    <div class="row">
                        <div class="col-md-10">
                            <h2><small>Reference Free Classifications</small></h2>
                            <table id="refFree" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th>Genome</th>
                                        <th class="text-center">Kmer</th>
                                        <th class="text-center">16S rRNA</th>
                                        <th class="text-center">Details</th>
                                        </tr>
                                    </thead>
                                <tbody>
                                <#list steps.finished as step>
                                    <tr>
                                        <td>${step.genome.id}</td>
                                        <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                        <td class="text-center"><#if step.kmer.classification?has_content><a href="http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?name=${step.kmer.classification.classification?replace(" ", "+")}" target="_blank">${step.kmer.classification.classification}</a><#else>-</#if></td>
                                        <td class="text-center"><#if step.rrna.classification?has_content><a href="http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?name=${step.rrna.classification.classification?replace(" ", "+")}" target="_blank">${step.rrna.classification.classification}</a><#else>-</#if></td>
                                        <td class="text-center"><a href="./taxonomy/${step.genomeName}.html"><span class="glyphicon glyphicon-search"></span></a></td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="row voffset">
                        <div class="col-md-8">
                            <h2><small>Highest Reference <abbr title="Average Nucleotide Identity">ANI</abbr>s</small></h2>
                            <table id="refBased" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th>Genome</th>
                                        <th class="text-center">Reference</th>
                                        <th class="text-center"><abbr title="Average Nucleotide Identity">ANI</abbr> [%]</th>
                                        <th class="text-center">Conserved DNA [%]</th>
                                        <th class="text-center">Details</th>
                                        </tr>
                                    </thead>
                                <tbody>
                                <#list steps.finished as step>
                                    <tr>
                                        <td>${step.genome.id}</td>
                                        <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                        <td class="text-center">${step.ani.best.reference}</td>
                                        <td class="text-center">${((step.ani.best.ani*100)?string["0.0"])}</td>
                                        <td class="text-center">${((step.ani.best.conservedDNA*100)?string["0.0"])}</td>
                                        <td class="text-center"><a href="./taxonomy/${step.genomeName}.html"><span class="glyphicon glyphicon-search"></span></a></td>
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
