angular.module('indigoeln')
    .controller('ExperimentDetailController',
        function ($scope, $rootScope, $state, Experiment, ExperimentUtil, PermissionManagement,
                  FileUploaderCash, AutoSaveEntitiesEngine, AutoRecoverEngine, pageInfo, $uibModal, EntitiesBrowser, $timeout, $stateParams, Alert) {


            var self = this;
            var tabName = pageInfo.notebook.name ? pageInfo.notebook.name + '-' + pageInfo.experiment.name : pageInfo.experiment.name;
            if (pageInfo.experiment.experimentVersion > 1 || !pageInfo.experiment.lastVersion) {
                tabName += ' v' + pageInfo.experiment.experimentVersion;
            }

            EntitiesBrowser.setCurrentTabTitle(tabName, $stateParams);
            $scope.isBtnSaveActive = EntitiesBrowser.getActiveTab().dirty;
            $timeout(function () {
                var tabKind = $state.$current.data.tab.kind;
                if(pageInfo.dirty){
                    $scope.experimentForm.$setDirty(pageInfo.dirty);
                }
                self.dirtyListener = $scope.$watch(tabKind, function () {
                    EntitiesBrowser.changeDirtyTab($stateParams, $scope.experimentForm.$dirty);
                    $timeout(function() {
                            $scope.isBtnSaveActive = EntitiesBrowser.getActiveTab().dirty;
                        }, 0)
                }, true);

                AutoRecoverEngine.trackEntityChanges(pageInfo.experiment, $scope.experimentForm, $scope, tabKind);

            }, 0, false);

            // TODO: the Action drop up button should be disable in case of there is unsaved data.
            $scope.statuses = ['Open', 'Completed', 'Submit_Fail', 'Submitted', 'Archived', 'Signing', 'Signed'];

            $scope.experiment = pageInfo.experiment;
            $scope.notebook = pageInfo.notebook;
            var experimentId = pageInfo.experimentId;
            var projectId = pageInfo.projectId;
            var notebookId = pageInfo.notebookId;
            var params = {projectId: projectId, notebookId: notebookId, experimentId: experimentId};
            var isContentEditor = pageInfo.isContentEditor;
            var hasEditAuthority = pageInfo.hasEditAuthority;

            PermissionManagement.setEntity('Experiment');
            PermissionManagement.setAuthor($scope.experiment.author);
            PermissionManagement.setAccessList($scope.experiment.accessList);
            FileUploaderCash.setFiles([]);

            PermissionManagement.hasPermission('UPDATE_ENTITY').then(function (hasEditPermission) {
                $scope.isEditAllowed = isContentEditor || hasEditAuthority && hasEditPermission;
            });

            var setStatus = function (status) {
                $scope.experiment.status = status;
            };

            var onAccessListChangedEvent = $scope.$on('access-list-changed', function () {
                $scope.experiment.accessList = PermissionManagement.getAccessList();
            });

            var onExperimentStatusChangedEvent = $scope.$on('experiment-status-changed', function(event, data) {
                _.each(data, function(status, id) {
                    if (id === $scope.experiment.fullId) {
                        setStatus(status);
                    }
                });
            });

            $scope.$on('$destroy', function () {
                onAccessListChangedEvent();
                onExperimentStatusChangedEvent();
                self.dirtyListener();
            });

            $scope.save = function (experiment) {
                var experimentForSave = _.extend({}, experiment);
                $scope.loading = (experiment.template !== null)  ? Experiment.update($stateParams, $scope.experiment).$promise 
                    : Experiment.save(experimentForSave).$promise ;
                $scope.loading.then(function (result) {
                    $scope.experiment.version = result.version;
                    $scope.experimentForm.$setPristine();
                }, function () {
                    Alert.error('Experiment is not saved due to server error!')
                });
            };

            var unsubscribeExp = $scope.$watch('experiment', function () {
                EntitiesBrowser.setCurrentExperiment($scope.experiment);
            });

            var unsubscribe = $scope.$watch('experiment.status', function () {
                $scope.isEditAllowed = $scope.isStatusOpen();
            });

            $scope.$on('$destroy', function () {
                unsubscribe();
                unsubscribeExp();
            });

            $scope.completeExperiment = function () {
                $scope.loading = ExperimentUtil.completeExperiment($scope.experiment, params, $scope.notebook.name);
            };

            $scope.completeExperimentAndSign = function () {
                ExperimentUtil.completeExperimentAndSign($scope.experiment, params, $scope.notebook.name);
            };

            $scope.reopenExperiment = function () {
                $scope.loading = ExperimentUtil.reopenExperiment($scope.experiment, params);
            };

            $scope.repeatExperiment = function () {
                $scope.loading = ExperimentUtil.repeatExperiment($scope.experiment, params);
            };

            $scope.versionExperiment = function () {
                $scope.loading = ExperimentUtil.versionExperiment($scope.experiment, params);
            };

            $scope.printExperiment = function () {
                ExperimentUtil.printExperiment(params);
            };
            $scope.refresh = function () {
               $scope.loading = Experiment.get($stateParams).$promise
                .then(function (result) {
                    angular.extend($scope.experiment, result);
                    $scope.experimentForm.$setPristine();
                    $scope.experimentForm.$dirty = false;
                    EntitiesBrowser.changeDirtyTab($stateParams, false);
                }, function () {
                    Alert.error('Experiment not refreshed due to server error!')
                });
            };

            $scope.isStatusOpen = function () {
                return $scope.experiment.status === 'Open';
            };
            $scope.isStatusComplete = function () {
                return $scope.experiment.status === 'Completed';
            };
            $scope.isStatusSubmitFail = function () {
                return $scope.experiment.status === 'Submit_Fail';
            };
            $scope.isStatusSubmitted = function () {
                return $scope.experiment.status === 'Submitted';
            };
            $scope.isStatusArchieved = function () {
                return $scope.experiment.status === 'Archived';
            };
            $scope.isStatusSigned = function () {
                return $scope.experiment.status === 'Signed';
            };
            $scope.isStatusSigning = function () {
                return $scope.experiment.status === 'Signing';
            };

        });
