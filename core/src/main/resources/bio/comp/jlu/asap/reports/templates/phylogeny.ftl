<!DOCTYPE html>
<html>
    <head>
    <#include "commons/meta.ftl">

        <script src="js/phylocanvas-asap.js"></script>
    </head>
    <body>
    <#include "commons/header.ftl">
        <div class="container-fluid">
            <div class="row">
            <#include "commons/menu.ftl">

                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

                    <ol class="breadcrumb">
                        <li><a href="index.html">Dashboard</a></li>
                        <li class="active">Phylogeny</li>
                        <!-- trigger help-modal -->
                        <i class="fa fa-question fa-3x helpBtn" data-toggle="modal" data-target="#myModal"></i>
                    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                      <div class="modal-dialog" role="document">
                        <div class="modal-content">
                          <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h2 class="modal-title" id="myModalLabel">ASA&#179;P phylogeny overview</h2>
                          </div>
                          <div class="modal-body">
                            <p>
                              Based on a consensus sequence created with BCFtools during the SNP detection ASA&#179;P uses <code>FastTreeMP</code> to calculate
                              a phylogenetic tree of all analyzed genomes for which reads have been provided. FastTreeMP follows an approximately-maximum-likelihood
                              approach on the nucleotide level. The resulting newick file (.nwk) contains the tree representation
                              including edge lengths and can be used for graphical display. The phylogenetic distances of the analysed
                              genomes are calculated via <code>FastTreeMP</code> based on their SNPs. Information on the runtime
                              of the analysis is provided. The calculated phylogenetic trees are displayed via <code>Phylocanvas</code>.</p>

                            <h3>Phylogenetic tree display</h3>

                            <p>A tree type (<code>rectangular</code>, <code>radial</code>, <code>circular</code>, <code>diagonal</code> and <code>hierarchical</code>)
                                can be chosen via the drop down menu. The tree can be positioned via mouse drag and drop. The zoom function is controlled via mouse wheel.
                                Via right clicks in a blank area of the diagram further display and export options show up (<code>like Export as Image</code>).
                                Via a mouseover on a tree node the number of leaves associated with this subtree is displayed.
                                Via a left click on a tree node the subtree is highlighted in blue.
                                Via a right click on a tree node additional display and export options are available
                                (including <code>Collapse/Expand Subtree</code> and <code>Export Subtree as Newick File</code>)</p>

                            <h3>Downloads</h3>

                            <p>The SNP based phylogenetic distances can be downloaded as <code>nwk</code> file under <code>Downloads</code>                      on the top right.</p>

                            <h3>Links</h3>

                            <ul>
                              <li><a href="http://www.microbesonline.org/fasttree/">FastTreeMP</a>: Price, M.N., Dehal, P.S., and Arkin, A.P.
                                  FastTree 2 -- Approximately Maximum-Likelihood Trees for Large Alignments.
                                  PLoS ONE, 2010, 5(3):e9490. doi:10.1371/journal.pone.0009490.
                                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/20224823">PubMed</a>
                              </li>

                              <li><a href="http://phylocanvas.org/">Phylocanvas</a>: Centre for Genomic Pathogen Surveillance (2016 ).
                                Interactive tree visualisation for the web.</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>

                <#-- content start -->

                <#switch status>
                    <#case "finished">
                    <div class="row">

                        <div class="col-md-2">
                            <!--dropdown to select treetype-->
                            <div class="form-group">
                                <label for="treeType">Tree Type</label>
                                <select id="treeType" class="form-control" onchange="changeLook(this.value)">
                                    <option value="rectangular">rectangular</option>
                                    <option value="radial">radial</option>
                                    <option value="circular">circular</option>
                                    <option value="diagonal">diagonal</option>
                                    <option value="hierarchical">hierarchical</option>
                                </select>
                            <!--<button id="savesvg" onclick="saveSvg()">save SVG</button>-->
                            </div>
                        </div>

                        <div class="col-md-2 col-md-offset-5">
                            <table class="table table-hover table-condensed">
                                <caption>Downloads</caption>
                                <tbody>
                                    <tr><td>SNP based tree</td><td class="text-center"><a href="./phylogeny/tree.nwk">nwk</a></td></tr>
                                </tbody>
                            </table>
                        </div>

                        <div class="col-md-3">
                            <table class="table table-hover table-condensed">
                                <caption>Runtime</caption>
                                <tbody>
                                    <tr><td>start <span class="glyphicon glyphicon-play"></span></td><td class="text-center">${runtime.start}</tr>
                                    <tr><td>end <span class="glyphicon glyphicon-stop"></span></td><td class="text-center">${runtime.end}</tr>
                                    <tr><td>duration <span class="glyphicon glyphicon-refresh"></span></td><td class="text-center">${runtime.time}</tr>
                                </tbody>
                            </table>
                        </div>

                    </div>

                    <div class="row">
                        <div id="phylocanvas" ></div>
                        <script type="application/javascript">
                            const tree = Phylocanvas.createTree( 'phylocanvas' , { // generates the tree using the newick string
                                scalebar: { // Scale-bar Plugin config options
                                    active: true, //change to true for display
                                    width: 60,
                                    height: 20,
                                    fillStyle: 'black',
                                    strokeStyle: 'black',
                                    lineWidth: 1,
                                    font: '16px Sans-serif',
                                    textBaseline: 'bottom',
                                    textAlign: 'center',
                                    digits: 2,
                                    position: {
                                        bottom: 10,
                                        left: 10,
                                    },
                                }
                            } );
                            tree.load( "${tree}" );
                            tree.setTreeType('rectangular');
                            tree.draw();

                            function changeLook(look) { // change displayed tree type
                                tree.setTreeType(look);
                            }

                            // to save SVG of phylocanvas
                            //            function saveSvg (){
                            //                var svgToSave = tree.exportSVG.getSerialisedSVG();
                            //                if(svgToSave){
                            //                    var pom = document.createElement('a');
                            //                    pom.setAttribute('href', 'data:text/xml;charset=utf-8,' + window.encodeURIComponent(svgToSave));
                            //                    pom.setAttribute('download', 'TREENAME.svg');
                            //                    var event = document.createEvent('MouseEvents');
                            //                    event.initEvent('click', true, true);
                            //                    pom.dispatchEvent(event);
                            //                }
                            //            };
                            </script>
                    </div>

                        <#break>
                    <#case "skipped">
                        <div class="well">
                            SNP Phylogeny analysis status:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <span class="label label-warning"><span
                                class="glyphicon glyphicon-minus"></span> Skipped</span>
                        </div>
                        <#break>
                    <#case "failed">
                        <div class="well">
                            SNP Phylogeny analysis status:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <span class="label label-danger"><span
                                class="glyphicon glyphicon-remove"></span> Error</span>
                        </div>
                        <#break>
                    <#default>
                        <div class="well">
                            SNP Phylogeny analysis status:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <span class="label label-info"><span
                                class="glyphicon glyphicon-retweet"></span> Running</span>
                        </div>
                </#switch>

                <#-- content end -->

                </div>
            </div>
        </div>
    </body>
</html>