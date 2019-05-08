<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta.ftl">

	<link href="css/datatables.min.css" rel="stylesheet">
        <script src="js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {
                $('#snps').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
                    columnDefs: [
                        { orderable: false, targets: [6,7] }
                    ],
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'snps',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3, 4, 5 ]
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
                        <li class="active">SNPs</li>
                        <!-- trigger help-modal -->
                        <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal"></i>
                    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                      <div class="modal-dialog" role="document">
                        <div class="modal-content">
                          <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h2 class="modal-title" id="myModalLabel">ASA&#179;P single nucleotide polymorphism overview</h2>
                          </div>
                          <div class="modal-body">
                            <p>
                              This analysis provides information on SNPs compared to the first reference genome. Via the mpileup function of <code>SAMtools</code>
                              the mapped BAM files together with the reference fasta are used to compute the likelihood of each possible genotype. The
                              resulting likelihoods containing genomic positions are stored as Binary Variant Call Format (BCF). <code>BCFtools</code>is
                              then used to call variants in the sequence compared to the reference. The genomic variants in the resulting Variant Call
                              Format (VCF) file are then filtered via <code>SnpSift</code>. The filtered variants are then analysed by <code>SnpEff</code>
                              to predict the resulting effects. To improve further processing and compressing <code>HTSlib</code> is used. Finally, a
                              consensus sequence and statistics is calculated with <code>BCFtools</code>. This page provides an average SNP distribution
                              mapping and a SNP comparison of the analysed genome.</p>

                            <h3>SNP distribution graph</h3>

                            <p>The <code>mean number of SNPs</code> per <code>10 kb</code> compared to the reference genome are displayed. Mouse over the
                              graph to display the position and mean SNP number of an individual peak.</p>

                            <h3>Interactive data table</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>
                                function (top right of the table) to display only genomes that contain the search term in any of their table fields. The
                              <code>number of entries</code> displayed per page can be chosen on the top left of the table. <code>Blue horizontal bar plots</code>
                              are displayed in most columns containing numeric values. They visualize the relative relation of this value compared to
                              the according values of the other genomes. Mouse over on underlined term to display further information on it.</p>

                            <h3>Downloads</h3>

                            <p>The table can be saved as a comma separated value (<code>csv</code>) file via a click on the csv button (search and sorting are
                              contained in the downloaded file). The <code>vcf</code> file of each genome can be downloaded.</p>

                            <h3>Links</h3>

                            <ul>
                                <li><code>Details</code> on the SNPs of a particular genome can be accessed via click on the magnifying glass in the overview
                                table.</li>

                                <li><a href="http://www.htslib.org/">SAMtools</a>: Li H., Handsaker B., Wysoker A., Fennell T., Ruan J., Homer N., Marth G., Abecasis G., Durbin R., 1000 Genome Project Data Processing Subgroup
                                    The Sequence alignment/map (SAM) format and SAMtools.
                                    Bioinformatics, 2009, 25, 2078-9.
                                    <a href="https://www.ncbi.nlm.nih.gov/pubmed/19505943">PubMed</a>
                                </li>

                                <li><a href="https://github.com/samtools/bcftools">BCFtools</a>: Included in SAMtools.
                                </li>

                                <li><a href="http://snpeff.sourceforge.net/SnpSift.html">SnpSift</a>: Cingolani, P., et. al.
                                    Using Drosophila melanogaster as a model for genotoxic chemical mutational studies with a new program, SnpSift
                                    Frontiers in Genetics, 2012, 3
                                    <a href="https://www.ncbi.nlm.nih.gov/pubmed/22435069">PubMed</a>
                                </li>

                                <li><a href="http://snpeff.sourceforge.net/">SnpEff</a>: Cingolani P, Platts A, Wang le L, Coon M, Nguyen T, Wang L, Land SJ, Lu X, Ruden DM.
                                    A program for annotating and predicting the effects of single nucleotide polymorphisms, SnpEff: SNPs in the genome of Drosophila melanogaster strain w1118; iso-2; iso-3.
                                    Fly (Austin), 2012 Apr-Jun;6(2):80-92. <a href="https://www.ncbi.nlm.nih.gov/pubmed/22728672">PubMed</a>
                                </li>

                            </ul>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong>Change Range</strong>: Ratio of number single nucleotide polymorphisms to genome size.</li>

                              <li><strong>Genome</strong>: Name of the processed genome.</li>

                              <li><strong>HI SNPs</strong>: Number of high impact single nucleotide polymorphisms. SNPs are considered high impact if they
                                result in the gain or loss of a start or stop codon.</li>

                              <li><strong>SNPs</strong>: Number of single nucleotide polymorphisms.</li>

                              <li><strong>TS/TV</strong>: Ratio of number nucleotide transitions to number nucleotide transversions.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                    <#-- content start -->

                <#if avrgBinnedCounts?has_content>
                    <div class="row">
                        <div class="col-md-12">
                            <div id="chartSNPDist"></div>
                            <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
                            <script type="text/javascript">
                                google.charts.load( 'current', {'packages': ['corechart'] } );
                                google.charts.setOnLoadCallback( drawChart );
                                function drawChart() {
                                    var data = google.visualization.arrayToDataTable( [
                                        ['Position', '# SNPs'],
                                        ${avrgBinnedCounts}
                                    ] );
                                    var options = { // chart options
                                        legend: { position: 'none' },
                                        colors: ['#428bca'],
                                        width: '100%',
                                        height: 300,
                                        chartArea: {
                                            left:50,
                                            top:20,
                                            width: '94%',
                                            height: '80%'
                                        },
                                        hAxis: {
                                            title: 'Mean # SNPs'
                                        },
                                        vAxis: {
                                            title: 'Position [10 kb]'
                                        },
                                        axisTitlesPosition: 'in'
                                    };
                                    var chart = new google.visualization.LineChart( document.getElementById( 'chartSNPDist' ) );
                                    chart.draw( data, options );
                                }
                            </script>
                        </div>
                    </div>
                </#if>

                    <div class="row" id="warnings">
                <#if steps.failed?has_content >
                        <div class="col-md-4">
                            <div class="panel panel-danger">
                                <div class="panel-heading collapsible">
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableFailed" href="#">${steps.failed?size} Failed SNP Detection<#if (steps.failed?size>1)>s</#if></a></h3>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableSkipped" href="#" class="collapsed">${steps.skipped?size} Skipped SNP Detection<#if (steps.skipped?size>1)>s</#if></a></h3>
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
                    <div class="row voffset" id="stats">
                        <#-- statistics -->
                        <div class="col-md-12">
                            <table id="snps" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th>Genome</th>
                                        <th class="text-center"><abbr title="single nucleotide Polymorphism">SNP</abrr>s</th>
                                        <th class="text-center"><abbr title="high impact SNP">HI SNP</abrr>s</th>
                                        <th class="text-center"><abbr title="transitions / transversion">TS/TV</abrr></th>
                                        <th class="text-center"><abbr title="genome size / # SNPs">Change Range</abrr></th>
                                        <th class="text-center">Downloads</th>
                                        <th class="text-center">Details</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#list steps.finished as step>
                                    <tr>
                                        <td>${step.genome.id}</td>
                                        <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                        <td class="text-center" gradient="1">${step.noSNPs}</td>
                                        <td class="text-center" gradient="2">${step.noHighImpactSNPs}</td>
                                        <td class="text-center">${step.tstv.tstv?string["0.#"]}</td>
                                        <td class="text-center" gradient="3">${step.changeRate}</td>
                                        <td class="text-center"><a href="./snps/${step.genomeName}.vcf.gz">vcf.gz</a></td>
                                        <td class="text-center"><#if (step.noSNPs>0)><a href="./snps/${step.genomeName}.html"><span class="glyphicon glyphicon-search"></span></a><#else>-</#if></td>
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
