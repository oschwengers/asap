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
                    <h1 class="page-header">User Meta Information</h1>

                    <#-- content start -->

                    <p>&nbsp;</p>
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h3 class="panel-title">User</h3>
                        </div>
                        <div class="panel-body">
                            <p><span class="glyphicon glyphicon-user"></span>&nbsp;&nbsp;${user.name} ${user.surname}</p>
                            <p><span class="glyphicon glyphicon-envelope"></span>&nbsp;&nbsp;${user.email}</p>
                        </div>
                    </div>

                    <#-- content end -->

                </div>
            </div>
        </div>
        <#include "commons/footer.ftl">
    </body>
</html>
