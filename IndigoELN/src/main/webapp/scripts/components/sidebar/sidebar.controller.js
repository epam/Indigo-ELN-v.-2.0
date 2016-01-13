'use strict';

angular.module('indigoeln')
    .controller('SidebarController', function ($scope, $location, $state) {
        $scope.$state = $state;

        //$scope.logout = function () {
        //    Auth.logout();
        //    $state.go('home');
        //};
    });
