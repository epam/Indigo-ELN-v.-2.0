(function() {
    angular
        .module('indigoeln')
        .controller('ExperimentDetailController', ExperimentDetailController);

    /* @ngInject */
    function ExperimentDetailController($scope, $state, $timeout, $stateParams, Experiment, ExperimentUtil, PermissionManagement,
                                        FileUploaderCash, AutoRecoverEngine, pageInfo, EntitiesBrowser, Alert) {
        var vm = this;
        var tabName = pageInfo.notebook.name ? pageInfo.notebook.name + '-' + pageInfo.experiment.name : pageInfo.experiment.name;
        var experimentId = pageInfo.experimentId;
        var projectId = pageInfo.projectId;
        var notebookId = pageInfo.notebookId;
        var params = {
            projectId: projectId, notebookId: notebookId, experimentId: experimentId
        };
        var isContentEditor = pageInfo.isContentEditor;
        var hasEditAuthority = pageInfo.hasEditAuthority;
        var hasEditPermission;


        // TODO: the Action drop up button should be disable in case of there is unsaved data.
        vm.statuses = ['Open', 'Completed', 'Submit_Fail', 'Submitted', 'Archived', 'Signing', 'Signed'];
        vm.experiment = pageInfo.experiment;
        vm.notebook = pageInfo.notebook;
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
        vm.isStatusOpen = isStatusOpen;
        vm.isStatusComplete = isStatusComplete;
        vm.isStatusSubmitFail = isStatusSubmitFail;
        vm.isStatusSubmitted = isStatusSubmitted;
        vm.isStatusArchieved = isStatusArchieved;
        vm.isStatusSigned = isStatusSigned;
        vm.isStatusSigning = isStatusSigning;

        init();

        function init() {
            initPermissions();
            FileUploaderCash.setFiles([]);

            if (pageInfo.experiment.experimentVersion > 1 || !pageInfo.experiment.lastVersion) {
                tabName += ' v' + pageInfo.experiment.experimentVersion;
            }

            EntitiesBrowser.setCurrentTabTitle(tabName, $stateParams);
            EntitiesBrowser.setUpdateCurrentEntity(refresh);
            EntitiesBrowser.setSaveCurrentEntity(saveCurrent);
            EntitiesBrowser.setEntityActions({
                save: saveCurrent,
                duplicate: repeatExperiment,
                print: printExperiment
            });

            initDirtyListener();
            initEventListeners();
        }

        function setStatus(status) {
            vm.experiment.status = status;
        }

        function setReadOnly() {
            vm.isEditAllowed = (isContentEditor || hasEditAuthority && hasEditPermission) && vm.isStatusOpen();
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

        function isStatusOpen() {
            return vm.experiment.status === 'Open';
        }

        function isStatusComplete() {
            return vm.experiment.status === 'Completed';
        }

        function isStatusSubmitFail() {
            return vm.experiment.status === 'Submit_Fail';
        }

        function isStatusSubmitted() {
            return vm.experiment.status === 'Submitted';
        }

        function isStatusArchieved() {
            return vm.experiment.status === 'Archived';
        }

        function isStatusSigned() {
            return vm.experiment.status === 'Signed';
        }

        function isStatusSigning() {
            return vm.experiment.status === 'Signing';
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

        function initEventListeners() {
            var unsubscribeExp = $scope.$watch(function() {
                return vm.experiment;
            }, function() {
                EntitiesBrowser.setCurrentEntity(vm.experiment);
            });

            var unsubscribe = $scope.$watch(function() {
                return vm.experiment.status;
            }, function() {
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
