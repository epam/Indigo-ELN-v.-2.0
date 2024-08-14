/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 *
 * This file is part of Indigo Signature Service.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
angular.module('App.controllers')
.controller('uploadDocumentDlgController', ['$scope', '$modalInstance', 'dataFactory',
                     function ( $scope, $modalInstance, dataFactory) {

    $scope.UploadModel = {
        Template: null,
        File: null,
        Mode: 1,
        UploadProgressPercent: 0,
        UploadControl: null,
        Error: null
    };

    $scope.upload = function(){
        if($scope.UploadModel.File == null){
            $scope.UploadModel.Error = "Please select file.";
            return;
        }
        if($scope.UploadModel.File.type != "application/pdf" && $scope.UploadModel.File.type != "pdf"){
            $scope.UploadModel.Error = "Wrong file type. Please select only PDF.";
            return;
        }
        if($scope.UploadModel.Template == null){
            $scope.UploadModel.Error = "Please select template.";
            return;
        }

        $scope.UploadModel.Mode = 2;
        $scope.UploadModel.UploadControl = dataFactory.uploadDocument($scope.UploadModel.File, $scope.UploadModel.Template.id,
            function(event){
                $scope.UploadModel.UploadProgressPercent = parseInt(100.0 * event.loaded / event.total);
            },
            function(document){
                $scope.UploadModel.Mode = 3;
                $scope.UploadModel.UploadControl = null;
                $scope.MODEL.documents.push(document);
                $scope.fireDocumentChangedEvent();
                //$modalInstance.close();
            },
            function(errorText){
                $scope.UploadModel.Mode = 1;
                $scope.UploadModel.Error = errorText;
            });
    };

    $scope.onFileSelect = function($files) {
        $scope.UploadModel.File = $files[0];
        $scope.UploadModel.Error = null;
      };

    $scope.abort = function(){
        if($scope.UploadModel.UploadControl != null){
            $scope.UploadModel.UploadControl.abort();
            $scope.UploadModel.UploadControl = null;
            $scope.UploadModel.Mode = 1;
        }
    };

    $scope.ok = function () {
      $modalInstance.close();
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

    var init = function(){
        if($scope.MODEL.templates && $scope.MODEL.templates.length>0){
            $scope.UploadModel.Template = $scope.MODEL.templates[0];
        }
    };

    init();
}]);