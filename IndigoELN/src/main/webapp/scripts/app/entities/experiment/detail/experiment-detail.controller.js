(function() {
    angular
        .module('indigoeln')
        .controller('ExperimentDetailController', ExperimentDetailController);

    /* @ngInject */
    function ExperimentDetailController($scope, $state, $timeout, $stateParams, Experiment, ExperimentUtil,
                                        PermissionManagement, FileUploaderCash, AutoRecoverEngine, EntitiesBrowser,
                                        Alert, EntitiesCache, $q, AutoSaveEntitiesEngine, Principal, Notebook,
                                        Components) {
        var vm = this;
        var pageInfo;
        var tabName;
        var params;
        var isContentEditor;
        var hasEditAuthority;
        var hasEditPermission;

        init();

        function init() {
            vm.isCollapsed = true;
            vm.loading = getPageInfo().then(function(response) {
                initComponents(response.experiment.components);
                pageInfo = response;
                tabName = getExperimentName(response.notebook, response.experiment);
                params = {
                    projectId: response.projectId,
                    notebookId: response.notebookId,
                    experimentId: response.experimentId
                };
                isContentEditor = response.isContentEditor;
                hasEditAuthority = response.hasEditAuthority;
                vm.experiment = response.experiment;
                vm.notebook = response.notebook;

                EntitiesBrowser.setCurrentTabTitle(vm.notebook.name + '-' + vm.experiment.name, $stateParams);

                initPermissions();
                FileUploaderCash.setFiles([]);

                if (response.experiment.experimentVersion > 1 || !response.experiment.lastVersion) {
                    tabName += ' v' + response.experiment.experimentVersion;
                }

                initDirtyListener();
                initEventListeners();
            });

            // TODO: the Action drop up button should be disable in case of there is unsaved data.
            vm.statuses = ['Open', 'Completed', 'Submit_Fail', 'Submitted', 'Archived', 'Signing', 'Signed'];

            vm.isBtnSaveActive = EntitiesBrowser.getActiveTab().dirty;

            vm.save = save;
            vm.completeExperiment = completeExperiment;
            vm.completeExperimentAndSign = completeExperimentAndSign;
            vm.reopenExperiment = reopenExperiment;
            vm.repeatExperiment = repeatExperiment;
            vm.versionExperiment = versionExperiment;
            vm.printExperiment = printExperiment;
            vm.refresh = refresh;
            vm.saveCurrent = saveCurrent;
            vm.onChangedComponent = onChangedComponent;

            EntitiesBrowser.setCurrentTabTitle(tabName, $stateParams);
            EntitiesBrowser.setUpdateCurrentEntity(refresh);
            EntitiesBrowser.setSaveCurrentEntity(saveCurrent);
            EntitiesBrowser.setEntityActions({
                save: saveCurrent,
                duplicate: repeatExperiment,
                print: printExperiment
            });
        }

        function initComponents(components) {
            initPreferredCompoundSummary(components);
            initProductBatchSummary(components);
            initStoichTable(components);
            initReactionDetails(components);
        }

        function initPreferredCompoundSummary(components) {
            if (components.preferredCompoundSummary) {
                components.preferredCompoundSummary.compounds = components.preferredCompoundSummary.compounds || [];
            }
        }

        function initProductBatchSummary(components) {
            if (components.productBatchSummary) {
                components.productBatchSummary.batches = components.productBatchSummary.batches || [];
            }
        }

        function initStoichTable(components) {
            if (components.stoichTable) {
                components.stoichTable.products = components.stoichTable.products || [];
                components.stoichTable.reactants = components.stoichTable.reactants || [];
            }
        }

        function initReactionDetails(components) {
            if (components.reactionDetails) {
                components.reactionDetails.batchOwner = components.reactionDetails.batchOwner || [];
                components.reactionDetails.experimentCreator = components.reactionDetails.experimentCreator || {};
            }
        }

        function getExperimentName(notebook, experiment) {
            return notebook.name ? notebook.name + '-' + experiment.name : experiment.name;
        }

        function getPageInfo() {
            var entityParams = {
                projectId: $stateParams.projectId,
                notebookId: $stateParams.notebookId,
                experimentId: $stateParams.experimentId
            };
            var notebookParams = {
                projectId: $stateParams.projectId,
                notebookId: $stateParams.notebookId
            };

            if (!EntitiesCache.get(entityParams)) {
                EntitiesCache.put(entityParams, AutoSaveEntitiesEngine.autoRecover(Experiment, entityParams));
            }

            if (!EntitiesCache.get(notebookParams)) {
                EntitiesCache.put(notebookParams, Notebook.get(notebookParams).$promise);
            }

            return $q.all([
                EntitiesCache.get(entityParams),
                EntitiesCache.get(notebookParams),
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
        }

        function setReadOnly() {
            vm.isEditAllowed = ((isContentEditor || hasEditAuthority) && hasEditPermission) && vm.isStatusOpen;
            $scope.experimentForm.$$isReadOnly = !vm.isEditAllowed;
        }

        function save(experiment) {
            initComponents(experiment.components);
            var experimentForSave = _.extend({}, experiment);
            vm.loading = (experiment.template !== null) ? Experiment.update($stateParams, vm.experiment).$promise
                : Experiment.save(experimentForSave).$promise;
            vm.loading.then(function(result) {
                vm.experiment.version = result.version;
                $scope.experimentForm.$setPristine();
            }, function() {
                Alert.error('Experiment is not saved due to server error!');
            });

            return vm.loading;
        }

        function completeExperiment() {
            vm.loading = ExperimentUtil.completeExperiment(vm.experiment, params, vm.notebook.name);
        }

        function completeExperimentAndSign() {
            ExperimentUtil.completeExperimentAndSign(vm.experiment, params, vm.notebook.name);
        }

        function reopenExperiment() {
            vm.loading = ExperimentUtil.reopenExperiment(vm.experiment, params);
        }

        function repeatExperiment() {
            vm.loading = ExperimentUtil.repeatExperiment(vm.experiment, params);
        }

        function versionExperiment() {
            var original = vm.experiment;

            vm.loading = ExperimentUtil.versionExperiment(vm.experiment, params);
            vm.loading.then(function() {
                Experiment.get($stateParams).$promise.then(function(result) {
                    angular.extend(original, result);
                });
            });
        }

        function printExperiment() {
            if ($scope.experimentForm.$dirty) {
                vm.save(vm.experiment).then(function() {
                    ExperimentUtil.printExperiment(params);
                });
            } else {
                ExperimentUtil.printExperiment(params);
            }
        }

        function refresh() {
            vm.loading = Experiment.get($stateParams).$promise;
            vm.loading.then(function(result) {
                Alert.success('Experiment updated');
                angular.extend(vm.experiment, result);
                $timeout($scope.experimentForm.$setPristine);
            }, function() {
                Alert.error('Experiment not refreshed due to server error!');
            });

            return vm.loading;
        }

        function saveCurrent() {
            return vm.save(vm.experiment);
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

        function updateFormState() {
            if (pageInfo.dirty) {
                $scope.experimentForm.$setDirty();
            } else {
                $scope.experimentForm.$setPristine();
            }
        }

        function initDirtyListener() {
            $timeout(function() {
                var tabKind = $state.$current.data.tab.kind;
                EntitiesBrowser.setCurrentForm($scope.experimentForm);
                AutoRecoverEngine.track({
                    vm: vm,
                    kind: tabKind,
                    onSetDirty: function() {
                        $scope.experimentForm.$setDirty();
                    }
                });
                updateFormState();
                $scope.$watch('vm.experiment', function(entity, old) {
                    EntitiesBrowser.setCurrentEntity(vm.experiment);
                    AutoRecoverEngine.tracker.change(entity, old);
                }, true);

                $scope.$watch('experimentForm.$dirty', function(cur, old) {
                    var dirty = $scope.experimentForm.$dirty;
                    vm.isBtnSaveActive = dirty;
                    AutoRecoverEngine.tracker.changeDirty(cur, old);
                }, true);
            }, 0, false);
        }

        function updateStatuses() {
            var status = vm.experiment.status;
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
                        Alert.error('Experiment not refreshed due to server error!');
                    });
            }

            $scope.experimentForm.$setDirty();
            EntitiesBrowser.changeDirtyTab($stateParams, true);
        }

        function initEventListeners() {
            $scope.$watch('vm.isEditAllowed', function() {
                if (!vm.isEditAllowed) {
                    $scope.experimentForm.$setPristine();
                }
            }, true);

            $scope.$watch('vm.experiment.status', function() {
                updateStatuses();
                setReadOnly();
            });

            $scope.$on('access-list-changed', function() {
                vm.experiment.accessList = PermissionManagement.getAccessList();
                $scope.experimentForm.$setDirty();
            });

            $scope.$on('experiment-status-changed', function(event, data) {
                _.each(data, function(status, id) {
                    if (id === vm.experiment.fullId) {
                        setStatus(status);
                        setReadOnly();
                    }
                });
            });

            // Activate save button when change permission
            $scope.$on('activate button', function() {
                $timeout(function() {
                    vm.isBtnSaveActive = true;
                    // If put 0, then save button isn't activated
                }, 10);
            });
        }
    }
})();
