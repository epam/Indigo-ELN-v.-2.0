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
.controller('userListController', ['$scope', 'dataFactory', '$modal', '$filter', 'userFactory',
                            function ($scope, dataFactory, $modal, $filter, userFactory) {

    $scope.UI.activeMenu = 3;

    $scope.UserListModel = {
        savedUser: {},
        isNewUser: false,
        IsValidated: false,
        Error: null,
        SearchUserTerm: "",
        Sort:   {
            Field: 'login', //name, author
            IsDescending: false
        },
        users: []
    };

    $scope.searchUsers = function() {
        userFactory.find($scope.UserListModel.SearchUserTerm, 0, function(data){
            $scope.MODEL.users = data;
        });
    };

    $scope.editUser = function(user){
        $scope.MODEL.currentUser = user;
        $scope.UserListModel.savedUser = angular.copy(user);
        $scope.UserListModel.isNewUser = false;
        $scope.UserListModel.IsValidated = false;

        $scope.fireUserEditEvent();
    };

    $scope.cancelEdit = function(){
        if($scope.UserListModel.isNewUser){
            $scope.MODEL.currentUser = null;
        }else{
            angular.extend($scope.MODEL.currentUser, $scope.UserListModel.savedUser);
        }
        $scope.MODEL.currentUser = null;
        $scope.UserListModel.isNewUser = false;
        $scope.UserListModel.IsValidated = false;
    };

    $scope.saveUser = function(){
        $scope.UserListModel.IsValidated = true;

        if(!$scope.UserListModel.frmUser.$valid  || !$scope.UserListModel.frmUser.inpName.$valid){
            return;
        }

        if($scope.UserListModel.isNewUser){
            userFactory.createUser($scope.MODEL.currentUser, function(user){
                $scope.MODEL.currentUser = null;
                $scope.UserListModel.isNewUser = false;
                $scope.UserListModel.IsValidated = false;
            });
        }
        else{
            $scope.UserListModel.savedUser = false;
            userFactory.saveUser($scope.MODEL.currentUser, function(user){
                angular.extend($scope.MODEL.currentUser, user);
                $scope.MODEL.currentUser = null;
                $scope.UserListModel.isNewUser = false;
                $scope.UserListModel.IsValidated = false;
            });
        }
    };

    $scope.newUser = function(){
        $scope.MODEL.currentUser = DataMapper.getNewUser();
        $scope.UserListModel.isNewUser = true;
        $scope.UserListModel.savedUser = null;
        $scope.UserListModel.IsValidated = false;

        $scope.fireUserEditEvent();
    };

    $scope.setSortOptions = function(fieldName){
        if($scope.UserListModel.Sort.Field == fieldName){
            $scope.UserListModel.Sort.IsDescending = !$scope.UserListModel.Sort.IsDescending;
        }
        else{
            $scope.UserListModel.Sort.Field = fieldName;
            $scope.UserListModel.Sort.IsDescending = false;
        }
    };

    $scope.fireUserEditEvent = function(){
        $scope.$broadcast("Users:edit");
    };

    var init = function(){
        $scope.checkLoggedUser();
    };

    init();
}]);