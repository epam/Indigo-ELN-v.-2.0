'use strict';

angular.module('indigoeln')
    .controller('NewExperimentDialogController', function($scope, $uibModalInstance) {
        $scope.notebook = {};

        $scope.ok = function () {
            $uibModalInstance.close($scope.notebook);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss();
        };
    });