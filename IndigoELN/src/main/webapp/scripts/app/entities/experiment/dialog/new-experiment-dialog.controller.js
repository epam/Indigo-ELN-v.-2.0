'use strict';

angular
    .module('indigoeln')
    .controller('NewExperimentDialogController', NewExperimentDialogController);

NewExperimentDialogController.$inject = ['$scope', '$uibModalInstance'];

function NewExperimentDialogController($scope, $uibModalInstance) {
    $scope.notebook = {};

    $scope.ok = function () {
        $uibModalInstance.close($scope.notebook);
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss();
    };
}