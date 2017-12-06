<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta_sub.ftl">

        <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.0/d3.min.js"></script>
	<script src="https://cdn.rawgit.com/novus/nvd3/v1.8.1/build/nv.d3.min.js"></script>

        <style>
            #chartCont svg {
                height: 200px;
                width: 800px;
            }
        </style>

    </head>
    <body>
        <#include "commons/header_sub.ftl">
        <div class="container-fluid">
            <div class="row">
                <#include "commons/menu_sub.ftl">

                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

                    <ol class="breadcrumb">
                        <li><a href="../index.html">Dashboard</a></li>
                        <li><a href="../qc.html">Quality Control</a></li>
                        <li class="active dropdown"><a class="dropdown-toggle" data-toggle="dropdown" href="#">${project.genus[0]}. ${genome.species} ${genome.strain} <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                                <li><a href="#basic">Basic Statistics</a></li>
                                <li><a href="#pbq">Per Base Quality</a></li>
                                <li><a href="#psq">Per Sequence Qualities</a></li>
                                <li><a href="#pbsc">Per Base Sequence Contents</a></li>
                                <li><a href="#psgcc">Per Sequence GC Contents</a></li>
                                <li><a href="#pbnc">Per Base N Contents</a></li>
                                <li><a href="#sld">Sequence Length Distributions</a></li>
                                <li><a href="#kp">Kmer Profiles</a></li>
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
                            <h2 class="modal-title" id="myModalLabel">ASAP quality control detail</h2>
                          </div>
                          <div class="modal-body">
                            <p>Contains several comparisons of read sets before (Raw) and after quality control (QC) generated
                              by <code>Fast QC</code>. For paired end sequenced reads the comparison consists of four data
                              sets. Forward and reverse reads, before and after quality control. Select a comparison by clicking
                              on the genome name (top middle) to open the drop down menu.</p>

                            <h3>Table raw</h3>

                            <p>Displays the properties of the raw data, including <code>File</code> names, the <code># Reads</code>,
                              read <code>Lengths</code>, <code>Quality</code> and <code>GC</code> percentage.</p>

                            <h3>Table QC</h3>

                            <p>Displays the properties of data after quality control, including <code>File</code> names, the
                              <code># Reads</code>, read <code>Lengths</code>, <code>Quality</code> and <code>GC</code> percentage.</p>

                            <h3>Boxplot potential contaminations [%]</h3>

                            <p>The percentage of <code>reads that could not be mapped to the reference</code> but to different
                              contamination targets is shown per target. The different targets include human, mouse PhiX
                              and vectors.</p>

                            <h3>Interactive diagram groups</h3>

                            <p>The first diagram of each quartet refers to the forward reads of raw data, the second to forward
                              quality controlled data, the third to reverse reads of raw data and the fourth to reverse reads
                              of quality controlled data. Via mouse over on the diagram the according file name is displayed.</p>

                            <h4>Per base qualities</h4>

                            <p>Diagrams with the <code>quality scores across all bases</code>. On the x-axis the base position
                              in the reads is displayed. On the y-axis the <code>Quality</code> as PHRED score is shown.</p>

                            <h4>Per sequence qualities</h4>

                            <p>Diagrams with the <code>quality score distribution over all sequences</code>. On the x-axis the
                              mean sequence <code>Quality</code> as PHRED score of a read is shown. On the y-axis the number
                              of reads is display.</p>

                            <h5>Per base sequence contents</h5>

                            <p>Diagrams with the <code>sequence content across all bases</code>. On the x-axis the base position
                              in the reads is displayed. On the y-axis the percentage of each base (A, C, G, T) across all
                              reads is displayed.</p>

                            <h4>Per sequence GC contents</h4>

                            <p>Diagrams with the <code>GC distribution over all sequences</code>. The red graph shows the GC
                              count per read, the blue graph shows the theoretical distribution. On the x-axis the mean GC
                              content of the reads is display. On the y-axis the number of reads is display.</p>

                            <h4>Per base N contents</h4>

                            <p>Diagrams with the <code>N content across all bases</code>. On the x-axis the base position in
                              the reads is displayed. On the y-axis the percentage of bases characterised as 'N' (not assignable)
                              is displayed.</p>

                            <h4>Sequence length distributions</h4>

                            <p>Diagrams with the <code>distribution of sequence lengths over all sequences</code>. On the x-axis
                              the sequence lengths of the reads are displayed. On the y-axis the number of reads is displayed.</p>

                            <h4>Kmer profiles</h4>

                            <p>Diagrams with the <code>log2 ratio from observations to expected kmers</code>. The six kmers
                              with the highest log2 obs/exp are displayed. On the x-axis the base position in the reads is
                              display. On the y-axis the log2 ratio from observations to expected kmers is displayed.</p>

                            <h3>Glossary</h3>

                            <ul>
                              <li><strong>GC</strong>: GC content in percent.</li>

                              <li><strong>Length</strong>: Minimal/ mean/ maximal read length for this particular file.</li>

                              <li><strong>Potential Contaminations</strong>: Read percentage of potential contaminations. Based
                                on a 10% random subset mapping against a contamination references data base (e.g. containing
                                phiX sequences).</li>

                              <li><strong>Quality</strong>: Minimal/ mean/ maximal PHRED score of sequenced reads for this particular
                                genome (error probability; PHRED 20: 1 in 100; PHRED 30: 1 in 1000).</li>

                              <li><strong># Reads</strong>: Number of sequenced reads for this particular file.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                    <#-- content start -->

                    <div id="basic" class="row">
                        <div class="col-md-6">
                            <table class="table table-striped">
                                <caption>Raw</caption>
                                <thead>
                                    <tr>
                                        <th class="col-md-6">File</th>
                                        <th class="text-center"># Reads</th>
                                        <th class="text-center"><abbr title="[min / max]">Lengths</abbr></th>
                                        <th class="text-center"><abbr title="PHRED score [min / mean / max]">Quality</abbr></th>
                                        <th class="text-center"><abbr title="GC content [%]">GC</abbr></th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#list rawReads as read>
                                    <tr>
                                        <td class="col-md-6">${read.file}</td>
                                        <td class="text-center">${read.noReads}</td>
                                        <td class="text-center">${read.readLengths.min} / ${read.readLengths.mean?string["0.#"]} / ${read.readLengths.max}</td>
                                        <td class="text-center">${read.qual.min?round} / ${read.qual.mean?string["0.#"]} / ${read.qual.max?round}</td>
                                        <td class="text-center">${read.gc}</td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                        <div class="col-md-6">
                            <table class="table table-striped">
                                <caption>QC</caption>
                                <thead>
                                    <tr>
                                        <th class="col-md-6">File</th>
                                        <th class="text-center"># Reads</th>
                                        <th class="text-center"><abbr title="[min / mean / max]">Lengths</abbr></th>
                                        <th class="text-center"><abbr title="PHRED score [min / mean / max]">Quality</abbr></th>
                                        <th class="text-center"><abbr title="GC content [%]">GC</abbr></th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#list qcReads as read>
                                    <tr>
                                        <td class="col-md-6">${read.file}</td>
                                        <td class="text-center">${read.noReads}</td>
                                        <td class="text-center">${read.readLengths.min} / ${read.readLengths.mean?string["0.#"]} / ${read.readLengths.max}</td>
                                        <td class="text-center">${read.qual.min?round} / ${read.qual.mean?string["0.#"]} / ${read.qual.max?round}</td>
                                        <td class="text-center">${read.gc}</td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>


                    <!-- contamination figure -->
                    <div class="row voffset" id="charts">
                        <div class="col-md-12">
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h3 class="panel-title">Potential Contaminations [%]</h3>
                                </div>
                                <div id="chartCont"><svg></svg></div>
                                <script type="text/javascript">
                                    nv.addGraph( function() {
                                        var chart = nv.models.discreteBarChart()
                                            .x( function(d) { return d.label } )
                                            .y( function(d) { return d.value } )
                                            .showValues( true );
                                        d3.select( '#chartCont svg' )
                                            .datum(
                                                [ {
                                                    key: "bla",
                                                    values: [
                                                        <#list contaminations.references?values as ref>
                                                            { "label": "${ref.name}", "value": ${ref.noPotentialContaminations?c}/${ref.noReads?c}*100 },
                                                        </#list>
                                                    ]
                                                } ]
                                            )
                                            .call( chart );
                                        return chart;
                                    } );
                                </script>
                            </div>
                        </div>
                    </div>


                    <!-- base quality figures -->
                    <div id="pbq" class="row voffset">
                        <div class="col-md-12">
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h3 class="panel-title">Per Base Qualities</h3>
                                </div>
                                <div class="panel-body">
                                <#list qcReads as read>
                                    <div class="row">
                                        <div class="col-md-5">
                                            <img class="img-responsive thumbnail" src="${genomeName}/raw/${read.fileName}/per_base_quality.png" data-toggle="tooltip" title="Raw: per base quality: ${read.file}">
                                        </div>
                                        <div class="col-md-5 col-md-offset-1">
                                            <img class="img-responsive thumbnail" src="${genomeName}/qc/${read.fileName}/per_base_quality.png" data-toggle="tooltip" title="QC: per base quality: ${read.file}">
                                        </div>
                                    </div>
                                </#list>
                                </div>
                            </div>
                        </div>
                    </div>


                    <!-- sequence quality figures -->
                    <div id="psq" class="row voffset">
                        <div class="col-md-12">
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h3 class="panel-title">Per Sequence Qualities</h3>
                                </div>
                                <div class="panel-body">
                                <#list qcReads as read>
                                    <div class="row">
                                        <div class="col-md-5">
                                            <img class="img-responsive thumbnail" src="${genomeName}/raw/${read.fileName}/per_sequence_quality.png" data-toggle="tooltip" title="Raw: per sequence quality: ${read.file}">
                                        </div>
                                        <div class="col-md-5 col-md-offset-1">
                                            <img class="img-responsive thumbnail" src="${genomeName}/qc/${read.fileName}/per_sequence_quality.png" data-toggle="tooltip" title="QC: per sequence quality: ${read.file}">
                                        </div>
                                    </div>
                                </#list>
                                </div>
                            </div>
                        </div>
                    </div>


                    <!-- per base sequence content figures -->
                    <div id="pbsc" class="row voffset">
                        <div class="col-md-12">
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h3 class="panel-title">Per Base Sequence Contents</h3>
                                </div>
                                <div class="panel-body">
                                <#list qcReads as read>
                                    <div class="row">
                                        <div class="col-md-5">
                                            <img class="img-responsive thumbnail" src="${genomeName}/raw/${read.fileName}/per_base_sequence_content.png" data-toggle="tooltip" title="Raw: per base content: ${read.file}">
                                        </div>
                                        <div class="col-md-5 col-md-offset-1">
                                            <img class="img-responsive thumbnail" src="${genomeName}/qc/${read.fileName}/per_base_sequence_content.png" data-toggle="tooltip" title="QC: per base content: ${read.file}">
                                        </div>
                                    </div>
                                </#list>
                                </div>
                            </div>
                        </div>
                    </div>


                    <!-- per sequence GC content figures -->
                    <div id="psgcc" class="row voffset">
                        <div class="col-md-12">
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h3 class="panel-title">Per Sequence GC Contents</h3>
                                </div>
                                <div class="panel-body">
                                <#list qcReads as read>
                                    <div class="row">
                                        <div class="col-md-5">
                                            <img class="img-responsive thumbnail" src="${genomeName}/raw/${read.fileName}/per_sequence_gc_content.png" data-toggle="tooltip" title="Raw: per sequence GC content: ${read.file}">
                                        </div>
                                        <div class="col-md-5 col-md-offset-1">
                                            <img class="img-responsive thumbnail" src="${genomeName}/qc/${read.fileName}/per_sequence_gc_content.png" data-toggle="tooltip" title="QC: per sequence GC content: ${read.file}">
                                        </div>
                                    </div>
                                </#list>
                                </div>
                            </div>
                        </div>
                    </div>


                    <!-- per base N content figures -->
                    <div id="pbnc" class="row voffset">
                        <div class="col-md-12">
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h3 class="panel-title">Per Base N Contents</h3>
                                </div>
                                <div class="panel-body">
                                <#list qcReads as read>
                                    <div class="row">
                                        <div class="col-md-5">
                                            <img class="img-responsive thumbnail" src="${genomeName}/raw/${read.fileName}/per_base_n_content.png" data-toggle="tooltip" title="Raw: per base N content: ${read.file}">
                                        </div>
                                        <div class="col-md-5 col-md-offset-1">
                                            <img class="img-responsive thumbnail" src="${genomeName}/qc/${read.fileName}/per_base_n_content.png" data-toggle="tooltip" title="QC: per base N content: ${read.file}">
                                        </div>
                                    </div>
                                </#list>
                                </div>
                            </div>
                        </div>
                    </div>


                    <!-- sequence length distribution figures -->
                    <div id="sld" class="row voffset">
                        <div class="col-md-12">
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h3 class="panel-title">Sequence Length Distributions</h3>
                                </div>
                                <div class="panel-body">
                                <#list qcReads as read>
                                    <div class="row">
                                        <div class="col-md-5">
                                            <img class="img-responsive thumbnail" src="${genomeName}/raw/${read.fileName}/sequence_length_distribution.png" data-toggle="tooltip" title="Raw: sequence length distribution: ${read.file}">
                                        </div>
                                        <div class="col-md-5 col-md-offset-1">
                                            <img class="img-responsive thumbnail" src="${genomeName}/qc/${read.fileName}/sequence_length_distribution.png" data-toggle="tooltip" title="QC: sequence length distribution: ${read.file}">
                                        </div>
                                    </div>
                                </#list>
                                </div>
                            </div>
                        </div>
                    </div>


                    <!-- kmer profile figures -->
                    <div id="kp" class="row voffset">
                        <div class="col-md-12">
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h3 class="panel-title">Kmer Profiles</h3>
                                </div>
                                <div class="panel-body">
                                <#list qcReads as read>
                                    <div class="row">
                                        <div class="col-md-5">
                                            <img class="img-responsive thumbnail" src="${genomeName}/raw/${read.fileName}/kmer_profiles.png" data-toggle="tooltip" title="Raw: kmer profile: ${read.file}">
                                        </div>
                                        <div class="col-md-5 col-md-offset-1">
                                            <img class="img-responsive thumbnail" src="${genomeName}/qc/${read.fileName}/kmer_profiles.png" data-toggle="tooltip" title="QC: kmer profile: ${read.file}">
                                        </div>
                                    </div>
                                </#list>
                                </div>
                            </div>
                        </div>
                    </div>


                </div>
            </div>
        </div>
    </body>
</html>
