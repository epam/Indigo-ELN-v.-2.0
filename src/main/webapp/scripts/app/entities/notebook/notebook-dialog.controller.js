'use strict';

angular.module('indigoeln')
    .controller('NotebookDialogController',
        function($scope, $rootScope, $stateParams, $uibModal, $state, Notebook, AlertService, Principal, PermissionManagement, notebook, identity) {

            $scope.notebook = notebook;
            $scope.notebook.author = $scope.notebook.author || identity;
            $scope.notebook.accessList = $scope.notebook.accessList ||
                [{user: identity, permissions: [], permissionView: 'OWNER'}];

            PermissionManagement.setAuthor($scope.notebook.author);
            PermissionManagement.setAccessList($scope.notebook.accessList);

            $scope.$on('access-list-changed', function(event) {
                $scope.notebook.accessList = PermissionManagement.getAccessList();
            });

            $scope.editDisabled = !Principal.hasAuthority('CONTENT_EDITOR');// && $scope.accessList; // todo
            $scope.show = function(form) {
                if (!$scope.editDisabled) {
                    form.$show();
                }
            };

            var onSaveSuccess = function (result) {
                $scope.isSaving = false;
                AlertService.success('Notebook successfully saved');
                $rootScope.$broadcast('notebook-created', {id: result.id});
                $state.go('notebook', {projectId: $stateParams.projectId, id: result.id});
            };

            var onSaveError = function (result) {
                $scope.isSaving = false;
                AlertService.error('Error saving notebook: ' + result);
            };

            $scope.save = function () {
                $scope.isSaving = true;
                $scope.notebook.accessList = PermissionManagement.expandPermission($scope.notebook.accessList);

                if ($scope.notebook.id) {
                    Notebook.update($scope.notebook, onSaveSuccess, onSaveError);
                } else {
                    Notebook.save($scope.notebook, onSaveSuccess, onSaveError);
                }
            };

            $scope.newExperiment = function(event) {
                var modalInstance = $uibModal.open({
                    animation: true,
                    templateUrl: 'scripts/app/entities/notebook/new/dialog/new-notebook-dialog.html',
                    controller: 'NewNotebookDialogController',
                    size: 'lg'
                });
                modalInstance.result.then(function (experimentName) {
                    $rootScope.$broadcast('created-experiment', {experimentName: experimentName});
                    $state.go('experiment.new', {notebookId:notebook.id});
                }, function () {
                });
            };
        });