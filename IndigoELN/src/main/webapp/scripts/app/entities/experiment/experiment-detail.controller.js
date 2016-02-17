'use strict';

angular.module('indigoeln')
    .controller('ExperimentDetailController', function ($scope, $rootScope, $stateParams, experiments, Experiment, Principal, ExperimentBrowser, $state) {
        $scope.experiments = experiments;
        $scope.experiment = _.find(experiments, function (item) {
            return ExperimentBrowser.getIdByVal(item) == $stateParams.experimentId;
        });

        $scope.experimentId = $stateParams.experimentId;
        $scope.getIdByVal = ExperimentBrowser.getIdByVal;
        $scope.toModel = function toModel(components) {
            if (_.isArray(components)) {
                return _.object(_.map(components, function (component) {
                    return [component.name, component.content]
                }));
            } else {
                return components;
            }
        };
        Principal.hasAuthority('CONTENT_EDITOR').then(function (result) {
            $scope.isContentEditor = result;
        });
        var onSaveSuccess = function (result) {
            $scope.isSaving = false;
        };

        var onSaveError = function (result) {
            $scope.isSaving = false;
        };

        $scope.save = function (experiment) {
            $scope.isSaving = true;
            var experimentForSave = _.extend({}, experiment, {components: toComponents(experiment.components)});
            if (experiment.template != null) {
                var params = ExperimentBrowser.expandIds($stateParams.experimentId);
                $scope.loading = Experiment.update({
                    notebookId: params.notebookId,
                    projectId: params.projectId
                }, experimentForSave, onSaveSuccess, onSaveError).$promise;
            } else {
                $scope.loading = Experiment.save(experimentForSave, onSaveSuccess, onSaveError).$promise;
            }
        };

        $scope.closeTab = function (compactId) {
            ExperimentBrowser.close($state, compactId, $scope.experimentId);
        };

        function toComponents(model) {
            return _.map(model, function (val, key) {
                return {name: key, content: val};
            });
        }
    });
