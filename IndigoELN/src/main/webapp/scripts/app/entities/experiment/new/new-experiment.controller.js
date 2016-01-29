'use strict';

angular.module('indigoeln')
    .controller('NewExperimentController', function($scope, experiment) {
        $scope.experimentId = experiment.id;
});