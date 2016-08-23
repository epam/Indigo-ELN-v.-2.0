angular.module('indigoeln')
    .controller('NotebookDialogController',
        function ($scope, $rootScope, $state, Notebook, Alert, PermissionManagement, pageInfo, EntitiesBrowser, $timeout) {

            var identity = pageInfo.identity;
            var isContentEditor = pageInfo.isContentEditor;
            var hasEditAuthority = pageInfo.hasEditAuthority;
            var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;
            $timeout(function () {
                EntitiesBrowser.trackEntityChanges($scope.createNotebookForm, $scope, pageInfo.notebook);
            }, 0, false);
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
                $rootScope.$broadcast('notebook-created', {id: result.id, projectId: $scope.projectId});
                $state.go('entities.notebook-detail', {projectId: $scope.projectId, notebookId: result.id});
            };

            var onSaveError = function (result) {
                Alert.error('Error saving notebook: ' + result);
            };

            $scope.save = function () {
                if ($scope.notebook.id) {
                    $scope.loading = EntitiesBrowser.saveEntity($scope.notebook.fullId).then(onSaveSuccess.bind(null, {id: $scope.notebook.id}));
                } else {
                    $scope.loading = Notebook.save({
                        projectId: $scope.projectId
                    }, $scope.notebook, onSaveSuccess, onSaveError).$promise;
                }
            };
        });