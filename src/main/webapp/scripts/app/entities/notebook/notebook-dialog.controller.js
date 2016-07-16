angular.module('indigoeln')
    .controller('NotebookDialogController',
        function ($scope, $rootScope, $state, Notebook, Alert, PermissionManagement, pageInfo) {

            var identity = pageInfo.identity;
            var isContentEditor = pageInfo.isContentEditor;
            var hasEditAuthority = pageInfo.hasEditAuthority;
            var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;
            $scope.notebook = pageInfo.notebook;
            $scope.newNotebook = _.isUndefined($scope.notebook.id) || _.isNull($scope.notebook.id);
            $scope.notebook.author = $scope.notebook.author || identity;
            $scope.notebook.accessList = $scope.notebook.accessList || PermissionManagement.getAuthorAccessList(identity);
            $scope.projectId = pageInfo.projectId;
            $scope.isCollapsed = true;

            PermissionManagement.setEntity('Notebook');
            PermissionManagement.setEntityId($scope.notebook.id);
            PermissionManagement.setParentId($scope.projectId);
            PermissionManagement.setAuthor($scope.notebook.author);
            PermissionManagement.setAccessList($scope.notebook.accessList);

            var onAccessListChangedEvent = $scope.$on('access-list-changed', function () {
                $scope.notebook.accessList = PermissionManagement.getAccessList();
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
                Alert.success('Notebook successfully saved');
                $rootScope.$broadcast('notebook-created', {id: result.id, projectId: $scope.projectId});
                $state.go('entities.notebook-detail', {projectId: $scope.projectId, notebookId: result.id});
            };

            var onSaveError = function (result) {
                $scope.isSaving = false;
                Alert.error('Error saving notebook: ' + result);
            };

            $scope.save = function () {
                $scope.isSaving = true;
                if ($scope.notebook.id) {
                    Notebook.update({
                        projectId: $scope.projectId
                    }, $scope.notebook, onSaveSuccess, onSaveError);
                } else {
                    Notebook.save({
                        projectId: $scope.projectId
                    }, $scope.notebook, onSaveSuccess, onSaveError);
                }
            };
        });