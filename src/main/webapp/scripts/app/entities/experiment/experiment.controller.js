'use strict';

angular.module('indigoeln')
    .controller('ExperimentController', function ($scope, $state, Dashboard) {

        $scope.experiments = [];
        $scope.predicate = 'id';
        $scope.reverse = true;
        $scope.page = 1;
        $scope.loadAll = function () {
            Dashboard.get({}, function (result) {
                $scope.experiments = result;
            });
        };
        $scope.loadPage = function (page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();


        $scope.refresh = function () {
            $scope.loadAll();
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.experiment = {
                name: null,
                experimentNumber: null,
                templateId: null,
                id: null
            };
        };
    });
