'use strict';

angular
    .module('indigoeln')
    .controller('NewNotebookCtrl', NewNotebookCtrl);

NewNotebookCtrl.$inject = ['$scope', '$uibModalInstance'];

function NewNotebookCtrl($scope, $uibModalInstance) {
    $scope.notebook = {};

    $scope.ok = function () {
        $uibModalInstance.close($scope.notebook);
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss();
    };
}