<!DOCTYPE html>
<html>
    <head>
        <#include "commons/meta.ftl">
    </head>
    <body>
        <#include "commons/header.ftl">
        <div class="container-fluid">
            <div class="row">
                <#include "commons/menu.ftl">

                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

                    <ol class="breadcrumb">
                        <li><a href="index.html">Dashboard</a></li>
                        <li class="active">${report}</li>
                        <!-- trigger help-modal -->
                        <i class="fa fa-question fa-3x" data-toggle="modal" data-target="#myModal"></i>
                    </ol>

                    <!-- help-modal -->
                    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
                        <div class="modal-dialog" role="document">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                                    <h4 class="modal-title" id="myModalLabel">Help</h4>
                                </div>
                                <div class="modal-body">
                                    ...
                                </div>
                            </div>
                        </div>
                    </div>

                    <#-- content start -->

                    <p>&nbsp;</p>
                    <div class="panel panel-warning">
                        <div class="panel-heading">
                            <h3 class="panel-title">Warning</h3>
                        </div>
                        <div class="panel-body">
                            <p>Sorry, this step was skiped due to unsuitable input files or a failed precursor step!<br>
                                In case of the latter option, please, have a look at a failed precursor step.
                                If this error occures constantly, please do not hesitate to contact us!</p>
                            <p><span class="glyphicon glyphicon-th-large" title="step"></span>: &nbsp;&nbsp;${step}</p>
                        </div>
                    </div>

                    <#-- content end -->

                </div>
            </div>
        </div>
    </body>
</html>
