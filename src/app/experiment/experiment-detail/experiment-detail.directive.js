var template = require('./experiment-detail.html');
var roles = require('../../permissions/permission-roles.json');

function experimentDetail() {
    return {
        scope: true,
        template: template,
        controller: ExperimentDetailController,
        controllerAs: 'vm'
    };
}

/* @ngInject */
function ExperimentDetailController($scope, $state, $stateParams, experimentService, experimentUtil,
                                    permissionService, fileUploader, entitiesBrowserService,
                                    autorecoveryHelper, notifyService, entitiesCache, $q,
                                    principalService, notebookService, typeOfComponents, autorecoveryCache,
                                    confirmationModal, entityHelper, apiUrl, componentsUtil, entityTreeService) {
    var vm = this;
    var params;
    var isContentEditor;
    var hasEditAuthority;
    var hasEditPermission;
    var originalExperiment;
    var updateRecovery;
    var entityTitle;
    var isSaving = false;

    init();

    function init() {
        updateRecovery = autorecoveryHelper.getUpdateRecoveryDebounce($stateParams);
        vm.apiUrl = apiUrl;
        vm.stateData = $state.current.data;
        vm.deferLoading = $q.defer();
        vm.isInit = false;
        vm.loading = $q.all([
            vm.deferLoading.promise,
            getPageInfo().then(function(response) {
                // Init components because we have old experiments with wrong template
                initComponents(response.experiment);
                params = {
                    projectId: response.projectId,
                    notebookId: response.notebookId,
                    experimentId: response.experimentId
                };
                isContentEditor = response.isContentEditor;
                hasEditAuthority = response.hasEditAuthority;
                vm.notebook = response.notebook;
                entityTitle = response.notebook.name + ' ' + response.experiment.name;

                return initExperiment(response.experiment).then(function() {
                    updateOriginal(response.experiment);
                    updateCurrentTabTitle();
                    initPermissions();
                    fileUploader.setFiles([]);
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
        var restoredExperiment = entitiesCache.get($stateParams);

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

    function initComponents(experiment) {
        componentsUtil.initComponents(
            experiment.components,
            experiment.template.templateContent
        );
    }

    function onRestore(storeData, lastVersion) {
        var version = lastVersion || vm.experiment.version || storeData.version;
        vm.experiment = storeData;

        initPermissions();
        vm.experiment.version = version;

        entitiesCache.put($stateParams, vm.experiment);
    }

    function getExperimentPromise() {
        return experimentService.get($stateParams).$promise.catch(function() {
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
            notebookService.get(notebookParams).$promise,
            principalService.hasAuthorityIdentitySafe(roles.CONTENT_EDITOR),
            principalService.hasAuthorityIdentitySafe(roles.EXPERIMENT_CREATOR),
            entitiesBrowserService.getTabByParams($stateParams)
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
            entitiesCache.put($stateParams, vm.experiment);
        }
        entitiesBrowserService.changeDirtyTab($stateParams, vm.isEntityChanged);
    }

    function getSaveService(experimentForSave) {
        if (vm.experiment.template !== null) {
            return experimentService.update($stateParams, experimentForSave, entityTreeService.updateExperiment).$promise;
        }

        return experimentService.save(experimentForSave, entityTreeService.addExperiment).$promise;
    }

    function save() {
        if (!vm.isEntityChanged) {
            return $q.resolve();
        }

        isSaving = true;
        vm.loading = getSaveService(_.extend({}, vm.experiment))
            .then(function(result) {
                _.merge(vm.experiment, result);
                entitiesCache.removeByParams($stateParams);
                updateOriginal(vm.experiment);
            }, function() {
                notifyService.error('Experiment is not saved due to server error!');
            })
            .finally(function() {
                isSaving = false;
            });

        return vm.loading;
    }

    function updateExperiment(experiment) {
        vm.experiment = experiment;
        postInitExperiment(vm.experiment);

        return vm.experiment;
    }

    function completeExperiment() {
        vm.loading = save()
            .then(function() {
                return experimentUtil
                    .completeExperiment(vm.experiment, params, vm.notebook.name)
                    .then(updateExperiment);
            });
    }

    function completeExperimentAndSign() {
        vm.loading = save()
            .then(function() {
                return experimentUtil
                    .completeExperimentAndSign(vm.experiment, params, vm.notebook.name, entityTitle)
                    .then(getExperiment)
                    .then(updateExperiment);
            });
    }

    function reopenExperiment() {
        vm.loading = experimentUtil
            .reopenExperiment(vm.experiment, params)
            .then(getExperiment)
            .then(updateExperiment);
    }

    function repeatExperiment() {
        vm.loading = experimentUtil
            .repeatExperiment(vm.experiment, params)
            .then(updateExperiment);
    }

    /**
     * Do version current experiment, update title and go to new version experiment
     * @return { Promise } - promise of state transition
     */
    function versionExperiment() {
        vm.loading = experimentUtil
            .versionExperiment(vm.experiment, params)
            .then(function(experiment) {
                return getExperiment()
                    .then(updateExperiment)
                    .then(updateCurrentTabTitle)
                    .then(function() {
                        $state.go($state.current, _.extend({}, params, {
                            experimentId: experiment.id
                        }));
                    });
            });
    }

    function updateCurrentTabTitle() {
        entitiesBrowserService.setCurrentTabTitle(vm.notebook.name + '-' + vm.experiment.fullName, $stateParams);
        entitiesBrowserService.setEntityActions({
            save: save,
            duplicate: repeatExperiment,
            print: printExperiment
        });
    }

    function printExperiment() {
        if (vm.isEntityChanged) {
            vm.save(vm.experiment).then(function() {
                $state.go('entities.experiment-detail.print', null, {notify: false});
            });
        } else {
            $state.go('entities.experiment-detail.print', null, {notify: false});
        }
    }

    function getExperiment() {
        if (!$stateParams.experimentId) {
            return $q.reject('It is impossible to load experiment due to experiment id is undefined');
        }
        return experimentService
            .get($stateParams)
            .$promise;
    }

    function postInitExperiment(experiment) {
        updateOriginal(experiment);
        initPermissions();
        fileUploader.setFiles([]);
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
        permissionService.setExperiment(vm.experiment);

        hasEditPermission = permissionService.hasPermission(roles.UPDATE_ENTITY);
        updateStatuses();
        setReadOnly();
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

    function onChangedComponent(component) {
        if (component && component.componentId === typeOfComponents.attachments.id) {
            vm.loading = experimentService.get($stateParams).$promise
                .then(function(result) {
                    vm.experiment.version = result.version;
                }, function() {
                    notifyService.error('Experiment not refreshed due to server error!');
                });
        }
    }

    function restoreVersion() {
        vm.experiment.version = originalExperiment.version;
    }

    function getRegistrationStatusMessage(statuses) {
        var resultStatuses = {};
        _.forEach(statuses, function(status, fullNbkBatch) {
            if (_.find(vm.experiment.components.productBatchSummary.batches, {fullNbkBatch: fullNbkBatch})) {
                if (resultStatuses[status.status]) {
                    resultStatuses[status.status]++;
                } else {
                    resultStatuses[status.status] = 1;
                }
            }
        });

        if (_.size(resultStatuses)) {
            var message = 'The Registration Status of batches is updated: ';
            var isFirst = true;
            _.forEach(resultStatuses, function(count, rs) {
                if (!isFirst) {
                    message += ', ';
                }
                isFirst = false;
                message += count > 1 ? (count + ' batches are ' + rs) : ('1 batch is ' + rs);
            });
            message += '.';

            return message;
        }

        return null;
    }

    function initEventListeners() {
        var entityUpdate = $scope.$on('entity-updated', function(event, data) {
            if (isSaving) {
                return;
            }

            vm.loading.then(function() {
                var currentEntity = {
                    projectId: params.projectId,
                    notebookId: params.notebookId,
                    experimentId: params.experimentId,
                    version: vm.experiment.version,
                    type: 'Experiment',
                    title: entityTitle
                };
                var updatedEntity = data.entity;
                updatedEntity.version = data.version;

                entityHelper.onEntityUpdate(
                    currentEntity, updatedEntity, vm.isEntityChanged, refresh, restoreVersion
                );
            });
        });

        var entitySave = $scope.$on('ON_ENTITY_SAVE', function(event, data) {
            if (_.isEqual(data.tab.params, $stateParams)) {
                save().then(data.defer.resolve);
            }
        });

        var experimentWatch = $scope.$watch('vm.experiment', function(newEntity) {
            if (vm.isStatusOpen && vm.isEditAllowed) {
                var isDirty = autorecoveryHelper.isEntityDirty(originalExperiment, newEntity);
                toggleDirty(isDirty);
                updateRecovery(newEntity, isDirty);
            }
        }, true);

        var accessList = $scope.$on('access-list-changed', function() {
            vm.experiment.accessList = permissionService.getAccessList();
        });

        var experimentStatus = $scope.$on('experiment-status-changed', function(event, experiments) {
            if (experiments[vm.experiment.fullId]) {
                refresh();
            }
        });

        var batchRegistrationStatus = $scope.$on('batch-registration-status-changed', function(event, statuses) {
            var message = getRegistrationStatusMessage(statuses);
            if (message) {
                notifyService.info(message);
                vm.loading = getExperiment().then(function(experiment) {
                    return initExperiment(experiment, true).then(updateOriginal);
                });
            }
        });

        $scope.$on('$destroy', function() {
            entityUpdate();
            entitySave();
            experimentWatch();
            accessList();
            experimentStatus();
            batchRegistrationStatus();
        });
    }
}

module.exports = experimentDetail;
