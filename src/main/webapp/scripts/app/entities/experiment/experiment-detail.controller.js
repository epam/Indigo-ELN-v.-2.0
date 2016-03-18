'use strict';

angular.module('indigoeln')
    .controller('ExperimentDetailController',
        function ($scope, $rootScope, $stateParams, Experiment, Principal, PermissionManagement, pageInfo, $uibModal) {

            // TODO: the Action drop up button should be disable in case of there is unsaved data.

            $scope.statuses = ['Opened', 'Completed', 'SubmitFailed', 'Submitted', 'Archived', 'Signed'];

            $scope.experiment = pageInfo.experiment;
            $scope.notebook = pageInfo.notebook;
            $scope.experimentId = $stateParams.experimentId;
            $scope.experiment.author = $scope.experiment.author || pageInfo.identity;
            $scope.experiment.accessList = $scope.experiment.accessList || PermissionManagement.getAuthorAccessList(pageInfo.identity);
            $scope.isCollapsed = true;

            PermissionManagement.setEntity('Experiment');
            PermissionManagement.setAuthor($scope.experiment.author);
            PermissionManagement.setAccessList($scope.experiment.accessList);

            var onAccessListChangedEvent = $scope.$on('access-list-changed', function (event) {
                $scope.experiment.accessList = PermissionManagement.getAccessList();
            });
            $scope.$on('$destroy', function () {
                onAccessListChangedEvent();
            });

            PermissionManagement.hasPermission('UPDATE_ENTITY').then(function (hasEditPermission) {
                $scope.isEditAllowed = pageInfo.isContentEditor || pageInfo.hasEditAuthority && hasEditPermission;
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

            var setStatus = function (status) {
                experiment.status = status;
            }

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


            $scope.$watch('experiment.status', function (newValue) {
                $scope.isEditAllowed = $scope.isStatusOpen();
            });

            var onCompleteSuccess = function (result) {
                onSaveSuccess(result);
                $rootScope.$broadcast('experiment-status-changed',
                    {projectId: $stateParams.projectId, notebookId: $stateParams.notebookId, id: result.id});
            };

            var onCompleteError = function (result) {
                onSaveError(result);
                setStatus(rememberStatus);
            };

            var rememberStatus = experiment.status;

            $scope.completeExperiment = function () {
                $uibModal.open({
                    animation: true,
                    templateUrl: 'scripts/app/entities/experiment/experiment-complete-modal.html',
                    controller: function ($scope, $uibModalInstance) {
                        $scope.fullExperimentName = function () {
                            return notebook.name + '-' + experiment.name;
                        };
                        $scope.dismiss = function () {
                            $uibModalInstance.dismiss('cancel');
                        };
                        $scope.confirmCompletion = function () {
                            $uibModalInstance.close(true);
                        };
                    }
                }).result.then(function () {
                    $scope.isSaving = true;
                    rememberStatus = experiment.status;
                    setStatus("Completed");
                    experiment.accessList = PermissionManagement.expandPermission(experiment.accessList);
                    var experimentForSave = _.extend({}, experiment);
                    $scope.loading = Experiment.update({
                        projectId: $stateParams.projectId,
                        notebookId: $stateParams.notebookId
                    }, experimentForSave, onCompleteSuccess, onCompleteError).$promise;
                });
            }

            $scope.isStatusOpen = function () {
                return experiment.status === 'Opened';
            };
            $scope.isStatusComplete = function () {
                return experiment.status === 'Completed';
            };
            $scope.isStatusSubmitFail = function () {
                return experiment.status === 'SubmitFailed';
            };
            $scope.isStatusSubmitted = function () {
                return experiment.status === 'Submitted';
            };
            $scope.isStatusArchieved = function () {
                return experiment.status === 'Archived';
            };
            $scope.isStatusSigned = function () {
                return experiment.status === 'Signed';
            };

        });
