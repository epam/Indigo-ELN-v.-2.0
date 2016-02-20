'use strict';

angular.module('indigoeln')
    .controller('ProjectDialogController',
        function($scope, $rootScope, $uibModal, $state, Project, AlertService, Principal,
                 PermissionManagement, project, identity, hasEditAuthority) {

            $scope.CONTENT_EDITOR = 'CONTENT_EDITOR';
            $scope.NOTEBOOK_CREATOR = 'NOTEBOOK_CREATOR';
            $scope.NOTEBOOK_CREATORS = [$scope.CONTENT_EDITOR, $scope.NOTEBOOK_CREATOR].join(',');

            $scope.project = project;
            $scope.project.author = $scope.project.author || identity;
            $scope.project.accessList = $scope.project.accessList ||
                [{user: identity, permissions: [], permissionView: 'OWNER'}];

            PermissionManagement.setAuthor($scope.project.author);
            PermissionManagement.setAccessList($scope.project.accessList);

            var onAccessListChangedEvent = $scope.$on('access-list-changed', function(event) {
                $scope.project.accessList = PermissionManagement.getAccessList();
            });
            $scope.$on('$destroy', function() {
                onAccessListChangedEvent();
            });

            // isEditEnabled
            if (hasEditAuthority) {
                $scope.isEditEnabled = hasEditAuthority;
            } else {
                PermissionManagement.hasPermission('UPDATE_ENTITY').then(function (result) {
                    $scope.isEditEnabled = result;
                });
            }

            $scope.show = function(form) {
                if (!$scope.editDisabled) {
                    form.$show();
                }
            };

            $scope.tags = [];
            angular.forEach($scope.project.tags, function(tag) {
                $scope.tags.push({ text: tag});
            });
            $scope.keywords = '';
            if ($scope.project.keywords) {
                $scope.keywords = $scope.project.keywords.join(', ');
            }

            var onSaveSuccess = function (result) {
                $scope.isSaving = false;
                AlertService.success('Project successfully saved');
                $rootScope.$broadcast('project-created', {id: result.id});
                $state.go('entities.project-detail', {projectId: result.id});
            };

            var onSaveError = function (result) {
                $scope.isSaving = false;
                AlertService.error('Error saving project: ' + result);
            };

            $scope.save = function () {
                $scope.isSaving = true;
                $scope.project.tags = [];
                if ($scope.tags) {
                    angular.forEach($scope.tags, function(tag, key) {
                        $scope.project.tags.push(tag.text);
                    });
                }
                $scope.project.keywords = [];
                if ($scope.keywords) {
                    angular.forEach($scope.keywords.split(','), function(ref) {
                        $scope.project.keywords.push(ref.trim());
                    });
                }
                $scope.project.accessList = PermissionManagement.expandPermission($scope.project.accessList);

                if ($scope.project.id) {
                    Project.update($scope.project, onSaveSuccess, onSaveError);
                } else {
                    Project.save($scope.project, onSaveSuccess, onSaveError);
                }
            };
        });