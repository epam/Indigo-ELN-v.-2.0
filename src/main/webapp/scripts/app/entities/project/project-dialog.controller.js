angular.module('indigoeln')
    .controller('ProjectDialogController',
        function ($scope, $rootScope, $state, Project, Alert,
                  PermissionManagement, FileUploaderCash, pageInfo, EntitiesBrowser, $timeout, $stateParams, AutoSaveEntitiesEngine, TabKeyUtils, AutoRecoverEngine) {
            var self = this;
            EntitiesBrowser.setCurrentTabTitle(pageInfo.project.name, $stateParams);
            var identity = pageInfo.identity;
            var project = pageInfo.project;
            var isContentEditor = pageInfo.isContentEditor;
            var hasEditAuthority = pageInfo.hasEditAuthority;
            var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;
            $scope.isBtnSaveActive = false;
            $timeout(function () {

                var tabKind = $state.$current.data.tab.kind;
                if(pageInfo.dirty){
                    $scope.createProjectForm.$setDirty(pageInfo.dirty);
                }
                self.dirtyListener = $scope.$watch(tabKind, function (oldValue, newValue) {
                    EntitiesBrowser.changeDirtyTab($stateParams, $scope.createProjectForm.$dirty);
                    EntitiesBrowser.setCurrentForm($scope.createProjectForm)
                    if (EntitiesBrowser.getActiveTab().name == "New Project"){
                        $scope.isBtnSaveActive = true;
                    } else {
                        $timeout(function() {
                            $scope.isBtnSaveActive = EntitiesBrowser.getActiveTab().dirty;
                        }, 0);
                    }
                }, true);

                AutoRecoverEngine.trackEntityChanges(project, $scope.createProjectForm, $scope, tabKind);

            }, 0, false);
            $scope.project = project;
            $scope.newProject = _.isUndefined($scope.project.id) || _.isNull($scope.project.id);
            $scope.project.author = $scope.project.author || identity;
            $scope.project.accessList = $scope.project.accessList || PermissionManagement.getAuthorAccessList(identity);
            $scope.isCollapsed = true;
            if (!$scope.project.id) {
                FileUploaderCash.setFiles([]);
            }

            //Activate save button when change permission
            $scope.$on("activate button", function(){
                $timeout(function() {
                    $scope.isBtnSaveActive = true;
                }, 10); //If put 0, then save button isn't activated
            });

            PermissionManagement.setEntity('Project');
            PermissionManagement.setEntityId($scope.project.id);
            PermissionManagement.setAuthor($scope.project.author);
            PermissionManagement.setAccessList($scope.project.accessList);

            var onAccessListChangedEvent = $scope.$on('access-list-changed', function () {
                $scope.project.accessList = PermissionManagement.getAccessList();
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

            var onUpdateSuccess = function (result) {
                $rootScope.$broadcast('project-created', {id: result.id});
                $state.go('entities.project-detail', {projectId: result.id});
            };

            var onSaveSuccess = function (result) {

                var tabName = $state.$current.data.tab.name;
                EntitiesBrowser.close(TabKeyUtils.getTabKeyFromName(tabName));
                $timeout(function(){
                    $rootScope.$broadcast('project-created', {id: result.id});
                    $state.go('entities.project-detail', {projectId: result.id});
                });
            };

            var onSaveError = function (result) {
                var mess =  (result.status == 400) ? 'this Project name is already in use in the system' : 'Project is not saved due to server error';
                Alert.error(mess);
            };

            $scope.save = function () {
                if ($scope.project.id) {
                    $scope.loading = Project.update($stateParams, $scope.project).$promise
                                 .then(function (result) {
                                     $scope.project.version = result.version;
                                     $scope.createProjectForm.$setPristine();
                                     onUpdateSuccess({id: $scope.project.id});
                                 }, onSaveError);
                } else {
                    $scope.loading = Project.save($scope.project, onSaveSuccess, onSaveError).$promise;
                }
            };

            $scope.refresh = function () {
               $scope.loading = Project.get($stateParams).$promise
                .then(function (result) {
                    angular.extend($scope.project, result);
                    $scope.createProjectForm.$setPristine();
                    $scope.createProjectForm.$dirty = false;
                    EntitiesBrowser.changeDirtyTab($stateParams, false);
                }, function () {
                    Alert.error('Project not refreshed due to server error!')
                });
            };

            //This is necessary for correct saving after attaching files
            $scope.$on("refresh after attach", function() {
                $scope.loading = Project.get($stateParams).$promise
                    .then(function (result) {
                             $scope.project.version = result.version;
                    }, function () {
                        Alert.error('Project not refreshed due to server error!')
                    });
            });
            var unsubscribeExp = $scope.$watch('project', function () {
                EntitiesBrowser.setCurrentEntity($scope.project);
            });

            $scope.$on('$destroy', function () {
                unsubscribeExp();
            });

        });