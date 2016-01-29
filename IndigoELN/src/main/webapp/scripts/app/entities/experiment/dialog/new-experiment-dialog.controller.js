'use strict';

angular.module('indigoeln')
    .controller('NewExperimentDialogController', function ($scope, $uibModalInstance, $log, Experiment, templates, notebook) {
        $scope.experiment = {};
        $scope.templates = templates;
        $scope.experiment.templateId = '';
        $scope.experiment.project = notebook.projectId;

        var onCreateSuccess = function (result) {
            $log.log('Experiment created successfully.');
            $uibModalInstance.close({experiment: {id: notebook.notebookName + '-' + result.experimentNumber}});
        };

        var onCreateError = function (result) {
            $log.warn('Experiment wasn\'t created.');
        };

        $scope.ok = function () {
            Experiment.post({notebookId: notebook.notebookId}, $scope.experiment, onCreateSuccess, onCreateError);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss();
        };
    });