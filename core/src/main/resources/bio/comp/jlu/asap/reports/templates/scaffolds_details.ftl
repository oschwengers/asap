<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta_sub.ftl">

        <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/4.7.1/d3.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/d3-tip/0.7.1/d3-tip.js"></script>
        <script src="https://cdn.jsdelivr.net/lodash/4.17.4/lodash.js"></script>

        <script src="../js/synteny.js"></script>

    </head>
    <body>
        <#include "commons/header_sub.ftl">
        <div class="container-fluid">
            <div class="row">
                <#include "commons/menu_sub.ftl">

                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

                    <ol class="breadcrumb">
                        <li><a href="../index.html">Dashboard</a></li>
                        <li><a href="../scaffolds.html">Scaffolds</a></li>
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
                            <h2 class="modal-title" id="myModalLabel">ASA&#179;P scaffolds detail</h2>
                          </div>
                          <div class="modal-body">
                            <p>This site provides information on contig alignment and assignment to reference genome(s). The contigs of
                              the particular Whole Genome Assembly (<code>WGA</code>) are compared to each of the reference
                              genomes via Synteny plots. In order to visualize the scaffolding quality the comparison is
                              done before and after the scaffolding process.</p>

                            <h3>Basic scaffolding statistics</h3>

                            <p>Provides information on scaffolding in general and on the scaffold length.</p>

                            <h3>DNA synteny plots</h3>

                            <p>The upper synteny plot of each genome comparison displays the position of all contigs in both
                              genomes before the scaffolding process (<code>Pre Scaffolding</code>). The lower synteny plot
                              after scaffolding (<code>Post Scaffolding</code>). On the x-axis the contig position in the
                              reference genome is displayed. On the y-axis the contig position in the <code>WGA</code> is
                              displayed. Contigs referenced to the minus strand are displayed in orange the ones referenced
                              to the plus strand are displayed in blue. Mouse over a contig to receive information on its
                              name, length assigned strand as well as start and end position in the reference.</p>

                            <h3>Downloads</h3>

                            <p>The scaffolds and the generated pseudo genome can be downloaded as <code>fasta</code> on the
                              top right.</p>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong># Contigs</strong>: Number of contigs (set of overlapping DNA segments).</li>

                              <li><strong>Genome Size [Mb]</strong>: Size of the WGA in million/mega bases.</li>

                              <li><strong>N50</strong>: Given ordered contigs from longest to smallest, length of the contig
                                at 50% of the genome length.</li>

                              <li><strong>N90</strong>: Given ordered contigs from longest to smallest, length of the contig
                                at 90% of the genome length.</li>

                              <li><strong># Scaffolds</strong>: Number of scaffolds (joined, aligned and assigned contigs) after
                                polishing.</li>

                              <li><strong>WGA</strong>: Whole Genome Assembly generated via joining all sequence elements after
                                scaffolding with the sequence 'NNNNNNNNNNCTAGCTAGCTAGCNNNNNNNNNN'.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                    <#-- content start -->

                    <div class="row">

                         <!-- Assembly overview -->
                        <div class="col-sm-3 col-md-3 col-md-offset-1">
                            <table class="table table-hover table-condensed">
                                <caption>Scaffolded Genome</caption>
                                <tbody>
                                    <tr><td>Scaffolds [#]</td><td class="text-center">${scaffolds.noScaffolds}</td></tr>
                                    <tr><td>Contigs [#]</td><td class="text-center">${scaffolds.noContigs}</td></tr>
                                    <tr><td>Genome Size [Mb]</td><td class="text-center">${scaffolds.length}</td></tr>
                                    <tr><td>N50 [kb]</td><td class="text-center">${scaffolds.n50}</td></tr>
                                    <tr><td>N90 [kb]</td><td class="text-center">${scaffolds.n90}</td></tr>
                                </tbody>
                            </table>
                        </div>

                        <!-- Scaffold lengths -->
                        <div class="col-sm-3 col-md-3 col-md-offset-1">
                            <table class="table table-hover table-condensed">
                                <caption>Scaffold Lengths</caption>
                                <tbody>
                                    <tr><td>Min [bp]</td><td class="text-center">${scaffolds.lengths.min}</td></tr>
                                    <tr><td>Max [kb]</td><td class="text-center">${scaffolds.lengths.max}</td></tr>
                                    <tr><td>Mean [kb]</td><td class="text-center">${scaffolds.lengths.mean?round}</td></tr>
                                    <tr><td>Median [kb]</td><td class="text-center">${scaffolds.lengths.median}</td></tr>
                                </tbody>
                            </table>
                        </div>

                        <!-- Downloads -->
                        <div class="col-sm-2 col-md-2 col-md-offset-1 col-sm-offset-1">
                            <table class="table table-hover table-condensed">
                                <caption>Downloads</caption>
                                <tbody>
                                    <tr><td>Scaffolds</td><td class="text-center"><a href="./${genomeName}/${genomeName}.fasta">fasta</a></td></tr>
                                    <tr><td>Pseudo Genome</td><td class="text-center"><a href="./${genomeName}/${genomeName}-pseudo.fasta">fasta</a></td></tr>
                                </tbody>
                            </table>
                        </div>

                    </div>

                    <#list scaffolds.syntenies as ref>
                    <div class="row voffset">
                        <div class="col">
                            <h2><small>WGA vs. ${ref.name}</small></h2>
                            <div class="row">
                                <div class="col-md-1 col-sd-2">
                                    <h4><small>Pre Scaffolding</small></h4>
                                </div>
                                <div class="col-md-11 col-sd-10">
                                    <section id="${ref.name?replace(".", "")}-pre" class="caption" role="region">
                                    <script>
                                        $(() => {
                                            let data = ${ref.preJson}
                                            drawDotPlot( data, '#${ref.name?replace(".", "")}-pre', 'WGA - Pre Scaffolding', '${ref.name}', '${genomeName}');
                                        });
                                    </script>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-1 col-sd-2">
                                    <h4><small>Post Scaffolding</small></h4>
                                </div>
                                <div class="col-md-11 col-sd-10">
                                    <section id="${ref.name?replace(".", "")}-post" class="caption" role="region">
                                    <script>
                                        $(() => {
                                            let data = ${ref.postJson}
                                            drawDotPlot( data, '#${ref.name?replace(".", "")}-post', 'WGA - Post Scaffolding', '${ref.name}', '${genomeName}');
                                        });
                                    </script>
                                </div>
                            </div>
                        </div>
                    </div>
                    </#list>
                    <#-- content end -->

                </div>
            </div>
        </div>
    </body>
</html>
