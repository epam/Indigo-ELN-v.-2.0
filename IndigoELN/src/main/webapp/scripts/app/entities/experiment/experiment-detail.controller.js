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
                var experimentForSave = _.extend({}, experiment);
                if (experiment.template !== null) {
                    $scope.loading = Experiment.update({
                        notebookId: $stateParams.notebookId,
                        projectId: $stateParams.projectId
                    }, experimentForSave, onSaveSuccess, onSaveError).$promise;
                } else {
                    $scope.loading = Experiment.save(experimentForSave, onSaveSuccess, onSaveError).$promise;
                }
            };

            $scope.statuses = ['Open', 'Complete', 'Submit_Fail', 'Submitted', 'Archieved', 'Signed'];

            $scope.statusOpen = function() {
                return experiment.status === 'Open';
            };
            $scope.statusComplete = function() {
                return experiment.status === 'Complete';
            };
            $scope.statusSubmitFail = function() {
                return experiment.status === 'Submit_Fail';
            };
            $scope.statusSubmitted = function() {
                return experiment.status === 'Submitted';
            };
            $scope.statusArchieved = function() {
                return experiment.status === 'Archieved';
            };
            $scope.statusSigned = function() {
                return experiment.status === 'Signed';
            };
    });
