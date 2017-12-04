var roles = require('../../permissions/permission-roles.json');
/* @ngInject */
function ProjectController($scope, $state, projectService, notifyService, permissionService, fileUploader,
                           pageInfo, entitiesBrowserService, $timeout, $stateParams, tabKeyService,
                           autorecoveryHelper, autorecoveryCache, entitiesCache,
                           confirmationModal, $q, entityHelper, apiUrl) {
    var vm = this;
    var identity = pageInfo.identity;
    var isContentEditor = pageInfo.isContentEditor;
    var hasEditAuthority = pageInfo.hasEditAuthority;
    var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;
    var updateRecovery;
    var originalProject;
    var entityTitle;

    init();

    function init() {
        updateRecovery = autorecoveryHelper.getUpdateRecoveryDebounce($stateParams);
        entityTitle = pageInfo.project.name;

        vm.apiUrl = apiUrl;
        vm.stateData = $state.current.data;

        vm.loading = initEntity().then(function() {
            originalProject = angular.copy(pageInfo.project);
            entitiesBrowserService.setCurrentTabTitle(vm.project.name, $stateParams);
            initPermissions();
            initDirtyListener();
            entitiesBrowserService.setEntityActions({
                save: save,
                print: vm.print
            });

            if (!vm.project.id) {
                fileUploader.setFiles([]);
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
        var restoredEntity = entitiesCache.get($stateParams);

        if (!restoredEntity) {
            pageInfo.project.author = pageInfo.project.author || identity;
            pageInfo.project.accessList = pageInfo.project.accessList
                || permissionService.getAuthorAccessList(identity);

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
        entitiesCache.put($stateParams, vm.project);
    }

    function onChanged() {
        entitiesBrowserService.changeDirtyTab($stateParams, true);
    }

    function updateAttachments() {
        vm.loading = projectService.get($stateParams).$promise
            .then(function(result) {
                vm.project.version = result.version;
            }, function() {
                notifyService.error('Project not refreshed due to server error!');
            });
    }

    function save() {
        if (!vm.isEntityChanged) {
            return $q.resolve();
        }

        if (vm.project.id) {
            vm.loading = projectService.update($stateParams, vm.project).$promise
                .then(function(result) {
                    vm.project = result;
                    originalProject = angular.copy(vm.project);
                    entitiesCache.removeByParams($stateParams);
                    entitiesBrowserService.setCurrentTabTitle(vm.project.name, $stateParams);
                    onUpdateSuccess({
                        id: vm.project.id
                    });
                }, onSaveError);
        } else {
            vm.loading = projectService.save(vm.project, onSaveSuccess, onSaveError).$promise;
        }

        return vm.loading;
    }

    function refresh() {
        if (vm.stateData.isNew) {
            vm.project = angular.copy(originalProject);

            return $q.resolve();
        }

        vm.loading = projectService.get($stateParams).$promise
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
        permissionService.setEntity('Project');
        permissionService.setEntityId(vm.project.id);
        permissionService.setAuthor(vm.project.author);
        permissionService.setAccessList(vm.project.accessList);

        vm.isEditAllowed = isContentEditor ||
            (hasEditAuthority && permissionService.hasPermission(roles.UPDATE_ENTITY));
        // isCreateChildAllowed
        vm.isCreateChildAllowed = isContentEditor ||
            (hasCreateChildAuthority && permissionService.hasPermission(roles.CREATE_SUB_ENTITY));
    }

    function toggleDirty(isDirty) {
        if (isDirty) {
            entitiesCache.put($stateParams, pageInfo.project);
        }
        vm.isEntityChanged = !!isDirty;
        entitiesBrowserService.changeDirtyTab($stateParams, isDirty);
    }

    function initDirtyListener() {
        $scope.$on('entity-updated', function(event, data) {
            vm.loading.then(function() {
                entityHelper.checkVersion(
                    $stateParams, data, vm.project, entityTitle, vm.isEntityChanged, refresh
                );
            });
        });

        $scope.$on('ON_ENTITY_SAVE', function(event, data) {
            if (_.isEqual(data.tab.params, $stateParams)) {
                save().then(data.defer.resolve);
            }
        });

        $scope.$watch('vm.project', function(newEntity) {
            var isDirty = autorecoveryHelper.isEntityDirty(originalProject, newEntity);
            toggleDirty(vm.stateData.isNew || isDirty);
            updateRecovery(newEntity, isDirty);
        }, true);

        $scope.$on('access-list-changed', function() {
            vm.project.accessList = permissionService.getAccessList();
        });
    }

    function onUpdateSuccess(result) {
        $state.go('entities.project-detail', {
            projectId: result.id
        });
    }

    function onSaveSuccess(result) {
        entitiesBrowserService.close(tabKeyService.getTabKeyFromParams($stateParams));
        $timeout(function() {
            $state.go('entities.project-detail', {
                projectId: result.id
            });
        });
        entitiesCache.removeByParams($stateParams);
    }

    function onSaveError(result) {
        var mess = (result.status === 400) ? 'this Project name is already in use in the system'
            : 'Project is not saved due to server error';
        notifyService.error(mess);
    }
}

module.exports = ProjectController;
