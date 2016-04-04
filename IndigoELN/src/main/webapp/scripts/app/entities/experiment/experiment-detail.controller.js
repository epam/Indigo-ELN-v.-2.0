'use strict';

angular.module('indigoeln')
    .controller('ExperimentDetailController',
        function ($scope, $rootScope, $stateParams, $state, Experiment, Principal, PermissionManagement, pageInfo,
                  SignatureTemplates, $uibModal) {

            // TODO: the Action drop up button should be disable in case of there is unsaved data.

            $scope.statuses = ['Open', 'Completed', 'Submit_Fail', 'Submitted', 'Archived', 'Signing', 'Signed'];

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
                $scope.experiment.status = status;
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

            $scope.$watch('experiment.status', function (newValue) {
                $scope.isEditAllowed = $scope.isStatusOpen();
            });

            var onChangeStatusSuccess = function (result) {
                onSaveSuccess(result);
                $rootScope.$broadcast('experiment-status-changed',
                    {projectId: $stateParams.projectId, notebookId: $stateParams.notebookId, id: result.id});
            };

            var onChangeStatusError = function (result) {
                onSaveError(result);
                setStatus(rememberStatus);
            };

            var rememberStatus = $scope.experiment.status;

            var openCompleteConfirmationModal = function () {
                return $uibModal.open({
                    animation: true,
                    templateUrl: 'scripts/app/entities/experiment/experiment-complete-modal.html',
                    resolve: {
                        fullExperimentName: function () {
                            return $scope.notebook.name + '-' + $scope.experiment.name;
                        }
                    },
                    controller: function ($scope, $uibModalInstance, fullExperimentName) {
                        $scope.fullExperimentName = fullExperimentName;
                        $scope.dismiss = function () {
                            $uibModalInstance.dismiss('cancel');
                        };
                        $scope.confirmCompletion = function () {
                            $uibModalInstance.close(true);
                        };
                    }
                });
            };

            $scope.completeExperiment = function () {
                openCompleteConfirmationModal().result.then(function () {
                    $scope.isSaving = true;
                    rememberStatus = $scope.experiment.status;
                    setStatus('Completed');
                    $scope.experiment.accessList = PermissionManagement.expandPermission($scope.experiment.accessList);
                    var experimentForSave = _.extend({}, $scope.experiment);
                    $scope.loading = Experiment.update({
                        projectId: $stateParams.projectId,
                        notebookId: $stateParams.notebookId
                    }, experimentForSave, onChangeStatusSuccess, onChangeStatusError).$promise;
                });
            };

            $scope.completeExperimentAndSign = function () {
                openCompleteConfirmationModal().result.then(function () {
                    // show PDF preview
                    $state.go('experiment-preview-submit', {
                            experimentId: $scope.experiment.id,
                            notebookId: $scope.notebook.id,
                            projectId: $scope.notebook.parentId
                        });
                    //$state.go('entities.experiment-detail.preview-submit');
                });
            };

            $scope.reopenExperiment = function () {
                $scope.isSaving = true;
                rememberStatus = $scope.experiment.status;
                setStatus('Open');
                $scope.experiment.accessList = PermissionManagement.expandPermission($scope.experiment.accessList);
                var experimentForSave = _.extend({}, $scope.experiment);
                $scope.loading = Experiment.update({
                    projectId: $stateParams.projectId,
                    notebookId: $stateParams.notebookId
                }, experimentForSave, onChangeStatusSuccess, onChangeStatusError).$promise;
            };

            $scope.isStatusOpen = function () {
                return $scope.experiment.status === 'Open';
            };
            $scope.isStatusComplete = function () {
                return $scope.experiment.status === 'Completed';
            };
            $scope.isStatusSubmitFail = function () {
                return $scope.experiment.status === 'Submit_Fail';
            };
            $scope.isStatusSubmitted = function () {
                return $scope.experiment.status === 'Submitted';
            };
            $scope.isStatusArchieved = function () {
                return $scope.experiment.status === 'Archived';
            };
            $scope.isStatusSigned = function () {
                return $scope.experiment.status === 'Signed';
            };
            $scope.isStatusSigning = function () {
                return $scope.experiment.status === 'Signing';
            };

        });
