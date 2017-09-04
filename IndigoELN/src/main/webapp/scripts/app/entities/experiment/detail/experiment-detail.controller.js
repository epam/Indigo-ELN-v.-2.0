(function() {
    angular
        .module('indigoeln')
        .controller('ExperimentDetailController', ExperimentDetailController);

    /* @ngInject */
    function ExperimentDetailController($scope, $state, $stateParams, Experiment, ExperimentUtil,
                                        PermissionManagement, FileUploaderCash, EntitiesBrowser, autorecoveryHelper,
                                        notifyService, EntitiesCache, $q, Principal, Notebook, Components,
                                        componentsUtils, autorecoveryCache, confirmationModal) {
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
            vm.loading = getPageInfo().then(function(response) {
                initComponents(response.experiment);
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

                initExperiment(response.experiment).then(function(experiment) {
                    updateOriginal(response.experiment);
                    EntitiesBrowser.setCurrentTabTitle(vm.notebook.name + '-' + experiment.name, $stateParams);
                    initPermissions();
                    FileUploaderCash.setFiles([]);

                    if (experiment.version > 1 || !experiment.lastVersion) {
                        tabName += ' v' + experiment.version;
                    }
                });
            });

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
         * @param experiment
         * @param { Boolean } withoutCheckVersion is flag which need only understand by update registration status
         * of batches by WS.
         * @return {Promise} resolve return the entity
         */
        function initExperiment(experiment, withoutCheckVersion) {
            var restoredExperiment = EntitiesCache.get($stateParams);

            if (!restoredExperiment || withoutCheckVersion) {
                EntitiesCache.put($stateParams, experiment);
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

        function initComponents(experiment) {
            componentsUtils.initComponents(
                experiment.components,
                experiment.template.templateContent
            );
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

        function setStatus(status) {
            vm.experiment.status = status;
            updateStatuses();
        }

        function setReadOnly() {
            vm.isEditAllowed = isContentEditor || (hasEditAuthority && hasEditPermission && vm.isStatusOpen);
        }

        function toggleDirty(isDirty) {
            vm.isEntityChanged = !!isDirty;
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
                    EntitiesCache.put($stateParams, result);
                    vm.experiment = result;
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
            vm.loading = ExperimentUtil
                .completeExperiment(vm.experiment, params, vm.notebook.name)
                .then(updateExperiment);
        }

        function completeExperimentAndSign() {
            vm.loading = ExperimentUtil
                .completeExperimentAndSign(vm.experiment, params, vm.notebook.name, entityTitle)
                .then(getExperiment)
                .then(updateExperiment);
        }

        function reopenExperiment() {
            vm.loading = ExperimentUtil
                .reopenExperiment(vm.experiment, params)
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
                .then(updateExperiment);
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
            updateStatuses();
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
                    if (_.isEqual($stateParams, data.entity) && data.version > vm.experiment.version) {
                        if (vm.isEntityChanged) {
                            confirmationModal
                                .openEntityVersionsConflictConfirm(entityTitle)
                                .then(refresh, function() {
                                    vm.experiment.version = data.version;
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
                if (_.isEqual(data.tab.params, $stateParams)) {
                    save().then(data.defer.resolve);
                }
            });

            $scope.$watch('vm.experiment', function(newEntity) {
                var isDirty = autorecoveryHelper.isEntityDirty(originalExperiment, newEntity);
                toggleDirty(isDirty);
                updateRecovery(newEntity, isDirty);
                if (newEntity) {
                    EntitiesBrowser.setCurrentEntity(newEntity);
                }
            }, true);

            $scope.$on('access-list-changed', function() {
                vm.experiment.accessList = PermissionManagement.getAccessList();
            });

            $scope.$on('experiment-status-changed', function(event, experiments) {
                var experimentStatus = experiments[vm.experiment.fullId];
                if (experimentStatus) {
                    setStatus(experimentStatus);
                    setReadOnly();
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
