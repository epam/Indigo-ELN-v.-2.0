'use strict';

angular.module('indigoeln').controller('ExperimentDialogController',
    function ($scope, $stateParams, entity, Experiment, templates, mode) {

        $scope.experiment = entity;
        $scope.notebookId = $stateParams.notebookId;
        $scope.templates = templates;
        $scope.mode = mode;

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
                Experiment.update({
                    notebookId: $stateParams.notebookId,
                    id: $scope.experiment.id
                }, $scope.experiment, onSaveSuccess, onSaveError);
            } else {
                Experiment.save({notebookId: $stateParams.notebookId}, $scope.experiment, onSaveSuccess, onSaveError);
            }
        };

    });
