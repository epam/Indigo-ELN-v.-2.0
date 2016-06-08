angular.module('indigoeln')
    .controller('ProjectDialogController',
        function ($scope, $rootScope, $state, Project, Alert, PermissionManagement, FileUploaderCash, pageInfo) {
            var identity = pageInfo.identity;
            var project = pageInfo.project;
            var isContentEditor = pageInfo.isContentEditor;
            var hasEditAuthority = pageInfo.hasEditAuthority;
            var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;
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
                $scope.project.tags = [];
                $scope.project.fileIds = _.pluck(FileUploaderCash.getFiles(), 'id');
                if ($scope.tags) {
                    angular.forEach($scope.tags, function (tag) {
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