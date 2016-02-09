'use strict';

angular.module('indigoeln')
    .controller('ExperimentDetailController', function ($scope, $rootScope, $stateParams, data, Experiment) {
        $scope.experiment = data.entity;
        $scope.template = data.template;
        $scope.notebookId = $stateParams.notebookId;
        $scope.model = toModel($scope.experiment.components);
        var onSaveSuccess = function (result) {
            $scope.isSaving = false;
        };

        var onSaveError = function (result) {
            $scope.isSaving = false;
        };

        $scope.save = function () {
            $scope.isSaving = true;
            var experimentForSave = _.extend({}, $scope.experiment, {components: toComponents($scope.model)});
            if ($scope.template.id != null) {
                Experiment.update({
                    notebookId: $stateParams.notebookId,
                    projectId: $stateParams.projectId
                }, experimentForSave, onSaveSuccess, onSaveError);
            } else {
                Experiment.save(experimentForSave, onSaveSuccess, onSaveError);
            }
        };

        function toModel(components) {
            return _.object(_.map(components, function (component) {
                return [component.name, component.content]
            }));
        }

        function toComponents(model) {
            return _.map(model, function (val, key) {
                return {name: key, content: val};
            });
        }
    });
