'use strict';

angular.module('indigoeln')
    .controller('ExperimentDetailController', function ($scope, $rootScope, $stateParams, entity, Experiment) {
        $scope.experiment = entity;
        $scope.load = function (id) {
            Experiment.get({id: id}, function (result) {
                $scope.experiment = result;
            });
        };
        var unsubscribe = $rootScope.$on('indigoeln:experimentUpdate', function (event, result) {
            $scope.experiment = result;
        });
        $scope.$on('$destroy', unsubscribe);

    });
