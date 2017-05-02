angular.module('indigoeln')
    .controller('NotebookDialogController',
        function ($scope, $rootScope, $state, Notebook, Alert, PermissionManagement,
                  ExperimentUtil, pageInfo, EntitiesBrowser, $timeout, $stateParams, AutoSaveEntitiesEngine, TabKeyUtils, AutoRecoverEngine) {
            var self = this;
            EntitiesBrowser.setCurrentTabTitle(pageInfo.notebook.name, $stateParams);
            var identity = pageInfo.identity;
            var isContentEditor = pageInfo.isContentEditor;
            var hasEditAuthority = pageInfo.hasEditAuthority;
            var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;
            $scope.experiments = pageInfo.experiments;
            $scope.isBtnSaveActive = false;
            $timeout(function () {
                var tabKind = $state.$current.data.tab.kind;
                if(pageInfo.dirty){
                    $scope.createNotebookForm.$setDirty(pageInfo.dirty);
                }

                self.dirtyListener = $scope.$watch(tabKind, function (oldValue, newValue) {
                    EntitiesBrowser.changeDirtyTab($stateParams, $scope.createNotebookForm.$dirty);
                    if (EntitiesBrowser.getActiveTab().name == "New Notebook") {
                        $scope.isBtnSaveActive = true;
                    } else {
                        $timeout(function(){
                                $scope.isBtnSaveActive = EntitiesBrowser.getActiveTab().dirty;
                        }, 0);
                    }
                }, true);
                AutoRecoverEngine.trackEntityChanges(pageInfo.notebook, $scope.createNotebookForm, $scope, tabKind);

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
                self.dirtyListener();
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

            $scope.repeatExperiment = function (experiment, params) {
                ExperimentUtil.repeatExperiment(experiment, params);
            };

            var onSaveSuccess = function (result) {

                EntitiesBrowser.close(TabKeyUtils.getTabKeyFromParams($stateParams));

                $timeout(function(){
                    $rootScope.$broadcast('notebook-created', {id: result.id, projectId: $scope.projectId});
                    $state.go('entities.notebook-detail', {projectId: $scope.projectId, notebookId: result.id});
                });
            };

            var onSaveError = function (result) {
                var mess =  (result.status == 400) ? 'This Notebook name is already in use in the system' : 'Notebook is not saved due to server error';
                Alert.error(mess);
            };
            $scope.refresh = function () {
               $scope.loading = Notebook.get($stateParams).$promise
                .then(function (result) {
                    angular.extend($scope.notebook, result);
                    $scope.createNotebookForm.$setPristine();
                    $scope.createNotebookForm.$dirty = false;
                    EntitiesBrowser.changeDirtyTab($stateParams, false);
                }, function () {
                    Alert.error('Notebook not refreshed due to server error!')
                });
            };
            $scope.save = function () {
                if ($scope.notebook.id) {
                    $scope.loading = Notebook.update($stateParams, $scope.notebook).$promise
                        .then(function (result) {
                            $scope.notebook.version = result.version;
                            $scope.createNotebookForm.$setPristine();
                        }, onSaveError);

                } else {
                    $scope.loading = Notebook.save({
                        projectId: $scope.projectId
                    }, $scope.notebook, onSaveSuccess, onSaveError).$promise;
                }
            };

        });