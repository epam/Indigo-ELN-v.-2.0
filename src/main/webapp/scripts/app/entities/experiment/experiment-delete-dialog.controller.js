'use strict';

angular.module('indigoeln')
    .controller('ExperimentDeleteController', function ($scope, $uibModalInstance, entity, Experiment) {

        $scope.experiment = entity;
        $scope.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };
        $scope.confirmDelete = function (id) {
            Experiment.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        };

    });
