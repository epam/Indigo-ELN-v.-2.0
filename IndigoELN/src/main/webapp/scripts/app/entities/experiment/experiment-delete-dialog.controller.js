'use strict';

angular.module('indigoeln')
    .controller('ExperimentDeleteController', function ($scope, $uibModalInstance, entity, Experiment, $stateParams) {

        $scope.experiment = entity;
        $scope.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };
        $scope.confirmDelete = function (id) {
            Experiment.delete({experimentId: id, notebookId: $stateParams.notebookId},
                function () {
                    $uibModalInstance.close(true);
                });
        };

    });
