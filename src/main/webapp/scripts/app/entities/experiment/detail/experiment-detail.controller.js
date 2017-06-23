(function () {
    angular
        .module('indigoeln')
        .controller('ExperimentDetailController', ExperimentDetailController);

    /* @ngInject */
    function ExperimentDetailController($scope, $state, $timeout, $stateParams, Experiment, ExperimentUtil, PermissionManagement,
                                        FileUploaderCash, AutoRecoverEngine, pageInfo, EntitiesBrowser, Alert) {

        var self = this,
            tabName = pageInfo.notebook.name ? pageInfo.notebook.name + '-' + pageInfo.experiment.name : pageInfo.experiment.name,
            experimentId = pageInfo.experimentId,
            projectId = pageInfo.projectId,
            notebookId = pageInfo.notebookId,
            params = {projectId: projectId, notebookId: notebookId, experimentId: experimentId},
            isContentEditor = pageInfo.isContentEditor,
            hasEditAuthority = pageInfo.hasEditAuthority,
            hasEditPermission;


        if (pageInfo.experiment.experimentVersion > 1 || !pageInfo.experiment.lastVersion) {
            tabName += ' v' + pageInfo.experiment.experimentVersion;
        }

        EntitiesBrowser.setCurrentTabTitle(tabName, $stateParams);

        // TODO: the Action drop up button should be disable in case of there is unsaved data.
        self.statuses = ['Open', 'Completed', 'Submit_Fail', 'Submitted', 'Archived', 'Signing', 'Signed'];
        self.experiment = pageInfo.experiment;
        self.notebook = pageInfo.notebook;
        self.isBtnSaveActive = EntitiesBrowser.getActiveTab().dirty;

        self.save                       = save;
        self.completeExperiment         = completeExperiment;
        self.completeExperimentAndSign  = completeExperimentAndSign;
        self.reopenExperiment           = reopenExperiment;
        self.repeatExperiment           = repeatExperiment;
        self.versionExperiment          = versionExperiment;
        self.printExperiment            = printExperiment;
        self.refresh                    = refresh;
        self.saveCurrent                = saveCurrent;
        self.isStatusOpen               = isStatusOpen;
        self.isStatusComplete           = isStatusComplete;
        self.isStatusSubmitFail         = isStatusSubmitFail;
        self.isStatusSubmitted          = isStatusSubmitted;
        self.isStatusArchieved          = isStatusArchieved;
        self.isStatusSigned             = isStatusSigned;
        self.isStatusSigning            = isStatusSigning;

        $timeout(function () {
            EntitiesBrowser.setCurrentForm($scope.experimentForm);
            var tabKind = $state.$current.data.tab.kind;
            if (pageInfo.dirty) {
                $scope.experimentForm.$setDirty(pageInfo.dirty);
            }
            self.dirtyListener = $scope.$watch(function () {
                return self.experiment;
            }, function () {
                EntitiesBrowser.changeDirtyTab($stateParams, $scope.experimentForm.$dirty);
                $timeout(function () {
                    self.isBtnSaveActive = EntitiesBrowser.getActiveTab().dirty;
                }, 0);
            }, true);

            AutoRecoverEngine.trackEntityChanges(pageInfo.experiment, $scope.experimentForm, $scope, tabKind, self);
        }, 0, false);

        FileUploaderCash.setFiles([]);
        PermissionManagement.setEntity('Experiment');
        PermissionManagement.setAuthor(self.experiment.author);
        PermissionManagement.setAccessList(self.experiment.accessList);
        PermissionManagement.hasPermission('UPDATE_ENTITY').then(function (_hasEditPermission) {
            hasEditPermission = _hasEditPermission;
            setReadOnly();
        });

        EntitiesBrowser.setUpdateCurrentEntity(self.refresh);
        EntitiesBrowser.setSaveCurrentEntity(self.saveCurrent);
        EntitiesBrowser.setEntityActions({
            save: self.saveCurrent,
            duplicate: self.repeatExperiment,
            print: self.printExperiment
        });


        //Activate save button when change permission
        $scope.$on("activate button", function () {
            $timeout(function () {
                self.isBtnSaveActive = true;
                //If put 0, then save button isn't activated
            }, 10);
        });

        //This is necessary for correct saving after attaching files
        $scope.$on("refresh after attach", function () {
            self.loading = Experiment.get($stateParams).$promise
                .then(function (result) {
                    self.experiment.version = result.version;
                }, function () {
                    Alert.error('Experiment not refreshed due to server error!');
                });
        });

        function setStatus(status) {
            self.experiment.status = status;
        }

        function onAccessListChangedEvent() {
            $scope.$on('access-list-changed', function () {
                self.experiment.accessList = PermissionManagement.getAccessList();
            });
        }

        function setReadOnly() {
            self.isEditAllowed = ( isContentEditor || hasEditAuthority && hasEditPermission) && self.isStatusOpen();
            $scope.experimentForm.$$isReadOnly = !self.isEditAllowed;
        }

        function onExperimentStatusChangedEvent() {
            $scope.$on('experiment-status-changed', function (event, data) {
                _.each(data, function (status, id) {
                    if (id === self.experiment.fullId) {
                        setStatus(status);
                        setReadOnly();
                    }
                });
            });
        }

        function save(experiment) {
            var experimentForSave = _.extend({}, experiment);
            self.loading = (experiment.template !== null) ? Experiment.update($stateParams, self.experiment).$promise
                : Experiment.save(experimentForSave).$promise;
            self.loading.then(function (result) {
                self.experiment.version = result.version;
                $scope.experimentForm.$setPristine();
                $scope.experimentForm.$dirty = false;
            }, function () {
                Alert.error('Experiment is not saved due to server error!');
            });
            return self.loading;
        }


        function completeExperiment() {
            self.loading = ExperimentUtil.completeExperiment(self.experiment, params, self.notebook.name);
        }

        function completeExperimentAndSign() {
            ExperimentUtil.completeExperimentAndSign(self.experiment, params, self.notebook.name);
        }

        function reopenExperiment() {
            self.loading = ExperimentUtil.reopenExperiment(self.experiment, params);
        }

        function repeatExperiment() {
            self.loading = ExperimentUtil.repeatExperiment(self.experiment, params);
        }

        function versionExperiment() {
            var original = self.experiment;

            self.loading = ExperimentUtil.versionExperiment(self.experiment, params);
            self.loading.then(function () {
                Experiment.get($stateParams).$promise.then(function (result) {
                    angular.extend(original, result);
                });
            });
        }

        function printExperiment() {
            if ($scope.experimentForm.$dirty) {
                self.save(self.experiment).then(function () {
                    ExperimentUtil.printExperiment(params);
                });
            } else {
                ExperimentUtil.printExperiment(params);
            }
        }

        function refresh(noExtend) {
            self.loading = Experiment.get($stateParams).$promise;
            self.loading.then(function (result) {
                Alert.success('Experiment updated');
                if (!noExtend) {
                    angular.extend(self.experiment, result);
                    EntitiesBrowser.changeDirtyTab($stateParams, false);
                    $scope.experimentForm.$setPristine();
                    $scope.experimentForm.$dirty = false;
                } else {
                    self.experiment.version = result.version;
                }
            }, function () {
                Alert.error('Experiment not refreshed due to server error!');
            });
            return self.loading;
        }

        function saveCurrent() {
            return self.save(self.experiment);
        }

        function isStatusOpen() {
            return self.experiment.status === 'Open';
        }

        function isStatusComplete() {
            return self.experiment.status === 'Completed';
        }

        function isStatusSubmitFail() {
            return self.experiment.status === 'Submit_Fail';
        }

        function isStatusSubmitted() {
            return self.experiment.status === 'Submitted';
        }

        function isStatusArchieved() {
            return self.experiment.status === 'Archived';
        }

        function isStatusSigned() {
            return self.experiment.status === 'Signed';
        }

        function isStatusSigning() {
            return self.experiment.status === 'Signing';
        }

        var unsubscribeExp = $scope.$watch(function () {
            return self.experiment;
        }, function () {
            EntitiesBrowser.setCurrentEntity(self.experiment);
        });


        var unsubscribe = $scope.$watch(function () {
            return self.experiment.status;
        }, function () {
            setReadOnly();
        });

        var accessListChangeListener = $scope.$on('access-list-changed', function () {
            self.experiment.accessList = PermissionManagement.getAccessList();
        });

        var experimentStatusChangeListener = $scope.$on('experiment-status-changed', function (event, data) {
            _.each(data, function (status, id) {
                if (id === self.experiment.fullId) {
                    setStatus(status);
                    setReadOnly();
                }
            });
        });


        $scope.$on('$destroy', function () {
            unsubscribe();
            unsubscribeExp();
            accessListChangeListener();
            experimentStatusChangeListener();
            self.dirtyListener();
        });
    }
})();
