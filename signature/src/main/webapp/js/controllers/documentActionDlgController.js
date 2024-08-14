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
.controller('documentActionDlgController', ['$scope', '$modalInstance', 'dataFactory', '$modal', '$window','$timeout',
                     function ( $scope, $modalInstance, dataFactory, $modal, $window, $timeout) {

    $scope.ActionModel = {
        comment: null,
        password: null,
        File: null,
        IsValidated: false,
        Error: null,
        actionHeaderText: '',
        actionMessageText: '',
        shouldCheckVerizon: true,
        dialogVerizonInfo: null,
        isVerizonDone: false,
        checkVerizonInterval: 5000
    };

     var optDialogInfoDlg  = {
         templateUrl: 'dialogInfoTmpl',
         controller: 'dialogInfoController',
         resolve: {
             headerText: function(){
                return 'initial';
            },
             messageText: function(){
                return 'initial';
            }
         }
     };
     var optDialogVerizonDlg  = {
         templateUrl: 'dialogVerizonTmpl',
         keyboard: false,
         backdrop: 'static',
         controller: function($scope, $modalInstance, headerText, messageText, canCloseFlag){
             $scope.InfoModel = {
                 header: headerText,
                 message: messageText,
                 canClose: canCloseFlag
             };

             $scope.cancel = function () {
                 $modalInstance.dismiss('cancel');
             };
         },
         resolve: {
             headerText: function(){
                 return 'initial';
             },
             messageText: function(){
                 return 'initial';
             },
             canCloseFlag: function(){
                 return true;
             }
         }
     };

    $scope.ok = function () {
        $modalInstance.close();
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

     $scope.download = function () {
         $scope.MODEL.currentDocument.isViewed = true;
         $scope.ActionModel.Error = '';
     };

    $scope.sign = function(){
        $scope.ActionModel.IsValidated = true;
        $scope.ActionModel.Error = '';
        if($scope.ActionModel.frmAct.$invalid){
            return;
        }
        switch($scope.UI.signMethod){
            case 1:
                // Verizon
                signVerizon();
                break;
            default:
                signDefault();
                break;
        }
     };

    var signVerizon = function(){
        var documentId = $scope.MODEL.currentDocument.id;
        dataFactory.signDocumentVerizon(
            documentId,
            $scope.ActionModel.password,
            $scope.ActionModel.comment,
            function(document){
                angular.extend($scope.MODEL.currentDocument, document);
                $scope.fireDocumentChangedEvent();
                showSuccess();
            },
            function(error) {
                switch (error.code){
                    case 1:
                        $window.open(error.redirect_url);
                        $timeout(function(){
                            checkVerizonStatus(documentId);
                        }, $scope.ActionModel.checkVerizonInterval);
                        var message = "<p> You will see Verizon authentication page in few seconds. Otherwise, you can click on the link right now.</p>"
                        message += "<p><a href='"+error.redirect_url+"' target='_blank'>Open Verizon login page</a></p>";
                        message += "<p>Once you have logged in Version, please wait for signing process completion.</p>";
                        showVerizonInfo(message);
                        break;
                    default:
                        $scope.ActionModel.Error = error.text;
                        break;
                }
            }
        );
    };

    var checkVerizonStatus = function(documentId){
        var docId = documentId;

        if(!$scope.ActionModel.shouldCheckVerizon){
            return;
        }

        dataFactory.checkVerizonStatus(
            docId,
            function(document){
                angular.extend($scope.MODEL.currentDocument, document);
                $scope.fireDocumentChangedEvent();
                showSuccess();
            },
            function(error) {
                switch (error.code){
                    case 2:
                    case 4:
                        $timeout(function(){
                            checkVerizonStatus(docId);
                        }, $scope.ActionModel.checkVerizonInterval);
                        break;
                    default:
                        if($scope.ActionModel.dialogVerizonInfo){
                            $scope.ActionModel.dialogVerizonInfo.close();
                        }
                        $scope.ActionModel.Error  = error.text;
                        $scope.ActionModel.shouldCheckVerizon = false;
                        break;
                }

                if(error.code==4 && !$scope.ActionModel.isVerizonDone){
                    $scope.ActionModel.isVerizonDone = true;
                    showSignProcessDialog();
                }
            }
        );
    };

     var showSignProcessDialog = function(){
         if($scope.ActionModel.dialogVerizonInfo){
             $scope.ActionModel.dialogVerizonInfo.close();
         }
         $scope.ActionModel.shouldCheckVerizon = true;
         optDialogVerizonDlg.resolve = {
             headerText: function(){
                 return "Signing document";
             },
             messageText: function(){
                 return "Please wait until document has been signed.";
             },
             canCloseFlag: function(){
                 return false;
             }
         };
         $scope.ActionModel.dialogVerizonInfo = $modal.open(optDialogVerizonDlg);

         $scope.ActionModel.dialogVerizonInfo.result.then(function () {
             $scope.ActionModel.shouldCheckVerizon = false;
             //$modalInstance.close();
         }, function () {
             $scope.ActionModel.shouldCheckVerizon = false;
             //$modalInstance.close();
         });
     }

     var showVerizonInfo = function(message){
         optDialogVerizonDlg.resolve = {
             headerText: function(){
                 return "Authorizing by Verizon";
             },
             messageText: function(){
                 return message;
             },
             canCloseFlag: function(){
                 return true;
             }
         };
         $scope.ActionModel.dialogVerizonInfo = $modal.open(optDialogVerizonDlg);

         $scope.ActionModel.dialogVerizonInfo.result.then(function () {
             //$scope.ActionModel.shouldCheckVerizon = false;
             //$modalInstance.close();
         }, function () {
             $scope.ActionModel.shouldCheckVerizon = false;
             //$modalInstance.close();
         });
     };

    var signDefault = function(){
        showSignProcessDialog();
        dataFactory.signDocument(
            $scope.MODEL.currentDocument.id, $scope.ActionModel.password, $scope.ActionModel.comment, $scope.ActionModel.File,
            function(){},
            function(document){
                angular.extend($scope.MODEL.currentDocument, document);
                $scope.fireDocumentChangedEvent();
                showSuccess();
            },
            function(errorText){
                $scope.ActionModel.Error = errorText;
                if($scope.ActionModel.dialogVerizonInfo){
                    $scope.ActionModel.dialogVerizonInfo.close();
                }
            });
    };

    var showSuccess = function(){
        if($scope.ActionModel.dialogVerizonInfo){
            $scope.ActionModel.dialogVerizonInfo.close();
        }

        optDialogInfoDlg.resolve = {
            headerText: function(){
                return "Document was signed";
            },
            messageText: function(){
                return "Document '" + $scope.MODEL.currentDocument.name  + "' was signed successfully.";
            }
        };
        var resultInfo = $modal.open(optDialogInfoDlg);

        resultInfo.result.then(function () {
            $modalInstance.close();
        }, function () {
            $modalInstance.close();
        });

    };


    $scope.onFileSelect = function($files) {
        $scope.ActionModel.File = $files[0];
      };

    $scope.reject = function(){
        $scope.ActionModel.IsValidated = true;
        $scope.ActionModel.Error = '';
        if($scope.ActionModel.frmAct.$invalid){
            return;
        }
        dataFactory.rejectDocument($scope.MODEL.currentDocument.id, $scope.ActionModel.comment, function(document){
            angular.extend($scope.MODEL.currentDocument, document);

            $scope.fireDocumentChangedEvent();
                optDialogInfoDlg.resolve = {
                    headerText: function(){
                        return "Document was rejected";
                    },
                    messageText: function(){
                        return "Document '" + $scope.MODEL.currentDocument.name  + "' was rejected.";
                    }
                };
                var resultInfo = $modal.open(optDialogInfoDlg);

                resultInfo.result.then(function () {
                    $modalInstance.close();
                }, function () {
                    $modalInstance.close();
                });
        },
         function(errorText){
             $scope.ActionModel.Error = errorText;
         });
    };

    var init = function(){
        if(!$scope.MODEL.currentDocument.isViewed){
            $scope.ActionModel.Error = 'Please download and read the document before performing any other actions.';
        }
    };

    init();
}]);