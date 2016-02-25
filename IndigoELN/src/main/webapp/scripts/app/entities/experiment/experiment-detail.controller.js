'use strict';

angular.module('indigoeln')
    .controller('ExperimentDetailController',
        function ($scope, $rootScope, $stateParams, Experiment, Principal, PermissionManagement,
                  experiment, identity, isContentEditor, hasEditAuthority) {

            $scope.experiment = experiment;
            $scope.experimentId = $stateParams.experimentId;
            $scope.experiment.author = $scope.experiment.author || identity;
            $scope.experiment.accessList = $scope.experiment.accessList || PermissionManagement.getAuthorAccessList(identity);
            $scope.isCollapsed = true;

            PermissionManagement.setEntity('Experiment');
            PermissionManagement.setAuthor($scope.experiment.author);
            PermissionManagement.setAccessList($scope.experiment.accessList);

            var onAccessListChangedEvent = $scope.$on('access-list-changed', function(event) {
                $scope.experiment.accessList = PermissionManagement.getAccessList();
            });
            $scope.$on('$destroy', function() {
                onAccessListChangedEvent();
            });

            PermissionManagement.hasPermission('UPDATE_ENTITY').then(function (hasEditPermission) {
                $scope.isEditAllowed = isContentEditor || hasEditAuthority && hasEditPermission;
            });

            $scope.toModel = function toModel(components) {
                if (_.isArray(components)) {
                    return _.object(_.map(components, function (component) {
                        return [component.name, component.content];
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
                experiment.accessList = PermissionManagement.expandPermission(experiment.accessList);
                var experimentForSave = _.extend({}, experiment, {components: toComponents(experiment.components)});
                if (experiment.template != null) {
                    $scope.loading = Experiment.update({
                        notebookId: $stateParams.notebookId,
                        projectId: $stateParams.projectId
                    }, experimentForSave, onSaveSuccess, onSaveError).$promise;
                } else {
                    $scope.loading = Experiment.save(experimentForSave, onSaveSuccess, onSaveError).$promise;
                }
            };

            function toComponents(model) {
                return _.map(model, function (val, key) {
                    return {name: key, content: val};
                });
            }
    });
