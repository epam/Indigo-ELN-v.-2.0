(function() {
    angular
        .module('indigoeln')
        .directive('experimentDetail', function() {
            return {
                scope: true,
                templateUrl: 'scripts/app/entities/experiment/experiment-detail/experiment-detail.html',
                controller: ExperimentDetailController,
                controllerAs: 'vm'
            };
        });

    /* @ngInject */
    function ExperimentDetailController($scope, $state, $stateParams, Experiment, ExperimentUtil,
                                        PermissionManagement, FileUploaderCash, EntitiesBrowser, autorecoveryHelper,
                                        notifyService, EntitiesCache, $q, Principal, Notebook, Components,
                                        autorecoveryCache, confirmationModal, entityHelper, $location) {
        var vm = this;
        var tabName;
        var params;
        var isContentEditor;
        var hasEditAuthority;
        var hasEditPermission;
        var originalExperiment;
        var updateRecovery;
        var entityTitle;

        init();

        function init() {
            updateRecovery = autorecoveryHelper.getUpdateRecoveryDebounce($stateParams);
            vm.stateData = $state.current.data;
            vm.isCollapsed = true;
            vm.deferLoading = $q.defer();
            vm.isInit = false;
            vm.loading = $q.all([
                vm.deferLoading.promise,
                getPageInfo().then(function(response) {
                    tabName = getExperimentName(response.notebook, response.experiment);
                    params = {
                        projectId: response.projectId,
                        notebookId: response.notebookId,
                        experimentId: response.experimentId
                    };
                    isContentEditor = response.isContentEditor;
                    hasEditAuthority = response.hasEditAuthority;
                    vm.notebook = response.notebook;
                    entityTitle = response.notebook.name + ' ' + response.experiment.name;

                    return initExperiment(response.experiment).then(function(experiment) {
                        updateOriginal(response.experiment);
                        updateCurrentTabTitle();
                        initPermissions();
                        FileUploaderCash.setFiles([]);

                        if (experiment.version > 1 || !experiment.lastVersion) {
                            tabName += ' v' + experiment.version;
                        }
                    });
                })
            ]);

            vm.save = save;
            vm.completeExperiment = completeExperiment;
            vm.completeExperimentAndSign = completeExperimentAndSign;
            vm.reopenExperiment = reopenExperiment;
            vm.repeatExperiment = repeatExperiment;
            vm.versionExperiment = versionExperiment;
            vm.printExperiment = printExperiment;
            vm.refresh = refresh;
            vm.onChangedComponent = onChangedComponent;
            vm.onRestore = onRestore;

            EntitiesBrowser.setCurrentTabTitle(tabName, $stateParams);
            EntitiesBrowser.setSaveCurrentEntity(save);
            EntitiesBrowser.setEntityActions({
                save: save,
                duplicate: repeatExperiment,
                print: printExperiment
            });

            initEventListeners();
        }

        /**
         * Check the cached entity with responsed entity and show confirm modal if enities have conflicts
         * @param { Object } experiment entity
         * @param { Boolean } withoutCheckVersion is flag which need only understand by update registration status
         * of batches by WS.
         * @return {Promise} resolve return the entity
         */
        function initExperiment(experiment, withoutCheckVersion) {
            var restoredExperiment = EntitiesCache.get($stateParams);

            if (!restoredExperiment || withoutCheckVersion) {
                vm.experiment = experiment;
            } else if (restoredExperiment.version === experiment.version) {
                vm.experiment = restoredExperiment;
            } else {
                return confirmationModal
                    .openEntityVersionsConflictConfirm(entityTitle)
                    .then(
                        function() {
                            vm.onRestore(experiment, experiment.version);
                            updateStatuses();

                            return experiment;
                        },
                        function() {
                            vm.onRestore(restoredExperiment, experiment.version);
                            updateStatuses();

                            return restoredExperiment;
                        });
            }

            updateStatuses();

            return $q.resolve(vm.experiment);
        }

        function updateOriginal(newEntity) {
            originalExperiment = angular.copy(newEntity);
        }

        function onRestore(storeData, lastVersion) {
            var version = lastVersion || _.get(vm.experiment, 'version') || storeData.version;
            vm.experiment = storeData;
            vm.experiment.version = version;
            EntitiesCache.put($stateParams, vm.experiment);
        }

        function getExperimentName(notebook, experiment) {
            return notebook.name ? notebook.name + '-' + experiment.name : experiment.name;
        }

        function getExperimentPromise() {
            return Experiment.get($stateParams).$promise.catch(function() {
                vm.isNotHavePermissions = true;
            });
        }

        function getPageInfo() {
            var notebookParams = {
                projectId: $stateParams.projectId,
                notebookId: $stateParams.notebookId
            };

            return $q.all([
                getExperimentPromise(),
                Notebook.get(notebookParams).$promise,
                Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                Principal.hasAuthorityIdentitySafe('EXPERIMENT_CREATOR'),
                EntitiesBrowser.getTabByParams($stateParams)
            ]).then(function(results) {
                return {
                    experiment: results[0],
                    notebook: results[1],
                    isContentEditor: results[2],
                    hasEditAuthority: results[3],
                    dirty: results[4] ? results[4].dirty : false,
                    experimentId: $stateParams.experimentId,
                    notebookId: $stateParams.notebookId,
                    projectId: $stateParams.projectId
                };
            });
        }

        function setReadOnly() {
            vm.isEditAllowed = isContentEditor || (hasEditAuthority && hasEditPermission);
            vm.isComponentsEditAllowed = vm.isStatusOpen && vm.isEditAllowed;
        }

        function toggleDirty(isDirty) {
            vm.isEntityChanged = !!isDirty;
            if (vm.isEntityChanged) {
                EntitiesCache.put($stateParams, vm.experiment);
            }
            EntitiesBrowser.changeDirtyTab($stateParams, vm.isEntityChanged);
        }

        function getSaveService(experimentForSave) {
            return (vm.experiment.template !== null ?
                Experiment.update($stateParams, experimentForSave).$promise
                : Experiment.save(experimentForSave).$promise);
        }

        function save() {
            if (!vm.isEntityChanged) {
                return $q.resolve();
            }

            vm.loading = getSaveService(_.extend({}, vm.experiment))
                .then(function(result) {
                    _.merge(vm.experiment, result);
                    updateOriginal(vm.experiment);
                }, function() {
                    notifyService.error('Experiment is not saved due to server error!');
                });

            return vm.loading;
        }

        function updateExperiment(experiment) {
            vm.experiment = experiment;
            postInitExperiment(vm.experiment);
        }

        function completeExperiment() {
            vm.loading = save()
                .then(function() {
                    return ExperimentUtil
                        .completeExperiment(vm.experiment, params, vm.notebook.name)
                        .then(updateExperiment);
                });
        }

        function completeExperimentAndSign() {
            vm.loading = save()
                .then(function() {
                    return ExperimentUtil
                        .completeExperimentAndSign(vm.experiment, params, vm.notebook.name, entityTitle)
                        .then(getExperiment)
                        .then(updateExperiment);
                });
        }

        function reopenExperiment() {
            vm.loading = ExperimentUtil
                .reopenExperiment(vm.experiment, params)
                .then(getExperiment)
                .then(updateExperiment);
        }

        function repeatExperiment() {
            vm.loading = ExperimentUtil
                .repeatExperiment(vm.experiment, params)
                .then(updateExperiment);
        }

        function versionExperiment() {
            vm.loading = ExperimentUtil
                .versionExperiment(vm.experiment, params)
                .then(getExperiment)
                .then(updateExperiment)
                .then(updateCurrentTabTitle);
        }

        function updateCurrentTabTitle() {
            EntitiesBrowser.setCurrentTabTitle(vm.notebook.name + '-' + vm.experiment.fullName, $stateParams);
        }

        function printExperiment() {
            if (vm.isEntityChanged) {
                vm.save(vm.experiment).then(function() {
                    $state.go('entities.experiment-detail.print');
                });
            } else {
                $state.go('entities.experiment-detail.print');
            }
        }

        function getExperiment() {
            return Experiment
                .get($stateParams)
                .$promise;
        }

        function postInitExperiment(experiment) {
            updateOriginal(experiment);
            initPermissions();
            FileUploaderCash.setFiles([]);
            autorecoveryCache.hide($stateParams);
        }

        function refresh() {
            vm.loading = getExperiment().then(function(result) {
                notifyService.success('Experiment updated');
                vm.experiment = result;
                postInitExperiment(vm.experiment);
            }, function() {
                notifyService.error('Experiment not refreshed due to server error!');
            });

            return vm.loading;
        }

        function initPermissions() {
            PermissionManagement.setEntity('Experiment');
            PermissionManagement.setAuthor(vm.experiment.author);
            PermissionManagement.setAccessList(vm.experiment.accessList);
            PermissionManagement.hasPermission('UPDATE_ENTITY').then(function(_hasEditPermission) {
                hasEditPermission = _hasEditPermission;
                updateStatuses();
                setReadOnly();
            });
        }

        function updateStatuses() {
            var status = _.get(vm.experiment, 'status');
            vm.isStatusOpen = status === 'Open';
            vm.isStatusComplete = status === 'Completed';
            vm.isStatusSubmitFail = status === 'Submit_Fail';
            vm.isStatusSubmitted = status === 'Submitted';
            vm.isStatusArchieved = status === 'Archived';
            vm.isStatusSigned = status === 'Signed';
            vm.isStatusSigning = status === 'Signing';
        }

        function onChangedComponent(componentId) {
            if (componentId === Components.attachments) {
                vm.loading = Experiment.get($stateParams).$promise
                    .then(function(result) {
                        vm.experiment.version = result.version;
                    }, function() {
                        notifyService.error('Experiment not refreshed due to server error!');
                    });
            }
        }

        function initEventListeners() {
            $scope.$on('entity-updated', function(event, data) {
                vm.loading.then(function() {
                    entityHelper.checkVersion($stateParams, data, vm.experiment, entityTitle, vm.isEntityChanged, refresh);
                });
            });

            $scope.$on('ON_ENTITY_SAVE', function(event, data) {
                if (_.isEqual(data.tab.params, $stateParams)) {
                    save().then(data.defer.resolve);
                }
            });

            $scope.$watch('vm.experiment', function(newEntity) {
                if (vm.isStatusOpen && vm.isEditAllowed) {
                    var isDirty = autorecoveryHelper.isEntityDirty(originalExperiment, newEntity);
                    toggleDirty(isDirty);
                    updateRecovery(newEntity, isDirty);
                    if (newEntity) {
                        EntitiesBrowser.setCurrentEntity(newEntity);
                    }
                }
            }, true);

            $scope.$on('access-list-changed', function() {
                vm.experiment.accessList = PermissionManagement.getAccessList();
                originalExperiment.accessList = angular.copy(vm.experiment.accessList);
            });

            $scope.$on('experiment-status-changed', function(event, experiments) {
                var experimentStatus = experiments[vm.experiment.fullId];
                if (experimentStatus) {
                    refresh();
                }
            });

            $scope.$on('batch-registration-status-changed', function(event, statuses) {
                _.each(statuses, function(status, fullNbkBatch) {
                    if (!_.find(vm.experiment.components.productBatchSummary.batches, {fullNbkBatch: fullNbkBatch})) {
                        return;
                    }

                    notifyService.info('The Registration Status of batch #' + fullNbkBatch + ' is ' + status.status);
                    vm.loading = getExperiment().then(function(experiment) {
                        return initExperiment(experiment, true).then(updateOriginal);
                    });
                });
            });
        }
    }
})();
