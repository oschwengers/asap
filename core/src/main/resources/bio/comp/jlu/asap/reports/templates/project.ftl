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
                    <h1 class="page-header">Project Meta Information</h1>

                    <#-- content start -->

                    <p>&nbsp;</p>
                    <div class="panel panel-default">
                        <div class="panel-heading">Name</div>
                        <div class="panel-body">${project.name}</div>
                    </div>

                    <p>&nbsp;</p>
                    <div class="panel panel-default">
                        <div class="panel-heading">Description</div>
                        <div class="panel-body">${project.description}</div>
                    </div>

                    <p>&nbsp;</p>
                    <div class="panel panel-default">
                        <div class="panel-heading">Path</div>
                        <div class="panel-body">${project.path}</div>
                    </div>

                    <#-- content end -->

                </div>
            </div>
        </div>
        <#include "commons/footer.ftl">
    </body>
</html>
