'use strict';

angular.module('indigoeln')
    .controller('ProjectDialogController',
        function ($scope, $rootScope, $state, Project, AlertService, PermissionManagement, project,
                  identity, isContentEditor, hasEditAuthority, hasCreateChildAuthority) {

            $scope.project = project;
            $scope.project.author = $scope.project.author || identity;
            $scope.project.accessList = $scope.project.accessList || PermissionManagement.getAuthorAccessList(identity);

            PermissionManagement.setEntity('Project');
            PermissionManagement.setAuthor($scope.project.author);
            PermissionManagement.setAccessList($scope.project.accessList);

            var onAccessListChangedEvent = $scope.$on('access-list-changed', function(event) {
                $scope.project.accessList = PermissionManagement.getAccessList();
            });
            $scope.$on('$destroy', function() {
                onAccessListChangedEvent();
            });

            // isEditAllowed
            PermissionManagement.hasPermission('UPDATE_ENTITY').then(function (hasEditPermission) {
                $scope.isEditAllowed = isContentEditor || hasEditAuthority && hasEditPermission;
            });
            // isCreateChildAllowed
            PermissionManagement.hasPermission('CREATE_SUB_ENTITY').then(function (hasCreateChildPermission) {
                $scope.isCreateChildAllowed = isContentEditor || hasCreateChildAuthority && hasCreateChildPermission;
            });

            $scope.show = function(form) {
                if ($scope.isEditAllowed) {
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