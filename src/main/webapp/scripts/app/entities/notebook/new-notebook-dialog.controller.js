'use strict';

angular
    .module('indigoeln')
    .controller('NewNotebookDialogController', NewNotebookDialogController);

NewNotebookDialogController.$inject = ['$scope', '$uibModalInstance', '$state'];

function NewNotebookDialogController($scope, $uibModalInstance, $state) {
    $scope.notebook = {};

    $scope.ok = function () {
        $uibModalInstance.close($scope.notebook);
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss();
    };
}