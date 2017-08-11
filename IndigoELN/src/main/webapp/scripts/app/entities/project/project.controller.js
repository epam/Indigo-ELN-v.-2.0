(function() {
    angular
        .module('indigoeln')
        .controller('ProjectController', ProjectController);

    /* @ngInject */
    function ProjectController($scope, $rootScope, $state, Project, notifyService, PermissionManagement, FileUploaderCash,
                               pageInfo, EntitiesBrowser, $timeout, $stateParams, TabKeyUtils, autorecoveryHelper) {
        var vm = this;
        var identity = pageInfo.identity;
        var project = pageInfo.project;
        var isContentEditor = pageInfo.isContentEditor;
        var hasEditAuthority = pageInfo.hasEditAuthority;
        var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;
        var updateRecovery;
        var originalProject;

        init();

        function init() {
            updateRecovery = autorecoveryHelper.getUpdateRecoveryDebounce($stateParams);
            vm.stateData = $state.current.data;
            vm.isBtnSaveActive = false;
            vm.project = project;

            vm.project.author = vm.project.author || identity;
            vm.project.accessList = vm.project.accessList || PermissionManagement.getAuthorAccessList(identity);
            originalProject = angular.copy(vm.project);

            vm.print = print;
            vm.save = save;
            vm.refresh = refresh;
            vm.updateAttachments = updateAttachments;
            vm.onChanged = onChanged;
            vm.onRestore = onRestore;

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

            // Activate save button when change permission
            $scope.$on('activate button', function() {
                // If put 0, then save button isn't activated
                $timeout(function() {
                    vm.isBtnSaveActive = true;
                }, 10);
            });
        }

        function onRestore(storeData) {
            vm.project = storeData;
        }

        function onChanged() {
            EntitiesBrowser.changeDirtyTab($stateParams, true);
        }

        function updateAttachments() {
            vm.loading = Project.get($stateParams).$promise
                .then(function(result) {
                    vm.project.version = result.version;
                }, function() {
                    notifyService.error('Project not refreshed due to server error!');
                });
        }

        function save() {
            if (vm.project.id) {
                vm.loading = Project.update($stateParams, vm.project).$promise
                    .then(function(result) {
                        vm.project.version = result.version;
                        originalProject = angular.copy(vm.project);
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
                    originalProject = angular.copy(vm.project);
                }, function() {
                    notifyService.error('Project not refreshed due to server error!');
                });
        }

        function print() {
            $state.go('entities.project-detail.print');
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

        function toggleDirty(isDirty) {
            if (!$scope.createProjectForm) {
                return;
            }

            var isChanged = _.isBoolean(isDirty) ? isDirty : !$scope.createProjectForm.$dirty;

            if (isChanged) {
                $scope.createProjectForm.$setDirty();
            } else {
                $scope.createProjectForm.$setPristine();
            }
            vm.isBtnSaveActive = $scope.createProjectForm.$dirty;
            EntitiesBrowser.changeDirtyTab($stateParams, isChanged);
        }

        function initDirtyListener() {
            $timeout(function() {
                $scope.$watch('vm.project', function(newEntity) {
                    EntitiesBrowser.setCurrentEntity(vm.project);
                    var isDirty = vm.stateData.isNew || autorecoveryHelper.isEntityDirty(originalProject, newEntity);
                    toggleDirty(isDirty);
                    updateRecovery(newEntity, isDirty);
                }, true);

                $scope.$watch('createProjectForm.$dirty', function(isDirty) {
                    vm.isBtnSaveActive = isDirty;
                });

                $scope.$watch('createProjectForm', function(newValue) {
                    EntitiesBrowser.setCurrentForm(newValue);
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
            notifyService.error(mess);
        }
    }
})();
