'use strict';

angular.module('indigoeln')
    .controller('ExperimentDetailController', function ($scope, $rootScope, $stateParams, entity, Experiment) {
        $scope.experiment = entity;
    });
