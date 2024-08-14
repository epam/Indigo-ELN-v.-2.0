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
.controller('loginController', ['$scope', 'userFactory', '$location',
                            function ($scope, userFactory, $location) {
    $scope.LoginModel = {
        login:"",
        password:"",
        IsValidated: false,
        Error: null
    };

    $scope.doLogin = function(){
        $scope.LoginModel.IsValidated = true;

        if($scope.LoginModel.frmLogin.$valid){
            userFactory.doLogin($scope.LoginModel.login, $scope.LoginModel.password, function(user){
                $scope.LoginModel.Error = null;
                $scope.MODEL.loggedUser = user;

                $scope.clearAll();

                $scope.loadDocuments();
                $scope.loadTemplates();
                $scope.loadReasons();

                $location.path("/");
            }, function(){
                $scope.LoginModel.Error = "Authorization failed. Invalid login or password.";
            });
            //$location.path("/");
        }
    };

    var init = function(){
        $scope.UI.isSessionExpiredDlgDisplayed = false;
    };

    init();
}]);