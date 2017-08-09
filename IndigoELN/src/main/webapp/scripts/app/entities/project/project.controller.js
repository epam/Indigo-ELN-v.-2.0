(function() {
    angular
        .module('indigoeln')
        .controller('ProjectController', ProjectController);

    /* @ngInject */
    function ProjectController($scope, $rootScope, $state, Project, Alert, PermissionManagement, FileUploaderCash,
                               pageInfo, EntitiesBrowser, $timeout, $stateParams, TabKeyUtils, AutoRecoverEngine) {
        var vm = this;
        var identity = pageInfo.identity;
        var project = pageInfo.project;
        var isContentEditor = pageInfo.isContentEditor;
        var hasEditAuthority = pageInfo.hasEditAuthority;
        var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;

        init();

        function init() {
            vm.isBtnSaveActive = false;
            vm.project = project;
            vm.project.author = vm.project.author || identity;
            vm.project.accessList = vm.project.accessList || PermissionManagement.getAuthorAccessList(identity);

            vm.save = save;
            vm.refresh = refresh;
            vm.updateAttachments = updateAttachments;
            vm.onChanged = onChanged;

            EntitiesBrowser.setSaveCurrentEntity(save);
            EntitiesBrowser.setUpdateCurrentEntity(refresh);
            EntitiesBrowser.setCurrentTabTitle(project.name, $stateParams);

            initPermissions();
            initDirtyListener();

            if (!vm.project.id) {
                FileUploaderCash.setFiles([]);
            }

            $scope.$on('access-list-changed', function() {
                vm.project.accessList = PermissionManagement.getAccessList();
            });

            $scope.$watch('vm.project', function() {
                EntitiesBrowser.setCurrentEntity(vm.project);
            });

            // Activate save button when change permission
            $scope.$on('activate button', function() {
                // If put 0, then save button isn't activated
                $timeout(function() {
                    vm.isBtnSaveActive = true;
                }, 10);
            });
        }

        function onChanged() {
            EntitiesBrowser.changeDirtyTab($stateParams, true);
        }

        function updateAttachments() {
            vm.loading = Project.get($stateParams).$promise
                .then(function(result) {
                    vm.project.version = result.version;
                }, function() {
                    Alert.error('Project not refreshed due to server error!');
                });
        }

        function save() {
            if (vm.project.id) {
                vm.loading = Project.update($stateParams, vm.project).$promise
                    .then(function(result) {
                        vm.project.version = result.version;
                        $scope.createProjectForm.$setPristine();
                        EntitiesBrowser.changeDirtyTab($stateParams, false);
                        onUpdateSuccess({
                            id: vm.project.id
                        });
                    }, onSaveError);
            } else {
                vm.loading = Project.save(vm.project, onSaveSuccess, onSaveError).$promise;
            }
        }

        function refresh() {
            vm.loading = Project.get($stateParams).$promise
                .then(function(result) {
                    angular.extend(vm.project, result);
                    $scope.createProjectForm.$setPristine();
                    $scope.createProjectForm.$dirty = false;
                    EntitiesBrowser.changeDirtyTab($stateParams, false);
                }, function() {
                    Alert.error('Project not refreshed due to server error!');
                });
        }

        function initPermissions() {
            PermissionManagement.setEntity('Project');
            PermissionManagement.setEntityId(vm.project.id);
            PermissionManagement.setAuthor(vm.project.author);
            PermissionManagement.setAccessList(vm.project.accessList);

            PermissionManagement.hasPermission('UPDATE_ENTITY').then(function(hasEditPermission) {
                vm.isEditAllowed = isContentEditor || (hasEditAuthority && hasEditPermission);
            });

            PermissionManagement.hasPermission('CREATE_SUB_ENTITY').then(function(hasCreateChildPermission) {
                vm.isCreateChildAllowed = isContentEditor || (hasCreateChildAuthority && hasCreateChildPermission);
            });
        }

        function initDirtyListener() {
            $timeout(function() {
                AutoRecoverEngine.track({
                    vm: vm,
                    kind: $state.$current.data.tab.kind,
                    onSetDirty: function() {
                        $scope.createProjectForm.$setDirty();
                    }
                });

                if (pageInfo.dirty) {
                    $scope.createProjectForm.$setDirty();
                }

                $scope.$watch('vm.project', function(newValue, oldValue) {
                    EntitiesBrowser.setCurrentForm($scope.createProjectForm);
                    AutoRecoverEngine.tracker.change(newValue, oldValue);
                    if (EntitiesBrowser.getActiveTab().name === 'New Project') {
                        vm.isBtnSaveActive = true;
                    } else {
                        $timeout(function() {
                            vm.isBtnSaveActive = EntitiesBrowser.getActiveTab().dirty;
                        }, 0);
                    }
                }, true);

                $scope.$watch('createProjectForm.$dirty', function(newValue, oldValue) {
                    AutoRecoverEngine.tracker.changeDirty(newValue);
                });
            }, 0, false);
        }

        function onUpdateSuccess(result) {
            $rootScope.$broadcast('project-created', {
                id: result.id
            });
            $state.go('entities.project-detail', {
                projectId: result.id
            });
        }

        function onSaveSuccess(result) {
            var tabName = $state.$current.data.tab.name;
            EntitiesBrowser.close(TabKeyUtils.getTabKeyFromName(tabName));
            $timeout(function() {
                $rootScope.$broadcast('project-created', {
                    id: result.id
                });
                $state.go('entities.project-detail', {
                    projectId: result.id
                });
            });
        }

        function onSaveError(result) {
            var mess = (result.status === 400) ? 'this Project name is already in use in the system'
                : 'Project is not saved due to server error';
            Alert.error(mess);
        }
    }
})();
