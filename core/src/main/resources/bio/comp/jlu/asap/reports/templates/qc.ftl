<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta.ftl">

	<link href="css/datatables.min.css" rel="stylesheet">
        <script src="js/datatables.min.js"></script>

        <script src="js/gradient.js" defer></script>

        <script>
            $(document).ready(function() {
                $('#qc').DataTable( {
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
                            filename: 'qc',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3, 4, 5, 6 ]
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
                        <li class="active">Quality Control</li>
                        <!-- trigger help-modal -->
                        <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal"></i>
                    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                        <div class="modal-dialog" role="document">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                                    <h2 class="modal-title" id="myModalLabel">ASAP quality control overview</h2>
                                </div>
                                <div class="modal-body">
                                    <p>Provides an overview on the quality control of the analysed genomes. The quality of the sequenced reads is determined via <code>FastQC</code>. For reference based quality acquisition  <code>FastQ Screen</code> is utilised. Depending on sequencing source (NGS or PacBio) filtering based on qualities is performed with <code>Trimmomatic</code>. Trimmomatic setting are: "ILLUMINACLIP: <PE or SE adapter> and <phiX data base> :2:30:10", 'LEADING:15', 'TRAILING:15', 'SLIDINGWINDOW:4:20', 'MINLEN:20', 'TOPHRED33'. Only reads that pass the quality control are included in the following analysis.</p>

                                    <h3 id="interactivedatatable">Interactive data table</h3>

                                    <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code> function (top right of the table) to display only genomes that contain the search term in any of their table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of the table.  <code>Blue horizontal bar plots</code> are displayed in '# Reads' column. Their data field filling ratio corresponds to the ratio of field value to column maximum. Mouse over on underlined table headers to display further information on it.</p>

                                    <h3 id="downloads">Downloads</h3>

                                    <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search and sorting are contained in the downloaded file).</p>

                                    <h3 id="links">Links</h3>

                                    <ul>
                                    <li><code>Details</code> on the quality control of a particular genome can be accessed via click on the magnifying glass in the overview table.</li>

                                    <li><a href="https://www.bioinformatics.babraham.ac.uk/projects/fastqc/">FastQC</a>; Simon Andrews (2010). FastQC: A quality control tool for high throughput sequence data.</li>

                                    <li><a href="http://www.bioinformatics.babraham.ac.uk/projects/fastq_screen/">FastQ Screen</a>; Steven Wingett (2011). FastQ Screen allows you to screen a library of sequences in FastQ format against a set of sequence databases so you can see if the composition of the library matches with what you expect.</li>

                                    <li><a href="http://www.usadellab.org/cms/?page=trimmomatic">Trimmomatic</a>: Bolger, A. M., Lohse, M., &amp; Usadel, B. (2014). Trimmomatic: A flexible trimmer for Illumina Sequence Data. Bioinformatics, btu170. <a href="https://www.ncbi.nlm.nih.gov/pubmed/24695404">PubMed</a>.</li>
                                    </ul>

                                    <h3 id="glossary">Glossary</h3>

                                    <ul>
                                    <li><strong>GC</strong>: GC content in percent.</li>

                                    <li><strong>Genome</strong>: Name of the processed genome.</li>

                                    <li><strong>Length</strong>: Minimal/ mean/ maximal read length for this particular genome.</li>

                                    <li><strong>PC</strong>: Read percentage of potential contaminations. Based on a 10% random subset mapping against a contamination references data base (e.g. containing phiX sequences).</li>

                                    <li><strong>Quality</strong>: Minimal/ mean/ maximal PHRED score of sequenced reads for this particular genome (error probability; PHRED 20: 1 in 100; PHRED 30: 1 in 1000).</li>

                                    <li><strong># Reads</strong>: Number of sequenced reads for this particular genome.</li>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableFailed" href="#">${steps.failed?size} Failed Quality Control<#if (steps.failed?size>1)>s</#if></a></h3>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableSkipped" href="#">${steps.skipped?size} Skipped Quality Control<#if (steps.skipped?size>1)>s</#if></a></h3>
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
                            <table id="qc" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th>Genome</th>
                                        <th class="text-center"># Reads</th>
                                        <th class="text-center"><abbr title="[min / mean / max]">Lengths</abbr></th>
                                        <th class="text-center"><abbr title="GC content [%]">GC</abbr></th>
                                        <th class="text-center"><abbr title="PHRED score [min / mean / max]">Quality</abbr></th>
                                        <th class="text-center"><abbr title="possible contamination (reads mapping to foreign genomes) [%]">PC</abbr></th>
                                        <th class="text-center">Details</th>
                                        </tr>
                                    </thead>
                                <tbody>
                                <#list steps.finished as step>
                                    <tr>
                                        <td>${step.genome.id}</td>
                                        <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                        <td class="text-center" gradient="1">${step.qcReadsAvg.noReads}</td>
                                        <td class="text-center">${step.qcReadsAvg.readLengths.min?round} / ${step.qcReadsAvg.readLengths.mean?string["0.#"]} / ${step.qcReadsAvg.readLengths.max?round}</td>
                                        <td class="text-center">${step.qcReadsAvg.gc}</td>
                                        <td class="text-center">${step.qcReadsAvg.qual.min?round} / ${step.qcReadsAvg.qual.mean?string["0.#"]} / ${step.qcReadsAvg.qual.max?round}</td>
                                        <td class="text-center">${step.contaminations.potentialContaminations?string["0.0"]}</td>
                                        <td class="text-center"><a href="reads_qc/${step.genomeName}.html"><span class="glyphicon glyphicon-search"></span></a></td>
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
