(function() {
    angular
        .module('indigoeln')
        .controller('NotebookDialogController', NotebookDialogController);

    /* @ngInject */
    function NotebookDialogController($scope, $rootScope, $state, Notebook, notifyService, PermissionManagement, modalHelper,
                                      ExperimentUtil, pageInfo, EntitiesBrowser, $timeout, $stateParams, TabKeyUtils,
                                      autorecoveryHelper, notebookSummaryExperiments, $q, EntitiesCache,
                                      autorecoveryCache, confirmationModal) {
        var vm = this;
        var identity = pageInfo.identity;
        var isContentEditor = pageInfo.isContentEditor;
        var hasEditAuthority = pageInfo.hasEditAuthority;
        var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;
        var originalNotebook;
        var updateRecovery = autorecoveryHelper.getUpdateRecoveryDebounce($stateParams);
        var isChanged;
        var entityTitle;

        init();

        function init() {
            vm.stateData = $state.current.data;
            entityTitle = pageInfo.notebook.name;

            initEntity().then(function() {
                originalNotebook = angular.copy(pageInfo.notebook);
                EntitiesBrowser.setCurrentTabTitle(pageInfo.notebook.name, $stateParams);
                initPermissions();
            });

            vm.projectId = pageInfo.projectId;
            vm.isCollapsed = true;
            vm.isBtnSaveActive = false;
            vm.isSummary = false;
            vm.hasError = false;
            vm.loading = $q.when();
            vm.isEditAllowed = true;
            vm.isCreateChildAllowed = true;

            vm.createExperiment = createExperiment;
            vm.showSummary = showSummary;
            vm.goToExp = goToExp;
            vm.repeatExperiment = repeatExperiment;
            vm.refresh = refresh;
            vm.save = save;
            vm.print = print;
            vm.onRestore = onRestore;

            EntitiesBrowser.setSaveCurrentEntity(save);


            bindEvents();
        }

        function initEntity() {
            var restoredEntity = EntitiesCache.get($stateParams);

            if (!restoredEntity) {
                pageInfo.notebook.author = pageInfo.notebook.author || identity;
                pageInfo.notebook.accessList = pageInfo.notebook.accessList || PermissionManagement.getAuthorAccessList(identity);
                EntitiesCache.put($stateParams, pageInfo.notebook);
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
            PermissionManagement.setEntity('Notebook');
            PermissionManagement.setEntityId(vm.notebook.id);
            PermissionManagement.setParentId(vm.projectId);
            PermissionManagement.setAuthor(vm.notebook.author);
            PermissionManagement.setAccessList(vm.notebook.accessList);

            // isEditAllowed
            PermissionManagement.hasPermission('UPDATE_ENTITY').then(function(hasEditPermission) {
                vm.isEditAllowed = isContentEditor || (hasEditAuthority && hasEditPermission);
            });
            // isCreateChildAllowed
            PermissionManagement.hasPermission('CREATE_SUB_ENTITY').then(function(hasCreateChildPermission) {
                vm.isCreateChildAllowed = isContentEditor || (hasCreateChildAuthority && hasCreateChildPermission);
            });
        }

        function bindEvents() {
            $scope.$on('entity-updated', function(event, data) {
                vm.loading.then(function() {
                    if (_.isEqual($stateParams, data.entity) && data.version > vm.notebook.version) {
                        if (isChanged) {
                            confirmationModal
                                .openEntityVersionsConflictConfirm(entityTitle)
                                .then(refresh, function() {
                                    vm.notebook.version = data.version;
                                });

                            return;
                        }
                        refresh().then(function() {
                            notifyService.info(entityTitle + ' has been changed by another user and reloaded');
                        });
                    }
                });
            });

            $scope.$on('ON_ENTITY_SAVE', function(event, data) {
                if (data.tab.params === $stateParams) {
                    save().then(data.defer.resolve);
                }
            });

            $scope.$watch(function() {
                return vm.notebook;
            }, function(newEntity) {
                var isDirty = vm.stateData.isNew || autorecoveryHelper.isEntityDirty(originalNotebook, newEntity);
                toggleDirty(isDirty);
                updateRecovery(newEntity, isDirty);
            }, true);

            $scope.$watch('createNotebookForm.$dirty', function(isDirty) {
                vm.isBtnSaveActive = isDirty;
            }, true);

            $scope.$watch('createNotebookForm', function(form) {
                EntitiesBrowser.setCurrentForm(form);
            });

            $scope.$on('access-list-changed', function() {
                vm.notebook.accessList = PermissionManagement.getAccessList();
            });
        }

        function onRestore(storeData) {
            var version = vm.notebook.version;
            vm.notebook = storeData;
            vm.notebook.version = version;
            EntitiesCache.put($stateParams, vm.notebook);
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
            vm.loading = notebookSummaryExperiments.query({
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
            ExperimentUtil.repeatExperiment(experiment, params);
        }

        function onSaveSuccess(result) {
            EntitiesBrowser.close(TabKeyUtils.getTabKeyFromParams($stateParams));
            $timeout(function() {
                $rootScope.$broadcast('notebook-created', {
                    id: result.id, projectId: vm.projectId
                });
                $state.go('entities.notebook-detail', {
                    projectId: vm.projectId, notebookId: result.id
                });
            });
        }

        function onSaveError(result) {
            if (result.status === 400 && result.data.params) {
                var firstParam = _.first(result.data.params);
                if (result.data.params.length > 1 || firstParam.indexOf('-') > -1) {
                    notifyService.error('This Notebook name cannot be changed because batches are created within its' +
                        ' experiments');
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

        function toggleDirty(isDirty) {
            if (!$scope.createNotebookForm) {
                return;
            }

            isChanged = _.isBoolean(isDirty) ? isDirty : !$scope.createNotebookForm.$dirty;

            if (isChanged) {
                $scope.createNotebookForm.$setDirty();
            } else {
                $scope.createNotebookForm.$setPristine();
            }
            vm.isBtnSaveActive = $scope.createNotebookForm.$dirty;
            EntitiesBrowser.changeDirtyTab($stateParams, isChanged);
        }

        function refresh() {
            vm.hasError = false;
            vm.loading = Notebook.get($stateParams).$promise.then(function(response) {
                vm.notebook = response;
                originalNotebook = angular.copy(vm.notebook);
                autorecoveryCache.hide($stateParams);
                EntitiesCache.put($stateParams, vm.notebook);
            });

            return vm.loading;
        }

        function partialRefresh() {
            vm.notebook.name = originalNotebook.name;
        }

        function print() {
            save().then(function() {
                $state.go('entities.notebook-detail.print');
            });
        }

        function save() {
            if (!isChanged) {
                return;
            }

            vm.hasError = false;
            if (vm.notebook.id) {
                vm.loading = Notebook.update($stateParams, vm.notebook).$promise
                    .then(function(result) {
                        EntitiesCache.removeByParams($stateParams);
                        autorecoveryCache.remove($stateParams);
                        vm.notebook.version = result.version;
                        originalNotebook = angular.copy(vm.notebook);
                        EntitiesCache.put($stateParams, vm.notebook);
                        EntitiesBrowser.setCurrentTabTitle(vm.notebook.name, $stateParams);
                        $rootScope.$broadcast('notebook-changed', {
                            projectId: vm.projectId,
                            notebook: vm.notebook
                        });
                    }, onSaveError);

                return vm.loading;
            }
            vm.loading = Notebook.save({
                projectId: vm.projectId
            }, vm.notebook, onSaveSuccess, onSaveError).$promise;

            return vm.loading;
        }
    }
})();
