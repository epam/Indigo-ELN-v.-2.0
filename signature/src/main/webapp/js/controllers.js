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
'use strict';

/* Controllers */

angular.module('App.controllers', [])
.controller('mainController', [ '$scope', 'dataFactory', '$location', 'userFactory', '$modal',
                                function($scope, dataFactory, $location, userFactory, $modal) {

	// This is model for global manipulation of data in all controllers.
	$scope.UI = {
	    statuses: [
	    { id: 1, name: "Submitted"},
	    { id: 2, name: "Signing"},
	    { id: 3, name: "Signed"},
	    { id: 4, name: "Rejected"},
        { id: 8, name: "Archived"}
	    ],
	    awaitStatuses: [
	    { id: 1, name: "Submitted"},
	    { id: 2, name: "Signing"}
	    ],
	    timePeriods: [
	     { id: 1, name: "Last year"},
	     { id: 2, name: "Last month"},
	     { id: 3, name: "Last week"},
	     { id: 4, name: "Last day"}
	    ],
	    activeMenu: 1, //1 - Document list, 2 - Awaiting documents, 3 - templates
	    optSessionExpiredDlg: {
           templateUrl: 'sessionExpiredTmpl',
           controller: function ($scope, $modalInstance) {
                   $scope.ok = function () {
                       $modalInstance.close();
                   };
              },
           scope: $scope,
           resolve: {  }
        },
        isSessionExpiredDlgDisplayed: true,
        isUploadDocumentsAllowed: true,
        dateFormat: 'dd-MMM-yyyy HH:mm',
        signMethod: 0 // 0-default, 1 - verison
    };

	$scope.MODEL = {
	    documents: [],
	    templates: [],
	    currentDocument: null,
	    currentAction: null,
	    currentTemplate: null,
	    currentUser: null,
	    reasons: [],
	    users: [],
	    loggedUser: null
	};

    $scope.DocumentListModel = {
        Filter: {
            SearchText: "",
            TimePeriod: null,
            Status: null,
            ShowAll: false,
            ResultList: []
        },
        Sort:   {
            Field: 'creationDate', //name, creationDate, modifiedDate
            IsDescending: true
        },
        optDocumentInfoDlg: {
           templateUrl: 'documentInfoTmpl',
           controller: 'documentInfoDlgController',
           scope: $scope,
           windowClass: 'large-width',
           resolve: {  }
        },
        optDocumentActionDlg: {
           templateUrl: 'documentActionTmpl',
           controller: 'documentActionDlgController',
           scope: $scope,
           resolve: {  }
        },
        optUploadDocumentDlg: {
           templateUrl: 'uploadDocumentTmpl',
           controller: 'uploadDocumentDlgController',
           scope: $scope,
           resolve: {  }
        },
        Pager: {
            CurrentPage: 1,
            ItemsPerPage: 10,
            ResultList: [],
            AvailableItemsPerPage:[5, 10, 25, 50, 100, 1000]
        }
    };

    $scope.TemplateListModel = {
        savedTemplate: {},
        isNewTemplate: false,
        IsValidated: false,
        optTemplateDeleteDlg: {
           templateUrl: 'templateDeleteTmpl',
           controller: 'templateDeleteDlgController',
           scope: $scope,
           resolve: {  }
        },
        Sort:   {
            Field: 'name', //name, author
            IsDescending: false
        },
        Pager: {
            CurrentPage: 1,
            ItemsPerPage: 10,
            ResultList: [],
            AvailableItemsPerPage:[5, 10, 25, 50, 100, 1000]
        },
        Filter: {
            ResultList: []
        },
        IsLoadingUsers: false,
        SearchUserTerm: false,
        AvailableUsers: []
    };

    $scope.loadDocuments = function(){
        dataFactory.getDocuments(function(documentList){
                    $scope.MODEL.documents = documentList;
                    $scope.fireDocumentChangedEvent();
            });
    };

    $scope.loadTemplates = function(){
        dataFactory.getTemplates(function(templateList){
                    $scope.MODEL.templates = templateList;
                    $scope.fireTemplateChangedEvent();
            });
    };

    $scope.loadReasons = function(){
        dataFactory.getReasons(function(reasonList){
                    $scope.MODEL.reasons = reasonList;
            });
    };

    $scope.getReasonById = function(reasonId){
        var reason = null;

        angular.forEach($scope.MODEL.reasons, function(r){
            if(r.id == reasonId){
                reason = r;
            }
        });

        return reason;
    };

    $scope.fireDocumentChangedEvent = function(){
        $scope.$broadcast("Documents:change");
    };

    $scope.fireTemplateChangedEvent = function(){
        $scope.$broadcast("Templates:change");
    };

    $scope.fireTemplateEditEvent = function(){
        $scope.$broadcast("Templates:edit");
    };

    $scope.checkLoggedUser = function(){
        if($scope.MODEL.loggedUser != null){
            return true;
        }

        userFactory.getUser(function(user){
            $scope.MODEL.loggedUser = user;
            $scope.loadDocuments();
            $scope.loadTemplates();
            $scope.loadReasons();
        }, function(){
            // error callback
            $location.path("/login");
        });
        return false;
    };

    $scope.showSessionExpiredDialog = function(){
        if($scope.UI.isSessionExpiredDlgDisplayed){ return; }

        $scope.MODEL.loggedUser = null;
        $scope.UI.isSessionExpiredDlgDisplayed = true;
        var modalInstance = $modal.open($scope.UI.optSessionExpiredDlg);

        modalInstance.result.then(function () {
            $location.path("/login");
        }, function () {
            $location.path("/login");
        });
    };

    $scope.logout = function(){
        userFactory.doLogout();
        $scope.clearAll();
        $location.path("/login");
    };

    $scope.$on("event:loginRequired", function(){
        if($location.path() != "/login"){
            $scope.showSessionExpiredDialog();
        }
    });

    $scope.clearAll = function(){
        $scope.MODEL.documents = [];
        $scope.MODEL.users = [];
        $scope.MODEL.templates = [];
        $scope.MODEL.reasons = [];
        $scope.MODEL.currentDocument = null;
        $scope.MODEL.currentTemplate = null;
        $scope.MODEL.currentUser = null;

        $scope.DocumentListModel.Filter.ResultList = [];
        $scope.DocumentListModel.Pager.ResultList = [];

        $scope.TemplateListModel.Filter.ResultList = [];
        $scope.TemplateListModel.Pager.ResultList = [];
    };

    var initSettings = function(){
        $scope.UI.isUploadDocumentsAllowed = window.settings.isUpload;
        $scope.UI.signMethod = window.settings.signingMethod=='verizon' ? 1 : 0;
    };

    var init = function(){
        initSettings();

        if(!$scope.checkLoggedUser()){ return }

        $scope.loadDocuments();
        $scope.loadTemplates();
        $scope.loadReasons();
    };

    init();
}]);
