'use strict';

angular.module('indigoeln')
    .controller('NavbarController', function ($scope, $state, $rootScope, Principal, Auth) {
        $scope.CONTENT_EDITOR = 'CONTENT_EDITOR';
        $scope.PROJECT_CREATOR = 'PROJECT_CREATOR';
        $scope.NOTEBOOK_CREATOR = 'NOTEBOOK_CREATOR';
        $scope.EXPERIMENT_CREATOR = 'EXPERIMENT_CREATOR';
        $scope.PROJECT_CREATORS = [$scope.CONTENT_EDITOR, $scope.PROJECT_CREATOR].join(',');
        $scope.NOTEBOOK_CREATORS = [$scope.CONTENT_EDITOR, $scope.NOTEBOOK_CREATOR].join(',');
        $scope.EXPERIMENT_CREATORS = [$scope.CONTENT_EDITOR, $scope.EXPERIMENT_CREATOR].join(',');
        $scope.ENTITY_CREATORS = [$scope.CONTENT_EDITOR, $scope.PROJECT_CREATOR, $scope.NOTEBOOK_CREATOR, $scope.EXPERIMENT_CREATOR].join(',');

        $scope.Principal = Principal;

        $scope.logout = function () {
            var userId = Principal.getIdentity().id;
            Auth.logout();
            $rootScope.$broadcast('user-logout', {id: userId});
            $state.go('login');
        };
    });