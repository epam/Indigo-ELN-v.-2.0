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
.controller('documentListController', ['$scope', 'dataFactory', '$modal', '$filter',
                            function ($scope, dataFactory, $modal, $filter) {

    $scope.UI.activeMenu = 1;



    $scope.signDocument = function(doc){
        $scope.MODEL.currentDocument = doc;
        $scope.MODEL.currentAction = 'sign';

        var modalInstance = $modal.open($scope.DocumentListModel.optDocumentActionDlg);

          modalInstance.result.then(function () {
            $scope.MODEL.currentDocument = null;
            $scope.MODEL.currentAction = null;
          }, function () {
                  $scope.MODEL.currentDocument = null;
                  $scope.MODEL.currentAction = null;
          });
    };

    $scope.rejectDocument = function(doc){
        $scope.MODEL.currentDocument = doc;
        $scope.MODEL.currentAction = 'reject';

        var modalInstance = $modal.open($scope.DocumentListModel.optDocumentActionDlg);

          modalInstance.result.then(function () {
            $scope.MODEL.currentDocument = null;
            $scope.MODEL.currentAction = null;
          }, function () {
                  $scope.MODEL.currentDocument = null;
                  $scope.MODEL.currentAction = null;
          });
    };

    $scope.downloadDocument = function(doc){
        doc.isViewed = true;
    };

    $scope.showDocumentInfo = function(doc){
        $scope.MODEL.currentDocument = doc;
        var modalInstance = $modal.open($scope.DocumentListModel.optDocumentInfoDlg);

          modalInstance.result.then(function () {
            $scope.MODEL.currentDocument = null;
          }, function () {
            $scope.MODEL.currentDocument = null;
          });
    };

    $scope.uploadDocument = function(){
        var modalInstance = $modal.open($scope.DocumentListModel.optUploadDocumentDlg);

          modalInstance.result.then(function () {
            // submit
          }, function () {});
    };

    $scope.changeShowAll = function(){
        $scope.DocumentListModel.Filter.Status = null;
        $scope.search();
    };

    $scope.setSortOptions = function(fieldName){
        if($scope.DocumentListModel.Sort.Field == fieldName){
            $scope.DocumentListModel.Sort.IsDescending = !$scope.DocumentListModel.Sort.IsDescending;
        }
        else{
            $scope.DocumentListModel.Sort.Field = fieldName;
            $scope.DocumentListModel.Sort.IsDescending = false;
        }
        $scope.search();
    };

    // init the filtered items
    $scope.search = function () {
        $scope.DocumentListModel.Filter.ResultList = $filter('filterDocuments')($scope.MODEL.documents, $scope.DocumentListModel.Filter.SearchText, $scope.DocumentListModel.Filter.TimePeriod, $scope.DocumentListModel.Filter.Status, $scope.DocumentListModel.Filter.ShowAll);
        // take care of the sorting order

        $scope.DocumentListModel.Filter.ResultList = $filter('orderBy')($scope.DocumentListModel.Filter.ResultList, $scope.DocumentListModel.Sort.Field, $scope.DocumentListModel.Sort.IsDescending);

        $scope.DocumentListModel.Pager.CurrentPage = 1;
        // now group by pages
        $scope.groupToPages();
    };

    // calculate page in place
    $scope.groupToPages = function () {
        $scope.DocumentListModel.Pager.ResultList = [];

        for (var i = 0; i < $scope.DocumentListModel.Filter.ResultList.length; i++) {
            if (i % $scope.DocumentListModel.Pager.ItemsPerPage === 0) {
                $scope.DocumentListModel.Pager.ResultList[Math.floor(i / $scope.DocumentListModel.Pager.ItemsPerPage)] = [ $scope.DocumentListModel.Filter.ResultList[i] ];
            } else {
                $scope.DocumentListModel.Pager.ResultList[Math.floor(i / $scope.DocumentListModel.Pager.ItemsPerPage)].push($scope.DocumentListModel.Filter.ResultList[i]);
            }
        }
    };

    $scope.setPage = function(n) {
        $scope.DocumentListModel.Pager.CurrentPage = n;
    };

    $scope.$on("Documents:change", function(){
        $scope.search();
    });

    var init = function(){
        if(!$scope.checkLoggedUser()){ return }

        if($scope.MODEL.documents.length != 0 && $scope.DocumentListModel.Pager.ResultList.length==0){
            $scope.fireDocumentChangedEvent();
        }
    };

    init();
}]);