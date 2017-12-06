<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta.ftl">

        <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.17/d3.min.js"></script>

        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.11/c3.min.css">
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.11/c3.min.js"></script>

	<link href="css/datatables.min.css" rel="stylesheet">
        <script src="js/datatables.min.js"></script>

        <script>
            $(document).ready(function() {
                $('#mlst').DataTable( {
                    paging:   true,
                    ordering: true,
                    info:     true,
		    dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
                    buttons: [
                        {
                            extend: 'csv',
                            text: 'csv',
                            filename: 'mlst',
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
                      <li class="active">Multi Locus Sequence Typing</li>
                      <!-- trigger help-modal -->
                      <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal"></i>
		    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                      <div class="modal-dialog" role="document">
                        <div class="modal-content">
                          <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h2 class="modal-title" id="myModalLabel">ASAP multi locus sequence typing overview</h2>
                          </div>
                          <div class="modal-body">
                            <p>
                              <code>MLST</code> is a typing method for closely related bacterial strains within a species. Therefore, genomes are blasted
                              against public databases containing 5 to 7 thoroughly selected loci for each typed organism. Each combination
                              of alleles determines a unique sequence type. ASA³P uses a proprietary implementation based on <code>BLASTn</code>                  and the public database <code>PubMLST</code>. If a genome contains exactly one reference loci set the classification
                              was successful. Otherwise, the most similar reference is shown in case there were sufficient matches.</p>

                            <h3>Interactive donut chart</h3>

                            <p>The distribution of the different Sequence Types, Clonal Clusters and Lineages are displayed.</p>

                            <h3>Interactive data table</h3>

                            <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                  function (top right of the table) to display only genomes that contain the search term in any of their
                              table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of the
                              table. In green the found classification elements are displayed.</p>

                            <h3>Downloads</h3>

                            <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                              and sorting are contained in the downloaded file).</p>

                            <h3>Links</h3>

                            <ul>
                              <li><a href="https://pubmlst.org/general.shtml">MLST</a>; R. Urwin &amp; M.C. Maiden, 2003, Multi-locus sequence
                                typing: a tool for global epidemiology. Trends Microbiol., 11, 479-487. <a href="https://www.ncbi.nlm.nih.gov/pubmed/14557031">PubMed</a>.</li>

                              <li><a href="https://pubmlst.org/">PubMLST</a>; Database.</li>
                            </ul>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong>Alleles</strong>: Contiguous nucleotide sequence 350 to 600 base pairs in length of a housekeeping
                                gene fragment used in MLST analysis.</li>

                              <li><strong>Clonal Cluster</strong>: Group of related sequence types.</li>

                              <li><strong>Genome</strong>: Name of the processed genome.</li>

                              <li><strong>Lineage</strong>: Members of particular clonal complexes.</li>

                              <li><strong>Scheme</strong>: Group of bacterial variants.</li>

                              <li><strong>Sequence Type</strong>: Unique combination of MLST allele designations used in an MLST scheme.</li>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableFailed" href="#">${steps.failed?size} Failed MLST Classification<#if (steps.failed?size>1)>s</#if></a></h3>
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
                                    <h3 class="panel-title"><a data-toggle="collapse" data-target="#stepTableSkipped" href="#" class="collapsed">${steps.skipped?size} Skipped MLST Classification<#if (steps.skipped?size>1)>s</#if></a></h3>
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

                    <div class="row" id="stats">
                        <div class="col-md-4">
                        <#if profileSTs?has_content>
                            <div id="stChart"></div>
                            <script>
                                var chart = c3.generate( {
                                    bindto: '#stChart',
                                    data: {
                                        columns: [
                                            <#list profileSTs as st, no>
                                                [ '${st}', ${no?c} ],
                                            </#list>
                                        ],
                                        type : 'donut'
                                    },
                                    donut: {
                                        title: "Sequence Types"
                                    }
                                } );
                            </script>
                        </#if>
                        </div>
                        <div class="col-md-4">
                        <#if profileCCs?has_content>
                            <div id="ccChart"></div>
                            <script>
                                var chart = c3.generate( {
                                    bindto: '#ccChart',
                                    data: {
                                        columns: [
                                            <#list profileCCs as cc, no>
                                                [ '${cc}', ${no?c} ],
                                            </#list>
                                        ],
                                        type : 'donut'
                                    },
                                    donut: {
                                        title: "Clonal Clusters"
                                    }
                                } );
                            </script>
                        </#if>
                        </div>
                        <div class="col-md-4">
                        <#if profileLineages?has_content>
                            <div id="llChart"></div>
                            <script>
                                var chart = c3.generate( {
                                    bindto: '#llChart',
                                    data: {
                                        columns: [
                                            <#list profileLineages as ll, no>
                                                [ '${ll}', ${no?c} ],
                                            </#list>
                                        ],
                                        type : 'donut'
                                    },
                                    donut: {
                                        title: "Lineages"
                                    }
                                } );
                            </script>
                        </#if>
                        </div>
                    </div>

                <#if steps.finished?has_content>
                    <div class="row voffset" id="stats">
                        <div class="col-md-12">
                            <table id="mlst" class="table table-hover table-condensed">
                                <thead>
                                    <tr>
                                        <th><span class="glyphicon glyphicon-barcode"></span></th>
                                        <th>Genome</th>
                                        <th class="text-center">Scheme</th>
                                        <th class="text-center">Sequence Type</th>
                                        <th class="text-center">Alleles</th>
                                        <th class="text-center">Clonal Cluster</th>
                                        <th class="text-center">Lineage</th>
                                        </tr>
                                    </thead>
                                <tbody>
                            <#list steps.finished as step>
                                <#if step.mlst.perfect?has_content || step.mlst.related?has_content >
                                    <tr>
                                        <td>${step.genome.id}</td>
                                        <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                        <td class="text-center">
                                            <#list step.mlst.perfect as st><span class="label label-success">${st.scheme}</span><br></#list>
                                            <#list step.mlst.related as st><span class="label label-warning">${st.scheme}</span><br></#list>
                                        </td>
                                        <td class="text-center">
                                            <#list step.mlst.perfect as st><span class="label label-success">${st.st}</span><br></#list>
                                            <#list step.mlst.related as st><span class="label label-warning">~${st.st}</span><br></#list>
                                        </td>
                                        <td class="text-center">
                                            <#list step.mlst.perfect as st><#list st.alleles as gene, allele><span class="label label-success">${gene}(${allele})</span>&nbsp;</#list><br></#list>
                                            <#list step.mlst.related as st>
                                                <#list st.alleles as gene, allele>
                                                    <span class="label label-${st.mismatches?seq_contains(gene)?string("danger", "success")}">${gene}(${st.mismatches?seq_contains(gene)?string("~", "")}${allele})</span>&nbsp;
                                                </#list><br>
                                            </#list>
                                        </td>
                                        <td class="text-center">
                                            <#list step.mlst.perfect as st><#if st.cc != '-'><span class="label label-success">${st.cc}</span><#else>-</#if><br></#list>
                                            <#list step.mlst.related as st><#if st.cc != '-'><span class="label label-warning">${st.cc}</span><#else>-</#if><br></#list>
                                        </td>
                                        <td class="text-center">
                                            <#list step.mlst.perfect as st><#if st.lineage != '-'><span class="label label-success">${st.lineage}</span><#else>-</#if><br></#list>
                                            <#list step.mlst.related as st><#if st.lineage != '-'><span class="label label-warning">${st.lineage}</span><#else>-</#if><br></#list>
                                        </td>
                                    </tr>
                                <#else>
                                    <tr class="bg-warning">
                                        <td>${step.genome.id}</td>
                                        <td>${project.genus[0]}. ${step.genome.species} ${step.genome.strain}</td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                    </tr>
                                </#if>
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
