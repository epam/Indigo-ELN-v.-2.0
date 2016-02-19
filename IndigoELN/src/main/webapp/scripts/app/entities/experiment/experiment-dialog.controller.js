'use strict';

angular.module('indigoeln').controller('ExperimentDialogController',
    function ($scope, $rootScope, $stateParams, entity, Experiment, templates, mode, $state) {

        $scope.experiment = entity;
        $scope.notebookId = $stateParams.notebookId;
        $scope.projectId = $stateParams.projectId;
        $scope.templates = templates;
        $scope.mode = mode;
        $scope.template = $scope.experiment.template;
        var onSaveSuccess = function (result) {
            $scope.isSaving = false;
            $state.go('entities.experiment-detail', {
                notebookId: $stateParams.notebookId,
                projectId: $stateParams.projectId,
                experimentId: result.id
            });
            $rootScope.$broadcast('experiment-created', {projectId: $stateParams.projectId, notebookId: $stateParams.notebookId, id: result.id});
        };

        var onSaveError = function (result) {
            $scope.isSaving = false;
        };

        $scope.save = function () {
            $scope.isSaving = true;
            $scope.experiment = _.extend($scope.experiment, {template: $scope.template})
            if ($scope.experiment.id != null) {
                Experiment.update({
                    projectId: $stateParams.projectId,
                    notebookId: $stateParams.notebookId,
                    id: $scope.experiment.id
                }, $scope.experiment, onSaveSuccess, onSaveError);
            } else {
                Experiment.save({
                    notebookId: $stateParams.notebookId,
                    projectId: $stateParams.projectId
                }, $scope.experiment, onSaveSuccess, onSaveError);
            }
        };

    });
