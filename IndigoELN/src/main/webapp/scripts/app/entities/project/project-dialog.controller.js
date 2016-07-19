angular.module('indigoeln')
    .controller('ProjectDialogController',
        function ($scope, $rootScope, $state, Project, Alert, PermissionManagement, FileUploaderCash, pageInfo, EntitiesBrowser, $timeout) {
            var identity = pageInfo.identity;
            var project = pageInfo.project;
            var isContentEditor = pageInfo.isContentEditor;
            var hasEditAuthority = pageInfo.hasEditAuthority;
            var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;
            $timeout(function () {
                EntitiesBrowser.trackEntityChanges($scope.createProjectForm, $scope);
            }, 0, false);
            $scope.project = project;
            $scope.newProject = _.isUndefined($scope.project.id) || _.isNull($scope.project.id);
            $scope.project.author = $scope.project.author || identity;
            $scope.project.accessList = $scope.project.accessList || PermissionManagement.getAuthorAccessList(identity);
            $scope.isCollapsed = true;
            if (!$scope.project.id) {
                FileUploaderCash.setFiles([]);
            }

            PermissionManagement.setEntity('Project');
            PermissionManagement.setEntityId($scope.project.id);
            PermissionManagement.setAuthor($scope.project.author);
            PermissionManagement.setAccessList($scope.project.accessList);

            var onAccessListChangedEvent = $scope.$on('access-list-changed', function () {
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

            var onSaveSuccess = function (result) {
                $scope.isSaving = false;
                Alert.success('Project successfully saved');
                $rootScope.$broadcast('project-created', {id: result.id});
                $state.go('entities.project-detail', {projectId: result.id});
            };

            var onSaveError = function (result) {
                $scope.isSaving = false;
                Alert.error('Error saving project: ' + result);
            };

            $scope.save = function () {
                $scope.isSaving = true;
                if ($scope.project.id) {
                    Project.update($scope.project, onSaveSuccess, onSaveError);
                } else {
                    Project.save($scope.project, onSaveSuccess, onSaveError);
                }
            };

        });