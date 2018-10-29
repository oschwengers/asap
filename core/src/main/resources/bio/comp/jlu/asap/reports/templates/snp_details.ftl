<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta_sub.ftl">

	<link href="../css/datatables.min.css" rel="stylesheet">
        <script src="../js/datatables.min.js"></script>

        <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.0/d3.min.js"></script>
	<script src="https://cdn.rawgit.com/novus/nvd3/v1.8.1/build/nv.d3.min.js"></script>

        <style>
            #chartCont svg {
                height: 200px;
                width: 800px;
            }
        </style>

        <script>
            $(document).ready(function() {
                $('#hisnps').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'snp-details-${project.genus[0]}_${genome.species}_${genome.strain}',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3, 4, 5, 6, 7, 8 ]
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
                        <li><a href="../snps.html">SNPs</a></li>
                        <li class="active dropdown"><a class="dropdown-toggle" data-toggle="dropdown" href="#">${project.genus[0]}. ${genome.species} ${genome.strain} <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                                <li><a href="#basic">SNP Distribution</a></li>
                                <li><a href="#pbsc">SNP Statistics</a></li>
                                <li><a href="#psq">SNP Effects</a></li>
                                <li><a href="#hisnps">High Impact SNPs</a></li>
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
                            <h2 class="modal-title" id="myModalLabel">ASA&#179;P single nucleotide polymorphism detail</h2>
                          </div>
                          <div class="modal-body">
                            <p>
                              This analysis provides information on SNPs compared to the reference genome. Via the mpileup function of <code>SAMtools</code> the mapped BAM files together
                              with the reference fasta are used to compute the likelihood of each possible genotype. The resulting
                              likelihoods containing genomic positions are stored as Binary Variant Call Format (BCF). <code>BCFtools</code>is
                              then used to call variants in the sequence compared to the reference. The genomic variants in
                              the resulting Variant Call Format (VCF) file are then filtered via <code>SnpSift</code>. The
                              filtered variants are then analysed by <code>SnpEff</code> to predict the resulting effects.
                              To improve further processing and compressing <code>HTSlib</code> is used. Finally a consensus
                              sequence and statistics is calculated with <code>BCFtools</code>. This page provides an average
                              SNP distribution mapping and a SNP comparison of the analysed genome.</p>

                            <h3>SNP distribution graph</h3>

                            <p>The <code>mean number of SNPs</code> per <code>10 kb</code> compared to the reference genome
                              are displayed. Mouse over the graph to display the position and mean SNP number of an individual
                              peak.</p>

                            <h3>Interactive data table</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header.
                              Use the <code>Search</code> function (top right of the table) to display only genomes that
                              contain the search term in any of their table fields. The <code>number of entries</code> displayed
                              per page can be chosen on the top left of the table. <code>Blue horizontal bar plots</code>                              are displayed in most columns containing numeric values. They visualize the relative relation
                              of this value compared to the according values of the other genomes. Mouse over on underlined
                              term to display further information on it.</p>

                            <h3>Downloads</h3>

                            <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv
                              button (search and sorting are contained in the downloaded file). The <code>vcf</code> file
                              of each genome can be downloaded.</p>

                            <h3>Links</h3>

                            <ul>
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

                              <li><strong>HI SNPs</strong>: Number of high impact single nucleotide polymorphisms. SNPs are considered
                                high impact if they result in the gain or loss of a start or stop codon.</li>

                              <li><strong>SNPs</strong>: Number of single nucleotide polymorphisms.</li>

                              <li><strong>TS/TV</strong>: Ratio of number nucleotide transitions to number nucleotide transversions.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                    <#-- content start -->

                    <div class="row">
                        <div class="col-md-12">
                            <div id="chartSNPDist"></div>
                            <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
                            <script type="text/javascript">
                                google.charts.load( 'current', {'packages': ['corechart'] } );
                                google.charts.setOnLoadCallback( drawChart );
                                function drawChart() {
                                    var data = google.visualization.arrayToDataTable( [
                                        ['Position', '# ⌀ SNPs', '# SNPs'],
                                        ${detailBinnedCounts}
                                    ] );
                                    var options = { // chart options
                                        legend: { position: 'bottom' },
                                        colors: ['#428bca', '#cd2d50'],
                                        width: '100%',
                                        height: 300,
                                        chartArea: {
                                            left:50,
                                            top:20,
                                            width: '94%',
                                            height: '80%'
                                        },
                                        hAxis: {
                                            title: '# SNPs'
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

                    <div class="row voffset" id="stats">
                        <div class="col-sm-4 col-md-4">
                            <div id="chartSNPRegions"></div>
                            <script type="text/javascript">
                                google.charts.setOnLoadCallback( function () {
                                    var data = google.visualization.arrayToDataTable( [
                                        ['Region', '# SNPs'],
                                        ["upstream", ${region.upstream?c}],
                                        ["gene", ${region.exon?c}],
                                        ["downstream", ${region.downstream?c}],
                                        ["intergenic", ${region.intergenic?c}]
                                    ] );
                                    var options = { // chart options
                                        legend: { position: 'none' },
                                    title: "Regions",
                                        colors: ['#428bca'],
                                        width: '100%',
                                        height: 200,
                                        orientation: 'horizontal',
                                        chartArea: {
                                            left:50,
                                            top:20,
                                            width: '94%',
                                            height: '80%'
                                        },
                                        vAxis: {format: 'short'}
                                    };
                                    var chart = new google.visualization.BarChart( document.getElementById( "chartSNPRegions" ) );
                                    chart.draw( data, options );
                                } );
                            </script>
                        </div>
                        <div class="col-sm-4 col-md-4">
                            <div id="chartSNPClasses"></div>
                            <script type="text/javascript">
                                google.charts.setOnLoadCallback( function () {
                                    var data = google.visualization.arrayToDataTable( [
                                        ['Classes', '# SNPs'],
                                        ["missense", ${classes.missense?c}],
                                        ["nonsense", ${classes.nonsense?c}],
                                        ["silent", ${classes.silent?c}]
                                    ] );
                                    var options = { // chart options
                                    title: "Classes",
                                        legend: { position: 'none' },
                                        colors: ['#428bca'],
                                        width: '100%',
                                        height: 200,
                                        orientation: 'horizontal',
                                        chartArea: {
                                            left:50,
                                            top:20,
                                            width: '94%',
                                            height: '80%'
                                        },
                                        vAxis: {format: 'short'}
                                    };
                                    var chart = new google.visualization.BarChart( document.getElementById( "chartSNPClasses" ) );
                                    chart.draw( data, options );
                                } );
                            </script>
                        </div>
                        <div class="col-sm-4 col-md-4">
                            <div id="chartSNPImpacts"></div>
                            <script type="text/javascript">
                                google.charts.setOnLoadCallback( function () {
                                    var data = google.visualization.arrayToDataTable( [
                                        ['Impacts', '# SNPs'],
                                        ["high", ${impacts.high?c}],
                                        ["moderate", ${impacts.moderate?c}],
                                        ["low", ${impacts.low?c}],
                                        ["modifier", ${impacts.modifier?c}]
                                    ] );
                                    var options = { // chart options
                                    title: "Impacts",
                                        legend: { position: 'none' },
                                        colors: ['#428bca'],
                                        width: '100%',
                                        height: 200,
                                        orientation: 'horizontal',
                                        chartArea: {
                                            left:50,
                                            top:20,
                                            width: '94%',
                                            height: '80%'
                                        },
                                        vAxis: {format: 'short'}
                                    };
                                    var chart = new google.visualization.BarChart( document.getElementById( "chartSNPImpacts" ) );
                                    chart.draw( data, options );
                                } );
                            </script>
                        </div>
                    </div>

                    <div class="row voffset" id="stats">
                        <#-- statistics -->
                        <div class="col-sm-3 col-md-3 col-md-offset-3 col-sm-offset-3">
                            <table class="table table-hover table-condensed">
                                <caption>Statistics</caption>
                                <tbody>
                                    <tr><td># SNPs</td><td class="text-center">${noSNPs}</td></tr>
                                    <tr><td><abbr title="transitions / transversions ratio">TS/TV</abrr></td><td class="text-center">${tstv.tstv}</td></tr>
                                    <tr><td><abbr title="SNPs per base pairs">Change Range</abrr></td><td class="text-center">${changeRate}</td></tr>
                                    <tr><td><abbr title="high impact">HI</abrr> SNPs</td><td class="text-center">${highImpactSNPs?size}</td></tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="col-sm-3 col-md-3">
                            <table class="table table-hover table-condensed">
                                <caption>Genetic Effects</caption>
                                <tbody>
                                    <tr><td>Start Lost</td><td class="text-center">${effects.startLost}</td></tr>
                                    <tr><td>Stop Gained</td><td class="text-center">${effects.stopGained}</td></tr>
                                    <tr><td>Stop Lost</td><td class="text-center">${effects.stopLost}</td></tr>
                                    <tr><td>Synonymous Variant</td><td class="text-center">${effects.synonymousVariant}</td></tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="row voffset" id="stats">
                        <#-- statistics -->
                        <div class="col-md-10">
                            <h2><small>High Impact SNPs</small></h2>
                            <table id="hisnps" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th class="text-center">Contig</th>
                                        <th class="text-center">Gene</th>
                                        <th class="text-center">Position</th>
                                        <th class="text-center"><abbr title="base in reference">Reference</abbr></th>
                                        <th class="text-center"><abbr title="base in sample">Sample</abbr></th>
                                        <th class="text-center">Coverage</th>
                                        <th class="text-center"><abbr title="mean quality [PHRED 33]">Mean Qual</abbr></th>
                                        <th class="text-center">Effect</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#list highImpactSNPs as hiSNP>
                                    <tr>
                                        <td>${( hiSNP?index + 1 )}</td>
                                        <td class="text-center">${hiSNP.contig}</td>
                                        <td class="text-center">${hiSNP.gene}</td>
                                        <td class="text-center">${hiSNP.pos}</td>
                                        <td class="text-center">${hiSNP.ref}</td>
                                        <td class="text-center">${hiSNP.alt}</td>
                                        <td class="text-center">${hiSNP.cov}</td>
                                        <td class="text-center">${hiSNP.meanQual}</td>
                                        <td class="text-center">${hiSNP.effect}</td>
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
