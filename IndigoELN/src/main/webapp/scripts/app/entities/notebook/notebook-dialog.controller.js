'use strict';

angular.module('indigoeln')
    .controller('NotebookDialogController',
        function($scope, $rootScope, $stateParams, $uibModal, $state, Notebook, AlertService, Principal, PermissionManagement, notebook, identity) {

            $scope.CONTENT_EDITOR = 'CONTENT_EDITOR';
            $scope.EXPERIMENT_CREATOR = 'EXPERIMENT_CREATOR';
            $scope.EXPERIMENT_CREATORS = [$scope.CONTENT_EDITOR, $scope.EXPERIMENT_CREATOR].join(',');

            $scope.notebook = notebook;
            $scope.notebook.author = $scope.notebook.author || identity;
            $scope.notebook.accessList = $scope.notebook.accessList ||
                [{user: identity, permissions: [], permissionView: 'OWNER'}];
            $scope.projectId = $stateParams.projectId;

            PermissionManagement.setAuthor($scope.notebook.author);
            PermissionManagement.setAccessList($scope.notebook.accessList);

            var onAccessListChangedEvent = $scope.$on('access-list-changed', function(event) {
                $scope.notebook.accessList = PermissionManagement.getAccessList();
            });
            $scope.$on('$destroy', function() {
                onAccessListChangedEvent();
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
                $rootScope.$broadcast('notebook-created', {id: result.id, projectId: $scope.projectId});
                $state.go('entities.notebook-detail', {projectId: $stateParams.projectId, notebookId: result.id});
            };

            var onSaveError = function (result) {
                $scope.isSaving = false;
                AlertService.error('Error saving notebook: ' + result);
            };

            $scope.save = function () {
                $scope.isSaving = true;
                $scope.notebook.accessList = PermissionManagement.expandPermission($scope.notebook.accessList);

                if ($scope.notebook.id) {
                    Notebook.update({
                        projectId: $stateParams.projectId
                    }, $scope.notebook, onSaveSuccess, onSaveError);
                } else {
                    Notebook.save({
                        projectId: $stateParams.projectId
                    }, $scope.notebook, onSaveSuccess, onSaveError);
                }
            };
        });