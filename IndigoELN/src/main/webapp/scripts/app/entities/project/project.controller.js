(function() {
    angular
        .module('indigoeln')
        .controller('ProjectController', ProjectController);

    /* @ngInject */
    function ProjectController($scope, $rootScope, $state, Project, notifyService, PermissionManagement, FileUploaderCash,
                               pageInfo, EntitiesBrowser, $timeout, $stateParams, TabKeyUtils, autorecoveryHelper,
                               autorecoveryCache, EntitiesCache, confirmationModal, $q, entityHelper) {
        var vm = this;
        var identity = pageInfo.identity;
        var isContentEditor = pageInfo.isContentEditor;
        var hasEditAuthority = pageInfo.hasEditAuthority;
        var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;
        var updateRecovery;
        var originalProject;
        var isChanged;
        var entityTitle;

        init();

        function init() {
            updateRecovery = autorecoveryHelper.getUpdateRecoveryDebounce($stateParams);
            entityTitle = pageInfo.project.name;

            vm.stateData = $state.current.data;
            vm.isBtnSaveActive = false;

            vm.loading = initEntity().then(function() {
                originalProject = angular.copy(pageInfo.project);
                EntitiesBrowser.setCurrentTabTitle(vm.project.name, $stateParams);
                initPermissions();
                initDirtyListener();
                EntitiesBrowser.setSaveCurrentEntity(save);
                if (!vm.project.id) {
                    FileUploaderCash.setFiles([]);
                }
            });

            vm.print = print;
            vm.save = save;
            vm.refresh = refresh;
            vm.updateAttachments = updateAttachments;
            vm.onChanged = onChanged;
            vm.onRestore = onRestore;
        }

        function initEntity() {
            var restoredEntity = EntitiesCache.get($stateParams);

            if (!restoredEntity) {
                pageInfo.project.author = pageInfo.project.author || identity;
                pageInfo.project.accessList = pageInfo.project.accessList || PermissionManagement.getAuthorAccessList(identity);
                vm.project = pageInfo.project;
            } else if (restoredEntity.version === pageInfo.project.version) {
                vm.project = restoredEntity;
            } else {
                return confirmationModal
                    .openEntityVersionsConflictConfirm(entityTitle)
                    .then(
                        function() {
                            vm.onRestore(pageInfo.project, pageInfo.project.version);
                        },
                        function() {
                            vm.onRestore(restoredEntity, pageInfo.project.version);
                            return $q.resolve();
                        });
            }

            return $q.resolve();
        }

        function onRestore(storeData) {
            var version = vm.project.version;
            vm.project = storeData;

            if (angular.isDefined(version)) {
                vm.project.version = version;
            }
            EntitiesCache.put($stateParams, vm.project);
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
            if (!isChanged) {
                return $q.resolve();
            }

            if (vm.project.id) {
                vm.loading = Project.update($stateParams, vm.project).$promise
                    .then(function(result) {
                        vm.project = result;
                        originalProject = angular.copy(vm.project);
                        EntitiesBrowser.setCurrentTabTitle(vm.project.name, $stateParams);
                        onUpdateSuccess({
                            id: vm.project.id
                        });
                    }, onSaveError);
            } else {
                vm.loading = Project.save(vm.project, onSaveSuccess, onSaveError).$promise;
            }

            return vm.loading;
        }

        function refresh() {
            if (vm.stateData.isNew) {
                vm.project = angular.copy(originalProject);

                return $q.resolve();
            }

            vm.loading = Project.get($stateParams).$promise
                .then(function(result) {
                    angular.extend(vm.project, result);
                    originalProject = angular.copy(vm.project);
                    autorecoveryCache.hide($stateParams);
                }, function() {
                    notifyService.error('Project not refreshed due to server error!');
                });

            return vm.loading;
        }

        function print() {
            save().then(function() {
                $state.go('entities.project-detail.print');
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

        function toggleDirty(isDirty) {
            if (!$scope.createProjectForm) {
                return;
            }

            isChanged = _.isBoolean(isDirty) ? isDirty : !$scope.createProjectForm.$dirty;

            if (isChanged) {
                $scope.createProjectForm.$setDirty();
                EntitiesCache.put($stateParams, pageInfo.project);
            } else {
                $scope.createProjectForm.$setPristine();
            }
            vm.isBtnSaveActive = $scope.createProjectForm.$dirty;
            EntitiesBrowser.changeDirtyTab($stateParams, isChanged);
        }

        function initDirtyListener() {
            $scope.$on('entity-updated', function(event, data) {
                vm.loading.then(function() {
                    entityHelper.checkVersion($stateParams, data, vm.project, entityTitle, isChanged, refresh);
                });
            });

            $scope.$on('ON_ENTITY_SAVE', function(event, data) {
                if (_.isEqual(data.tab.params, $stateParams)) {
                    save().then(data.defer.resolve);
                }
            });

            $scope.$watch('vm.project', function(newEntity) {
                EntitiesBrowser.setCurrentEntity(vm.project);
                var isDirty = autorecoveryHelper.isEntityDirty(originalProject, newEntity);
                toggleDirty(vm.stateData.isNew || isDirty);
                updateRecovery(newEntity, isDirty);
            }, true);


            $scope.$watch('createProjectForm', function(newValue) {
                EntitiesBrowser.setCurrentForm(newValue);
            });

            $scope.$on('access-list-changed', function() {
                vm.project.accessList = PermissionManagement.getAccessList();
            });
        }

        function onUpdateSuccess(result) {
            $state.go('entities.project-detail', {
                projectId: result.id
            });
        }

        function onSaveSuccess(result) {
            EntitiesBrowser.close(TabKeyUtils.getTabKeyFromParams($stateParams));
            $timeout(function() {
                $state.go('entities.project-detail', {
                    projectId: result.id
                });
            });
            EntitiesCache.removeByParams($stateParams);
        }

        function onSaveError(result) {
            var mess = (result.status === 400) ? 'this Project name is already in use in the system'
                : 'Project is not saved due to server error';
            notifyService.error(mess);
        }
    }
})();
