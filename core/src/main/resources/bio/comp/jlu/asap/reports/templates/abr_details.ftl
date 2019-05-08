<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta_sub.ftl">

        <script>
            $(document).ready( function() {
                Array.from( document.getElementsByClassName( "abr" ) ).forEach( e => {
                    let val = parseInt( e.innerText );
                    if(val < 80) e.style.backgroundColor = "#FE6E72";
                    else if(val < 95) e.style.backgroundColor = "#FFF0A1";
                    else if(val < 98) e.style.backgroundColor = "#A1F89C";
                    else e.style.backgroundColor = "#6EEE67 ";
                } );
            } );
        </script>

        <script>
            $(document).ready( function() {
		$('.model-description').popover( {trigger: "hover", placement: "top", html: true} );
            } );
        </script>

	<link href="../css/datatables.min.css" rel="stylesheet">
        <script src="../js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {
                $('#abrGenes').DataTable( {
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
                            filename: 'abr-${project.genus[0]}_${genome.species}_${genome.strain}',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3, 4, 5, 6 ]
                            }
                        }
                    ]
                } );
            } );
            $(document).ready( function() {
                $('#abrBestPotentialGenes').DataTable( {
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
                            filename: 'abr-potential-${project.genus[0]}_${genome.species}_${genome.strain}',
                            exportOptions: {
                                columns: [ 0, 1, 2, 3, 4, 5, 7, 8 ]
                            }
                        }
                    ]
                } );
            } );
            $(document).ready( function() {
                $('#abrPotentialGenes').DataTable( {
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
                            filename: 'abr',
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
                        <li><a href="../abr.html">Antibiotic Resistances</a></li>
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
                            <h2 class="modal-title" id="myModalLabel">ASA&#179;P antibiotic resistances detail</h2>
                          </div>
                          <div class="modal-body">
                            <h3>Interactive data tables</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header.
                              Use the <code>Search</code> function (top right of the table) to display only genomes that
                              contain the search term in any of their table fields. The <code>number of entries</code> displayed
                              per page can be chosen on the top left of the table. To display <code>additional model information</code>                              mouse over a model. The 'Seq Identity' is categorised into four groups based on value. Entries
                              below 80 % sequence identity are highlighted in red, below 95 % in yellow, below and above 98 % in light green
                              and green, respectively. To display the <code>aligned sequences</code> mouse over the bit score values.
                              Mouse over on underlined terms to display further information on it.</p>

                            <h5>ABR Genes</h5>

                            <p>Provides information on the genes with a <code>perfect</code> reference match (100%) in the ABR
                              database.</p>

                            <h5>Potential ABR Genes - Best Hits</h5>

                            <p>Provides information on genes and their <code>best</code> non perfect reference ABR database
                              match (40% &lt; match &lt;=100%).</p>

                            <h5>Potential ABR Genes - All Hits</h5>

                            <p>Provides information on genes with <code>all</code> their non perfect reference ABR database
                              matches (40% &lt; match &lt;=100%).</p>

                            <h3>Links</h3>

                            <p>Click on a model to redirect to the related <code>model reference</code> in the CARD database.</p>

                            <h3>Downloads</h3>

                            <p>The table can be saved as a comma separated value (<code>csv</code>) file via a click on the csv
                              button (search and sorting are contained in the downloaded file).</p>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong>ABR Target Drugs</strong>: The drug or drug family the resistance is associated with.</li>

                              <li><strong>Bit Score</strong>: Normalized chance to find the score or a higher one of this match
                                by chance given in bit (bit score of 3 equals a chance of 2³= 8 -> 1 : 8).</li>

                              <li><strong>End</strong>: End position of this resistance gene in this genome.</li>

                              <li><strong>eValue</strong>: Expected number of alignments in the database used with a score equivalent
                                or higher than this match.</li>

                              <li><strong>Length</strong>: Length of this resistance gene in this genome.</li>

                              <li><strong>Model</strong>: Name of the resistance mechanism.</li>

                              <li><strong>Seq Identity</strong>: Percentage of identical positioned nucleotides in the alignment.</li>

                              <li><strong>Start</strong>: Start position of this resistance gene in this genome.</li>

                              <li><strong>Strand</strong>: The forward/plus strand is marked via '+' and the reverse/minus strand
                                is marked with '-'.</li>
                            </ul>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                    <#-- content start -->

                    <div class="row">
                        <div class="col-md-12">
                            <h2><small>ABR Genes</small></h2>
                            <table id="abrGenes" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th class="text-center">Model</th>
                                        <th class="text-center">Drug Class(es)</th>
                                        <th class="text-center">Target Drugs</th>
                                        <th class="text-center">Start</th>
                                        <th class="text-center">End</th>
                                        <th class="text-center">Length</th>
                                        <th class="text-center">Strand</th>
                                        </tr>
                                    </thead>
                                <tbody>
                                    <#list abr.perfect as abr_>
                                    <tr>
                                        <td class="text-center"><a href="https://card.mcmaster.ca/aro/${abr_.model.aroId}" target="_blank" class="model-description" data-toggle="popover" title="Model Information" data-content="<p>ARO-ID: ${abr_.model.aroId}</p><p>Name: ${abr_.model.name}</p><p>Description: ${abr_.model.desc}</p><p>Type: ${abr_.model.type}</p><p>Bitscore: ${abr_.model.bitScore}</p>">${abr_.model.name}</a></td>
                                        <td class="text-center">${abr_.drugClasses?join(", ")}</td>
                                        <td class="text-center">${abr_.antibiotics?join(", ")}</td>
                                        <td class="text-center">${abr_.orf.start}</td>
                                        <td class="text-center">${abr_.orf.end}</td>
                                        <td class="text-center">${abr_.orf.length}</td>
                                        <td class="text-center">${abr_.orf.strand}</td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="row voffset">
                        <div class="col-md-12">
                            <h2><small>Potential ABR Genes - Best Hits</small></h2>
                            <table id="abrBestPotentialGenes" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th class="text-center">Model</th>
                                        <th class="text-center">Drug Class(es)</th>
                                        <th class="text-center">Target Drugs</th>
                                        <th class="text-center">Start</th>
                                        <th class="text-center">End</th>
                                        <th class="text-center">Length</th>
                                        <th class="text-center">Bit Score</th>
                                        <th class="text-center">Strand</th>
                                        <th class="text-center">eValue</th>
                                        <th class="text-center"><abbr title="[%]">Seq Identity</abbr></th>
                                        </tr>
                                    </thead>
                                <tbody>
                                    <#list abr.bestAdditinalABRs as abr_>
                                    <tr>
                                        <td class="text-center"><a href="https://card.mcmaster.ca/aro/${abr_.model.aroId}" target="_blank" class="model-description" data-toggle="popover" title="Model Information" data-content="<p>ARO-ID: ${abr_.model.aroId}</p><p>Name: ${abr_.model.name}</p><p>Description: ${abr_.model.desc}</p><p>Type: ${abr_.model.type}</p><p>Bitscore: ${abr_.model.bitScore}</p>">${abr_.model.name}</a></td>
                                        <td class="text-center">${abr_.drugClasses?join(", ")}</td>
                                        <td class="text-center">${abr_.antibiotics?join(", ")}</td>
                                        <td class="text-center">${abr_.orf.start}</td>
                                        <td class="text-center">${abr_.orf.end}</td>
                                        <td class="text-center">${abr_.orf.length}</td>
                                        <td class="text-center"><abbr title="${abr_.alignment}">${abr_.bitScore?round}</abbr></a></td>
                                        <td class="text-center">${abr_.orf.strand}</td>
                                        <td class="text-center"><#if abr_.eValue==0>0<#else>${abr_.eValue?string["0E0"]}</#if></td>
                                        <td class="text-center abr">${abr_.percentSeqIdentity * 100}</td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="row voffset">
                        <div class="col-md-12">
                            <h2><small>Potential ABR Genes - All Hits</small></h2>
                            <table id="abrPotentialGenes" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th class="text-center">Model</th>
                                        <th class="text-center">Drug Class(es)</th>
                                        <th class="text-center">Target Drugs</th>
                                        <th class="text-center">Start</th>
                                        <th class="text-center">End</th>
                                        <th class="text-center">Length</th>
                                        <th class="text-center">Bit Score</th>
                                        <th class="text-center">Strand</th>
                                        <th class="text-center">eValue</th>
                                        <th class="text-center"><abbr title="[%]">Seq Identity</abbr></th>
                                        </tr>
                                    </thead>
                                <tbody>
                                    <#list abr.additional as abr_>
                                    <tr>
                                        <td class="text-center"><a href="https://card.mcmaster.ca/aro/${abr_.model.aroId}" target="_blank" class="model-description" data-toggle="popover" title="Model Information" data-content="<p>ARO-ID: ${abr_.model.aroId}</p><p>Name: ${abr_.model.name}</p><p>Description: ${abr_.model.desc}</p><p>Type: ${abr_.model.type}</p><p>Bitscore: ${abr_.model.bitScore}</p>">${abr_.model.name}</a></td>
                                        <td class="text-center">${abr_.drugClasses?join(", ")}</td>
                                        <td class="text-center">${abr_.antibiotics?join(", ")}</td>
                                        <td class="text-center">${abr_.orf.start}</td>
                                        <td class="text-center">${abr_.orf.end}</td>
                                        <td class="text-center">${abr_.orf.length}</td>
                                        <td class="text-center"><abbr title="${abr_.alignment}">${abr_.bitScore?round}</abbr></a></td>
                                        <td class="text-center">${abr_.orf.strand}</td>
                                        <td class="text-center"><#if abr_.eValue==0>0<#else>${abr_.eValue?string["0E0"]}</#if></td>
                                        <td class="text-center abr">${abr_.percentSeqIdentity * 100}</td>
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
