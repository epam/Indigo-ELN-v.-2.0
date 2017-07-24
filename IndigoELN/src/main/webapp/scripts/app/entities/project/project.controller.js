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

        vm.isBtnSaveActive = false;
        vm.project = project;
        vm.newProject = _.isUndefined(vm.project.id) || _.isNull(vm.project.id);
        vm.project.author = vm.project.author || identity;
        vm.project.accessList = vm.project.accessList || PermissionManagement.getAuthorAccessList(identity);
        vm.isCollapsed = true;

        vm.show = show;
        vm.save = save;
        vm.refresh = refresh;

        init();

        function show(form) {
            if (vm.isEditAllowed) {
                form.$show();
            }
        }

        function save() {
            if (vm.project.id) {
                vm.loading = Project.update($stateParams, vm.project).$promise
                    .then(function(result) {
                        vm.project.version = result.version;
                        $scope.createProjectForm.$setPristine();
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

        function init() {
            initPermissions();

            EntitiesBrowser.setSaveCurrentEntity(save);
            EntitiesBrowser.setUpdateCurrentEntity(refresh);
            EntitiesBrowser.setCurrentTabTitle(project.name, $stateParams);

            initDirtyListener();

            if (!vm.project.id) {
                FileUploaderCash.setFiles([]);
            }

            var onAccessListChangedEvent = $scope.$on('access-list-changed', function() {
                vm.project.accessList = PermissionManagement.getAccessList();
            });

            var unsubscribeExp = $scope.$watch('vm.project', function() {
                EntitiesBrowser.setCurrentEntity(vm.project);
            });

            // Activate save button when change permission
            $scope.$on('activate button', function() {
                // If put 0, then save button isn't activated
                $timeout(function() {
                    vm.isBtnSaveActive = true;
                }, 10);
            });

            // This is necessary for correct saving after attaching files
            $scope.$on('refresh after attach', function() {
                vm.loading = Project.get($stateParams).$promise
                    .then(function(result) {
                        vm.project.version = result.version;
                    }, function() {
                        Alert.error('Project not refreshed due to server error!');
                    });
            });

            $scope.$on('$destroy', function() {
                unsubscribeExp();
                onAccessListChangedEvent();
                vm.dirtyListener();
                vm.formDirtyListener();
            });
        }

        function initPermissions() {
            PermissionManagement.setEntity('Project');
            PermissionManagement.setEntityId(vm.project.id);
            PermissionManagement.setAuthor(vm.project.author);
            PermissionManagement.setAccessList(vm.project.accessList);

            // isEditAllowed
            PermissionManagement.hasPermission('UPDATE_ENTITY').then(function(hasEditPermission) {
                vm.isEditAllowed = isContentEditor || hasEditAuthority && hasEditPermission;
            });
            // isCreateChildAllowed
            PermissionManagement.hasPermission('CREATE_SUB_ENTITY').then(function(hasCreateChildPermission) {
                vm.isCreateChildAllowed = isContentEditor || hasCreateChildAuthority && hasCreateChildPermission;
            });
        }

        function initDirtyListener() {
            $timeout(function() {
                var tabKind = $state.$current.data.tab.kind;
                AutoRecoverEngine.track({
                    vm: vm,
                    kind: tabKind,
                    onSetDirty: function() {
                        $scope.createProjectForm.$setDirty();
                    }
                });
                if (pageInfo.dirty) {
                    $scope.createProjectForm.$setDirty();
                }
                vm.dirtyListener = $scope.$watch(function() {
                    return vm.project;
                }, function(entity, old) {
                    EntitiesBrowser.setCurrentForm($scope.createProjectForm);
                    AutoRecoverEngine.tracker.change(entity, old);
                    if (EntitiesBrowser.getActiveTab().name === 'New Project') {
                        vm.isBtnSaveActive = true;
                    } else {
                        $timeout(function() {
                            vm.isBtnSaveActive = EntitiesBrowser.getActiveTab().dirty;
                        }, 0);
                    }
                }, true);
                vm.formDirtyListener = $scope.$watch('createProjectForm.$dirty', function(cur, old) {
                    AutoRecoverEngine.tracker.changeDirty(cur, old);
                }, true);
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
            var mess = (result.status === 400) ? 'this Project name is already in use in the system' : 'Project is not saved due to server error';
            Alert.error(mess);
        }
    }
})();