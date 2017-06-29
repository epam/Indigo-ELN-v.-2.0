(function() {
    angular
        .module('indigoeln')
        .controller('ExperimentDetailController', ExperimentDetailController);

    /* @ngInject */
    function ExperimentDetailController($scope, $state, $timeout, $stateParams, Experiment, ExperimentUtil,
                                        PermissionManagement, FileUploaderCash, AutoRecoverEngine, EntitiesBrowser,
                                        Alert, EntitiesCache, $q, AutoSaveEntitiesEngine, Principal, Notebook) {
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

            EntitiesBrowser.setCurrentTabTitle(tabName, $stateParams);
            EntitiesBrowser.setUpdateCurrentEntity(refresh);
            EntitiesBrowser.setSaveCurrentEntity(saveCurrent);
            EntitiesBrowser.setEntityActions({
                save: saveCurrent,
                duplicate: repeatExperiment,
                print: printExperiment
            });
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
            var experimentForSave = _.extend({}, experiment);
            vm.loading = (experiment.template !== null) ? Experiment.update($stateParams, vm.experiment).$promise
                : Experiment.save(experimentForSave).$promise;
            vm.loading.then(function(result) {
                vm.experiment.version = result.version;
                $scope.experimentForm.$setPristine();
                $scope.experimentForm.$dirty = false;
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

        function refresh(noExtend) {
            vm.loading = Experiment.get($stateParams).$promise;
            vm.loading.then(function(result) {
                Alert.success('Experiment updated');
                if (!noExtend) {
                    angular.extend(vm.experiment, result);
                    EntitiesBrowser.changeDirtyTab($stateParams, false);
                    $scope.experimentForm.$setPristine();
                    $scope.experimentForm.$dirty = false;
                } else {
                    vm.experiment.version = result.version;
                }
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
                setReadOnly();
            });
        }

        function initDirtyListener() {
            $timeout(function() {
                EntitiesBrowser.setCurrentForm($scope.experimentForm);
                var tabKind = $state.$current.data.tab.kind;
                if (pageInfo.dirty) {
                    $scope.experimentForm.$setDirty(pageInfo.dirty);
                }
                vm.dirtyListener = $scope.$watch(function() {
                    return vm.experiment;
                }, function() {
                    EntitiesBrowser.changeDirtyTab($stateParams, $scope.experimentForm.$dirty);
                    $timeout(function() {
                        vm.isBtnSaveActive = EntitiesBrowser.getActiveTab().dirty;
                    }, 0);
                }, true);

                AutoRecoverEngine.trackEntityChanges(pageInfo.experiment, $scope.experimentForm, $scope, tabKind, vm);
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

        function initEventListeners() {
            var unsubscribeExp = $scope.$watch(function() {
                return vm.experiment;
            }, function() {
                EntitiesBrowser.setCurrentEntity(vm.experiment);
            });

            var unsubscribe = $scope.$watch(function() {
                return vm.experiment.status;
            }, function() {
                updateStatuses();
                setReadOnly();
            });

            var accessListChangeListener = $scope.$on('access-list-changed', function() {
                vm.experiment.accessList = PermissionManagement.getAccessList();
            });

            var experimentStatusChangeListener = $scope.$on('experiment-status-changed', function(event, data) {
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

            // This is necessary for correct saving after attaching files
            $scope.$on('refresh after attach', function() {
                vm.loading = Experiment.get($stateParams).$promise
                    .then(function(result) {
                        vm.experiment.version = result.version;
                    }, function() {
                        Alert.error('Experiment not refreshed due to server error!');
                    });
            });

            $scope.$on('$destroy', function() {
                unsubscribe();
                unsubscribeExp();
                accessListChangeListener();
                experimentStatusChangeListener();
                vm.dirtyListener();
            });
        }
    }
})();
