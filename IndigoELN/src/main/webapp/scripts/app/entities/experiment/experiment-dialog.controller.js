'use strict';

angular.module('indigoeln').controller('ExperimentDialogController',
    function ($scope, $stateParams, entity, Experiment) {

        $scope.experiment = entity;
        $scope.load = function (id) {
            Experiment.get({id: id}, function (result) {
                $scope.experiment = result;
            });
        };

        var onSaveSuccess = function (result) {
            $scope.isSaving = false;
            $scope.back();
        };

        var onSaveError = function (result) {
            $scope.isSaving = false;
        };

        $scope.save = function () {
            $scope.isSaving = true;
            if ($scope.experiment.id != null) {
                Experiment.update($scope.experiment, onSaveSuccess, onSaveError);
            } else {
                Experiment.save($scope.experiment, onSaveSuccess, onSaveError);
            }
        };

    });
