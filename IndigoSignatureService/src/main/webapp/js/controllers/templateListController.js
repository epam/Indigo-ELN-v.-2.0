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
.controller('templateListController', ['$scope', 'dataFactory', '$modal', '$filter', 'userFactory',
                            function ($scope, dataFactory, $modal, $filter, userFactory) {

    $scope.UI.activeMenu = 2;


    $scope.editTemplate = function(template){
        $scope.MODEL.currentTemplate = template;
        $scope.TemplateListModel.savedTemplate = angular.copy(template);
        $scope.TemplateListModel.isNewTemplate = false;
        $scope.TemplateListModel.IsValidated = false;

        angular.forEach($scope.MODEL.currentTemplate.signatureBlocks, function(block){
            var reason = $scope.getReasonById(block.reasonId);

            if(reason != null){
                block.reason = reason;
            }
        });

        $scope.fireTemplateEditEvent();
    };

    $scope.cancelEdit = function(){
        if($scope.TemplateListModel.isNewTemplate){
            $scope.MODEL.currentTemplate = null;
        }else{
            $scope.MODEL.currentTemplate.signatureBlocks = [];
            angular.extend($scope.MODEL.currentTemplate, $scope.TemplateListModel.savedTemplate);
        }
        $scope.MODEL.currentTemplate = null;
        $scope.TemplateListModel.isNewTemplate = false;
        $scope.TemplateListModel.IsValidated = false;
    };

    $scope.saveTemplate = function(){
        $scope.TemplateListModel.IsValidated = true;
        if($scope.MODEL.currentTemplate.signatureBlocks.length==0){
            $scope.TemplateListModel.frmTemplate.$valid = false;
        }
        if(!$scope.TemplateListModel.frmTemplate.$valid  || !$scope.TemplateListModel.frmTemplate.inpName.$valid){
            return;
        }

        if($scope.TemplateListModel.isNewTemplate){
            dataFactory.createTemplate($scope.MODEL.currentTemplate, function(template){
                $scope.MODEL.templates.push(template);
                $scope.MODEL.currentTemplate = null;
                $scope.TemplateListModel.isNewTemplate = false;
                $scope.TemplateListModel.IsValidated = false;
                $scope.fireTemplateChangedEvent();
            });
        }
        else{
            $scope.TemplateListModel.savedTemplate = false;
            dataFactory.saveTemplate($scope.MODEL.currentTemplate, function(template){
                $scope.MODEL.currentTemplate.signatureBlocks = [];
                angular.extend($scope.MODEL.currentTemplate, template);
                $scope.MODEL.currentTemplate = null;
                $scope.TemplateListModel.isNewTemplate = false;
                $scope.TemplateListModel.IsValidated = false;
            });
        }
    };

    $scope.deleteTemplate = function(template){
        $scope.MODEL.currentTemplate = template;

        var modalInstance = $modal.open($scope.TemplateListModel.optTemplateDeleteDlg);

          modalInstance.result.then(function () {
            var inx = -1;
            angular.forEach($scope.MODEL.templates, function(template, i){
                if(template.id == $scope.MODEL.currentTemplate.id){
                    inx = i;
                }
            });
            if(inx != -1){
                $scope.MODEL.templates.splice(inx, 1);
            }
            $scope.MODEL.currentTemplate = null;

            $scope.fireTemplateChangedEvent();
          }, function () {
              $scope.MODEL.currentTemplate = null;
          });
    };

    $scope.newTemplate = function(){
        $scope.MODEL.currentTemplate = DataMapper.getNewTemplate();
        $scope.TemplateListModel.isNewTemplate = true;
        $scope.TemplateListModel.savedTemplate = null;
        $scope.TemplateListModel.IsValidated = false;

        $scope.fireTemplateEditEvent();
    };

    $scope.newSignBlock = function(){
        var newBlock = DataMapper.getNewSignatureBlock($scope.MODEL.currentTemplate.signatureBlocks.length+1);
        var reason = $scope.getReasonById(newBlock.reasonId);

        if(reason != null){
             newBlock.reason = reason;
        }
        $scope.MODEL.currentTemplate.signatureBlocks.push(newBlock);
        $scope.TemplateListModel.IsValidated = false;
    };

    $scope.deleteSignBlock = function(blockIndex){
        var ind = -1;
        angular.forEach($scope.MODEL.currentTemplate.signatureBlocks, function(block, indx){
            if(block.index == blockIndex){
                ind = indx;
            }
        });
        if(ind != -1){
            $scope.MODEL.currentTemplate.signatureBlocks.splice(ind, 1);
            for(var i=0; i < $scope.MODEL.currentTemplate.signatureBlocks.length; i++){
                var block = $scope.MODEL.currentTemplate.signatureBlocks[i];
                if(block.index > blockIndex){
                    block.index--;
                }
            }
        }
        $scope.TemplateListModel.IsValidated = false;
    };

    $scope.upSignBlock = function(blockIndex){
        var prevBlock = getSignBlockByIndex(blockIndex-1);
        var currBlock = getSignBlockByIndex(blockIndex);

        prevBlock.index++;
        currBlock.index--;

        $scope.TemplateListModel.IsValidated = false;
    };

    $scope.downSignBlock = function(blockIndex){
        var nextBlock = getSignBlockByIndex(parseInt(blockIndex)+1);
        var currBlock = getSignBlockByIndex(blockIndex);

        nextBlock.index--;
        currBlock.index++;

        $scope.TemplateListModel.IsValidated = false;
    };

    var getSignBlockByIndex = function(index){
        var res = null;
        angular.forEach($scope.MODEL.currentTemplate.signatureBlocks, function(block){
            if(block.index == index) res = block;
        });
        return res;
    };

    $scope.setSortOptions = function(fieldName){
        if($scope.TemplateListModel.Sort.Field == fieldName){
            $scope.TemplateListModel.Sort.IsDescending = !$scope.TemplateListModel.Sort.IsDescending;
        }
        else{
            $scope.TemplateListModel.Sort.Field = fieldName;
            $scope.TemplateListModel.Sort.IsDescending = false;
        }
        $scope.search();
    };

    // init the filtered items
    $scope.search = function () {
        //$scope.DocumentListModel.Filter.ResultList = $filter('filterDocuments')($scope.MODEL.documents, $scope.DocumentListModel.Filter.SearchText, $scope.DocumentListModel.Filter.TimePeriod, $scope.DocumentListModel.Filter.Status, $scope.DocumentListModel.Filter.ShowAll);
        // take care of the sorting order
        $scope.TemplateListModel.Filter.ResultList = $scope.MODEL.templates;

        $scope.TemplateListModel.Filter.ResultList = $filter('orderBy')($scope.TemplateListModel.Filter.ResultList, $scope.TemplateListModel.Sort.Field, $scope.TemplateListModel.Sort.IsDescending);

        $scope.TemplateListModel.Pager.CurrentPage = 1;
        // now group by pages
        $scope.groupToPages();

        $scope.TemplateListModel.isNewTemplate = false;
        $scope.TemplateListModel.savedTemplate = null;
        $scope.TemplateListModel.IsValidated = false;
        $scope.MODEL.currentTemplate = null;
    };

    // calculate page in place
    $scope.groupToPages = function () {
        $scope.TemplateListModel.Pager.ResultList = [];

        for (var i = 0; i < $scope.TemplateListModel.Filter.ResultList.length; i++) {
            var item = $scope.TemplateListModel.Filter.ResultList[i];
            var pos = Math.floor(i / $scope.TemplateListModel.Pager.ItemsPerPage);

            if (i % $scope.TemplateListModel.Pager.ItemsPerPage === 0) {
                $scope.TemplateListModel.Pager.ResultList[pos] = [item];
            } else {
                $scope.TemplateListModel.Pager.ResultList[pos].push(item);
            }
        }
    };

    $scope.setPage = function(n) {
        $scope.TemplateListModel.Pager.CurrentPage = n;
    };

     $scope.getTypeHeadLabel = function(user) {
         if(!user)return '';
         var postfix = (user.login != '')? (' (' + user.login + ')') : '';
         if(user.firstName == ''){
            return user.lastName + postfix;
         }
         if(user.lastName == ''){
            return user.firstName + postfix;
         }
         return user.firstName + " "+user.lastName + postfix;
     };

    $scope.getUsers = function(term) {
        $scope.TemplateListModel.IsLoadingUsers = true;
        $scope.TemplateListModel.SearchUserTerm = term;

        return userFactory.search($scope.TemplateListModel.SearchUserTerm, function(data){
            $scope.TemplateListModel.AvailableUsers = data;
            $scope.TemplateListModel.IsLoadingUsers = false;
            return $scope.TemplateListModel.AvailableUsers;
        });
    };

    $scope.$on("Templates:change", function(){
        $scope.search();
    });

    var init = function(){
        if(!$scope.checkLoggedUser()){ return }
        if($scope.MODEL.templates.length != 0 && $scope.TemplateListModel.Pager.ResultList.length==0){
            $scope.fireTemplateChangedEvent();
        }

        $scope.MODEL.currentTemplate = null;
        $scope.TemplateListModel.isNewTemplate = false;
        $scope.TemplateListModel.savedTemplate = null;
        $scope.TemplateListModel.IsValidated = false;
    };

    init();
}]);