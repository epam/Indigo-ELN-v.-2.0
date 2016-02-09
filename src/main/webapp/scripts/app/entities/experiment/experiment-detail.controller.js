'use strict';

angular.module('indigoeln')
    .controller('ExperimentDetailController', function ($scope, $rootScope, $stateParams, data) {
        $scope.experiment = data.entity;
        $scope.template = data.template;
        $scope.notebookId = $stateParams.notebookId;

    });
