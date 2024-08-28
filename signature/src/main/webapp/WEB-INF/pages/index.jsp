<!DOCTYPE html>
<html ng-app="App">
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta charset="utf-8">
    <title>Indigo Signature Service</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="css/bootstrap.css" rel="stylesheet" />
    <link href="css/style.css" rel="stylesheet" />
    <!--[if IE 9]><link href="css/ie9.css" rel="stylesheet" type="text/css" /><![endif]-->

     <script type="text/javascript">
        var settings = {
            isUpload: ${isUploadDocumentsAllowed},
            signingMethod: '${signingMethod}' //verison:default
        };
    </script>
    <script type="text/javascript" src="libs/upload/angular-file-upload-shim.min.js"></script>

    <script type="text/javascript" src="libs/jquery/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="libs/angular-1.2.32/angular.min.js"></script>
    <script type="text/javascript" src="libs/angular-1.2.32/angular-resource.min.js"></script>
    <script type="text/javascript" src="libs/angular-1.2.32/angular-route.min.js"></script>
    <script type="text/javascript" src="libs/angular-1.2.32/angular-sanitize.min.js"></script>

    <script type="text/javascript" src="libs/angular-ui/ui-bootstrap-tpls-0.10.0.min.js"></script>
    <script type="text/javascript" src="libs/upload/angular-file-upload.min.js"></script>
    <script type="text/javascript" src="libs/ng-idle/angular-idle.min.js"></script>
    <script type="text/javascript" src="libs/truncate/truncate.js"></script>

	<script type="text/javascript" src="js/app.js"></script>

    <script type="text/javascript" src="js/controllers.js"></script>
    <script type="text/javascript" src="js/controllers/documentListController.js"></script>
    <script type="text/javascript" src="js/controllers/uploadDocumentDlgController.js"></script>
    <script type="text/javascript" src="js/controllers/documentInfoDlgController.js"></script>
    <script type="text/javascript" src="js/controllers/documentActionDlgController.js"></script>
    <script type="text/javascript" src="js/controllers/templateListController.js"></script>
    <script type="text/javascript" src="js/controllers/templateDeleteDlgController.js"></script>
    <script type="text/javascript" src="js/controllers/loginController.js"></script>
    <script type="text/javascript" src="js/controllers/userListController.js"></script>
    <script type="text/javascript" src="js/controllers/dialogInfoController.js"></script>

    <script type="text/javascript" src="js/directives.js"></script>
    <script type="text/javascript" src="js/filters.js"></script>
    <script type="text/javascript" src="js/services.js"></script>

    <script type="text/javascript" src="js/helpers/dataMapper.js"></script>
</head>

<body>
    <div ng-controller="mainController" ng-view class="global-wrap"></div>

    <script type="text/ng-template" id="sessionExpiredTmpl">
        <div class="modal-header">
            <h2>Session Expired<span ng-click="ok()" title="Close"></span></h2>
        </div>
        <div class="modal-body">
            <p>Current session has been expired.<br>
               You will be redirected to the Login page.</p>
        </div>
        <div class="modal-footer">
            <button class="btn" ng-click="ok()">Ok</button>
        </div>
    </script>

    <div ng-include src="'views/shared/_dialogInfoTmpl.html'"></div>
</body>
</html>
