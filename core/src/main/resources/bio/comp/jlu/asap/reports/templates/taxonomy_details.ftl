<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta_sub.ftl">

        <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>

	<link href="../css/datatables.min.css" rel="stylesheet">
        <script src="../js/datatables.min.js"></script>
        <script>
            $(document).ready(function() {
                $('#kmerTable').DataTable( {
                    paging:   true,
                    ordering: true,
                    order: [[ 1, "desc" ]],
                    info:     true,
                    columnDefs: [
                        { orderable: false, targets: [3] }
                    ],
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'kmer-${project.genus[0]}_${genome.species}_${genome.strain}',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3 ]
                            }
                        }
                    ]
                } );
                $('#rRnaTable').DataTable( {
                    paging:   true,
                    ordering: true,
                    order: [[ 1, "desc" ]],
                    info:     true,
                    columnDefs: [
                        { orderable: false, targets: [3] }
                    ],
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'rrna-${project.genus[0]}_${genome.species}_${genome.strain}',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3 ]
                            }
                        }
                    ]
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
                        <li><a href="../taxonomy.html">Taxonomy</a></li>
                        <li class="active dropdown"><a class="dropdown-toggle" data-toggle="dropdown" href="#">${project.genus[0]}. ${genome.species} ${genome.strain} <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                                <li><a href="#kmerRow">Kmer</a></li>
                                <li><a href="#rrnaRow">16S</a></li>
                                <li><a href="#aniRow">ANI</a></li>
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
                            <h2 class="modal-title" id="myModalLabel">ASA&#179;P taxonomy detail</h2>
                          </div>
                          <div class="modal-body">
                            <h3>Interactive phylogeny visualization</h3>

                            <p>The height of the <code>phylogenetic levels</code> symbolizes the number of contigs classified
                              as such. The number of classified contigs may decreases with classification depth. On mouse
                              over the current and the next lower phylogenetic level together with the number of contigs
                              classified (weight) is displayed.</p>

                            <h5>Kmer / ANI classifications</h5>

                            <p>Taxonomy was calculated based on kmer profiles and ANI values.</p>

                            <h5>16S rRNA classifications</h5>

                            <p>Taxonomy was calculated based on 16S rRNAs.</p>

                            <h3>Interactive data table features</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header.
                              Use the <code>Search</code> function (top right of the table) to display only genomes that
                              contain the search term in any of their table fields. The <code>number of entries</code> displayed
                              per page can be chosen on the top left of the table. Mouse over on underlined table headers
                              to display further information on it.</p>

                            <h5>Kmer / ANI classifications</h5>

                            <p>The results from Mash / ANI are displayed.</p>

                            <h5>16S rRNA classifications</h5>

                            <p>Contains 16S rRNA classification results for all detected 16S sequences based on highest scoring 16S
                              RNA.</p>

                            <h3>Reference ANIs</h3>

                            <p>Table of reference genomes and their percent average nucleotide identity and percentage of conserved
                              DNA.</p>

                            <h3>Downloads</h3>

                            <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv
                              button (search and sorting are contained in the downloaded file).</p>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong>ANI [%]</strong>: Percent average nucleotide identity. Based on the ANI publication
                                the sequenced genome is split into 1020 bp fragments which are compared against the reference
                                (in our approach Nucmer was used instead of blastN). For the calculation the length of the
                                fragments with less than 30% non identities and an alignment length higher than 70% are summed
                                and divided by the total length of the sequenced genome.</li>

                              <li><strong>Classification</strong>: Deepest phylogenetic classification level for a single or
                                group of contigs/16S RNAs.</li>

                              <li><strong>Conserved DNA [%]</strong>: Percent conserved DNA. Based on the ANI publication the
                                sequenced genome is split into 1020 bp fragments which are compared against the reference
                                (in our approach Nucmer was used instead of blastN). For the calculation the length of the
                                fragments that matched with 90% sequence identity or higher are summed and divided by the
                                total length of the sequenced genome.</li>

                              <li><strong>Contigs [#]</strong>: Number of contigs that have been identified to this phylogenetic
                                level depth.</li>

                              <li><strong>Contigs [%]</strong>: Percentage out all contigs that have been identified to this
                                phylogenetic level depth.</li>

                              <li><strong>Hits [#]</strong>: Number of 16S RNAs in the analysed genome that match this 16S RNA
                                database entry.</li>

                              <li><strong>Hits [%]</strong>: Percentage of all 16S RNAs in the analysed genome that match this
                                16S RNA database entry.</li>

                              <li><strong>Linage</strong>: List of phylogenetic levels this particular level and the according
                                contigs are included.</li>

                              <li><strong>Reference</strong>: Accession of the reference genome.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                    <#-- content start -->

                    <!-- kmer -->
                <#if kmer.classification?has_content>
                    <div class="row voffset">
                        <div class="col">
                            <h2><small>Kmer / ANI Classifications</small></h2>
                            <table id="kmerTable" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th class="text-center">Classification</th>
                                        <th class="text-center"><abbr title="ANI * conserved DNA">Rank</abbr></th>
                                        <th class="text-center">Mash distance [#]</th>
                                        <th class="text-center"><abbr title="Average Nucleotide Identity">ANI</abbr> [%]</th>
                                        <th class="text-center">Conserved DNA [%]</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#list kmer.hits as hit>
                                    <tr <#if hit.isClassifier>class="info"</#if>>
                                        <td class="text-center"><a href="http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=${hit.taxonId}" target="_blank">${hit.classification}</a></td>
                                        <td class="text-center">${((hit.ani*hit.conservedDNA)?string["0.000"])}</td>
                                        <td class="text-center">${hit.dist}</td>
                                        <td class="text-center <#if (hit.ani >= 0.95)>success<#else>danger</#if>">${((hit.ani*100)?string["0.00"])}</td>
                                        <td class="text-center <#if (hit.conservedDNA >= 0.69)>success<#else>danger</#if>">${((hit.conservedDNA*100)?string["0.00"])}</td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                <#else>
                    <div class="row voffset">
                        <div class="col">
                            <div class="bs-callout bs-callout-warning"><h4>Kmers could not be classified!</h4><p>ASA&#179;P could not find any significant kmer classification for this strain.</p></div>
                        </div>
                    </div>
                </#if>


                    <!-- 16S -->
                <#if rrna.classification?has_content>
                    <div id="rrnaRow" class="row voffset">
                        <div class="col">
                            <h2><small>16S rRNA Classifications</small></h2>
                            <div id="rRnaChart"></div>
                            <script type="text/javascript">
                                google.charts.load( "current", {packages:["sankey"]} );
                                google.charts.setOnLoadCallback( function () {
                                    const data = new google.visualization.DataTable();
                                        data.addColumn('string', 'From');
                                        data.addColumn('string', 'To');
                                        data.addColumn('number', 'Weight');
                                        data.addRows( [
                                        <#list plots.sankeyRrna as link>
                                            [ '${link.from}', '${link.to}', ${link.weight?c} ],
                                        </#list>
                                        ] );
                                    let options = {
                                        height: 400,
                                        sankey: {
                                            iterations: 80,
                                            node: {
                                                width: 2,
                                                nodePadding: 20
                                            }
                                        }
                                    };
                                    const chart = new google.visualization.Sankey( document.getElementById( 'rRnaChart' ) );
                                        chart.draw( data, options );
                                } );
                            </script>
                        </div>
                    </div>
                    <div class="row voffset">
                        <div class="col">
                            <table id="rRnaTable" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th class="text-center">Classification</th>
                                        <th class="text-center">Hits [#]</th>
                                        <th class="text-center">Hits [%]</abbr></th>
                                        <th class="text-center">Lineage</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#list rrna.lineages as tax>
                                    <tr <#if tax?index==0>class="info"</#if>>
                                        <td class="text-center"><a href="http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?name=${tax.classification?replace(" ", "+")}" target="_blank">${tax.classification}</a></td>
                                        <td class="text-center">${tax.freq}</td>
                                        <td class="text-center">${(tax.freq/rrna.hits*100)?string["0.0"]}</td>
                                        <td class="text-center"><#list tax.lineage as taxon><span class="label label-default">${taxon}</span> </#list></td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                <#else>
                    <div class="row voffset">
                        <div class="col">
                            <div class="bs-callout bs-callout-warning"><h4>16S rRNA could not be classified!</h4><p>ASA&#179;P either did not find a proper 16S rRNA sequence or could not classifiy it.</p></div>
                        </div>
                    </div>
                </#if>


                        <!-- ANI -->
                    <div id="aniRow" class="row voffset">
                        <div class="col-md-6">
                            <h2><small>Reference <abbr title="Average Nucleotide Identity">ANI</abbr>s</small></h2>
                            <table id="refBased" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th>Reference</th>
                                        <th class="text-center"><abbr title="ANI * conserved DNA">Rank</abbr></th>
                                        <th class="text-center"><abbr title="Average Nucleotide Identity">ANI</abbr> [%]</th>
                                        <th class="text-center">Conserved DNA [%]</th>
                                        </tr>
                                    </thead>
                                <tbody>
                                <#list ani.all as ani>
                                    <tr>
                                        <td>${ani.reference}</td>
                                        <td class="text-center">${((ani.ani*ani.conservedDNA)?string["0.000"])}</td>
                                        <td class="text-center <#if (ani.ani>=0.95)>success<#else>danger</#if>">${((ani.ani*100)?string["0.00"])}</td>
                                        <td class="text-center <#if (ani.conservedDNA>=0.69)>success<#else>danger</#if>">${((ani.conservedDNA*100)?string["0.00"])}</td>
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
