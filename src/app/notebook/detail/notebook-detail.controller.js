/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

var roles = require('../../permissions/permission-roles.json');
/* @ngInject */
function NotebookDetailController($scope, $state, notebookService, notifyService, permissionService,
                                  modalHelper, experimentUtil, pageInfo, entitiesBrowserService,
                                  $timeout, $stateParams, tabKeyService, autorecoveryHelper,
                                  notebookSummaryExperimentsService, $q, entitiesCache,
                                  autorecoveryCache, confirmationModal, entityHelper, principalService) {
    var vm = this;
    var identity = pageInfo.identity;
    var isContentEditor = pageInfo.isContentEditor;
    var hasEditAuthority = pageInfo.hasEditAuthority;
    var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;
    vm.isNotHavePermissions = pageInfo.isNotHavePermissions;
    var originalNotebook;
    var updateRecovery = autorecoveryHelper.getUpdateRecoveryDebounce($stateParams);
    var entityTitle;

    init();

    function init() {
        if (vm.isNotHavePermissions) {
            return;
        }
        vm.stateData = $state.current.data;
        entityTitle = pageInfo.notebook.name;

        vm.loading = initEntity().then(function() {
            originalNotebook = angular.copy(pageInfo.notebook);
            entitiesBrowserService.setCurrentTabTitle(pageInfo.notebook.name, $stateParams);
            entitiesBrowserService.setEntityActions({
                save: save,
                print: vm.print
            });
            initPermissions();
        });

        vm.projectId = pageInfo.projectId;
        vm.isEntityChanged = false;
        vm.isSummary = false;
        vm.hasError = false;
        vm.isEditAllowed = true;
        vm.isCreateChildAllowed = true;
        vm.allowContent = principalService.hasAnyAuthority([roles.EXPERIMENT_READER, roles.CONTENT_EDITOR]);

        vm.createExperiment = createExperiment;
        vm.showSummary = showSummary;
        vm.goToExp = goToExp;
        vm.repeatExperiment = repeatExperiment;
        vm.refresh = refresh;
        vm.save = save;
        vm.print = print;
        vm.onRestore = onRestore;
        vm.notebookExistValidation = notebookExistValidation;

        bindEvents();
    }

    function notebookExistValidation(modelValue) {
        // Skip validation for saved notebook and empty value
        if (!modelValue || !$scope.createNotebookForm.notebookName.$dirty || originalNotebook.name === modelValue) {
            return $q.when(true);
        }

        return notebookService.isNew({name: modelValue})
            .$promise
            .then(function(result) {
                if (!result.isNew) {
                    // Role with provided name already exist
                    return $q.reject('Notebook name already in use');
                }

                // Nothing found, validation passes
                return true;
            });
    }

    function initEntity() {
        var restoredEntity = entitiesCache.get($stateParams);

        if (!restoredEntity) {
            pageInfo.notebook.author = pageInfo.notebook.author || identity;
            pageInfo.notebook.accessList = pageInfo.notebook.accessList
                || permissionService.getAuthorAccessList(identity);

            vm.notebook = pageInfo.notebook;
        } else if (restoredEntity.version === pageInfo.notebook.version) {
            vm.notebook = restoredEntity;
        } else {
            return confirmationModal
                .openEntityVersionsConflictConfirm(entityTitle)
                .then(
                    function() {
                        vm.onRestore(pageInfo.notebook, pageInfo.notebook.version);
                    },
                    function() {
                        vm.onRestore(restoredEntity, pageInfo.notebook.version);
                    });
        }

        return $q.resolve();
    }

    function initPermissions() {
        permissionService.setNotebook(vm.notebook, vm.projectId);

        // isEditAllowed
        vm.isEditAllowed = isContentEditor ||
            (hasEditAuthority && permissionService.hasPermission(roles.UPDATE_ENTITY));

        // isCreateChildAllowed
        vm.isCreateChildAllowed = isContentEditor ||
            (hasCreateChildAuthority && permissionService.hasPermission(roles.CREATE_SUB_ENTITY));
    }

    function restoreVersion() {
        vm.notebook.version = originalNotebook.version;
    }

    function bindEvents() {
        $scope.$on('entity-updated', function(event, data) {
            vm.loading.then(function() {
                var currentEntity = {
                    projectId: $stateParams.projectId,
                    notebookId: $stateParams.notebookId,
                    version: vm.notebook.version,
                    type: 'Notebook',
                    title: entityTitle
                };
                var updatedEntity = data.entity;
                updatedEntity.version = data.version;

                entityHelper.onEntityUpdate(
                    currentEntity, updatedEntity, vm.isEntityChanged, refresh, restoreVersion
                );
            });
        });

        $scope.$on('ON_ENTITY_SAVE', function(event, data) {
            if (_.isEqual(data.tab.params, $stateParams)) {
                save().then(data.defer.resolve);
            }
        });

        $scope.$watch(function() {
            return vm.notebook;
        }, function(newEntity) {
            var isDirty = autorecoveryHelper.isEntityDirty(originalNotebook, newEntity);
            toggleDirty(vm.stateData.isNew || isDirty);
            updateRecovery(newEntity, isDirty);
        }, true);


        $scope.$on('access-list-changed', function() {
            vm.notebook.accessList = permissionService.getAccessList();
        });

        $scope.$on('$destroy', function() {
            modalHelper.close();
        });
    }

    function onRestore(storeData, lastVersion) {
        var version = lastVersion || vm.notebook.version || storeData.version;
        vm.notebook = storeData;

        initPermissions();
        vm.notebook.version = version;

        entitiesCache.put($stateParams, vm.notebook);
    }

    function createExperiment() {
        var resolve = {
            fullNotebookId: function() {
                return vm.notebook.fullId;
            }
        };

        modalHelper.openCreateNewExperimentModal(resolve).then(function(result) {
            $state.go('entities.experiment-detail', {
                notebookId: result.notebookId,
                projectId: result.projectId,
                experimentId: result.id
            });
        });
    }

    function showSummary() {
        if (vm.isSummary) {
            vm.isSummary = false;

            return;
        }
        if (vm.experiments && vm.experiments.length) {
            vm.isSummary = true;

            return;
        }
        vm.loading = notebookSummaryExperimentsService.query({
            notebookId: $stateParams.notebookId,
            projectId: $stateParams.projectId
        }).$promise.then(function(data) {
            if (!data.length) {
                notifyService.info('There are no experiments in this notebook');

                return;
            }
            data.forEach(function(exp) {
                if (!exp.lastVersion || exp.experimentVersion > 1) {
                    exp.fullName = vm.notebook.name + '-' + exp.name + ' v' + exp.experimentVersion;
                } else {
                    exp.fullName = vm.notebook.name + '-' + exp.name;
                }
            });

            vm.experiments = data;
            vm.isSummary = true;
        }, function() {
            notifyService.error('Cannot get summary right now due to server error');
        });
    }

    function goToExp(exp) {
        var ids = exp.fullId.split('-');
        $state.go('entities.experiment-detail', {
            experimentId: ids[2], notebookId: ids[1], projectId: ids[0]
        });
    }

    function repeatExperiment(experiment, params) {
        experimentUtil.repeatExperiment(experiment, params);
    }

    function toggleDirty(isDirty) {
        vm.isEntityChanged = !!isDirty;
        if (vm.isEntityChanged) {
            entitiesCache.put($stateParams, vm.notebook);
        }
        entitiesBrowserService.changeDirtyTab($stateParams, vm.isEntityChanged);
    }

    function refresh() {
        vm.hasError = false;
        if (vm.stateData.isNew) {
            vm.project = angular.copy(originalNotebook);
            initPermissions();

            return $q.resolve();
        }

        vm.loading = notebookService.get($stateParams).$promise.then(function(response) {
            vm.notebook = response;
            initPermissions();
            originalNotebook = angular.copy(vm.notebook);
            autorecoveryCache.hide($stateParams);
        });

        return vm.loading;
    }

    function partialRefresh() {
        vm.notebook.name = originalNotebook.name;
    }

    function print() {
        save().then(function() {
            $state.go('entities.notebook-detail.print', null, {notify: false});
        });
    }

    function save() {
        if (!vm.isEntityChanged) {
            return $q.resolve();
        }

        vm.hasError = false;
        if (vm.notebook.id) {
            vm.loading = notebookService.update($stateParams, vm.notebook).$promise
                .then(function(result) {
                    entitiesCache.removeByParams($stateParams);
                    autorecoveryCache.remove($stateParams);
                    vm.notebook.version = result.version;
                    originalNotebook = angular.copy(vm.notebook);
                    entitiesBrowserService.setCurrentTabTitle(vm.notebook.name, $stateParams);
                }, onSaveError);

            return vm.loading;
        }

        vm.loading = notebookService.save({
            projectId: vm.projectId
        }, vm.notebook, onSaveSuccess, onSaveError).$promise;

        return vm.loading;
    }

    function onSaveSuccess(result) {
        entitiesBrowserService.close(tabKeyService.getTabKeyFromParams($stateParams));
        entitiesCache.removeByParams($stateParams);
        autorecoveryCache.remove($stateParams);

        $timeout(function() {
            $state.go('entities.notebook-detail', {
                projectId: vm.projectId, notebookId: result.id
            });
        });
    }

    function onSaveError(result) {
        if (result.status === 400) {
            if (result.data && result.data.message) {
                notifyService.error(result.data.message);
            } else {
                notifyService.error('This Notebook name is already in use in the system');
            }
            vm.hasError = false;
            partialRefresh();

            return;
        }

        $timeout(function() {
            vm.hasError = true;
        });
        notifyService.error('Notebook is not saved due to server error');
    }
}

module.exports = NotebookDetailController;
