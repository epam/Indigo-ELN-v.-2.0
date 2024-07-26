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
.controller('documentInfoDlgController', ['$scope', '$modalInstance', 'dataFactory',
                     function ( $scope, $modalInstance, dataFactory) {

    $scope.InfoModel = {

    };

    $scope.ok = function () {
        $modalInstance.close();
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

    var loadDocumentInfo = function(){
        dataFactory.GetDocumentInfo($scope.MODEL.currentDocument.id,  function(document){
            angular.extend($scope.MODEL.currentDocument, document);
        });
    };

    var init = function(){
        if($scope.MODEL.currentDocument){
            loadDocumentInfo();
        }
    };

    init();
}]);