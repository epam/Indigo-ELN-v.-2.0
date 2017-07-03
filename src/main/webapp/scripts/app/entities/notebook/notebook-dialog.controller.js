(function() {
    angular
        .module('indigoeln')
        .controller('NotebookDialogController', NotebookDialogController);

    /* @ngInject */
    function NotebookDialogController($scope, $rootScope, $state, Notebook, Alert, PermissionManagement, modalHelper,
                                      ExperimentUtil, pageInfo, EntitiesBrowser, $timeout, $stateParams, TabKeyUtils,
                                      AutoRecoverEngine, NotebookSummaryExperiments) {
        var vm = this;
        var identity = pageInfo.identity;
        var isContentEditor = pageInfo.isContentEditor;
        var hasEditAuthority = pageInfo.hasEditAuthority;
        var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;


        vm.notebook = pageInfo.notebook;
        vm.newNotebook = _.isUndefined(vm.notebook.id) || _.isNull(vm.notebook.id);
        vm.notebook.author = vm.notebook.author || identity;
        vm.notebook.accessList = vm.notebook.accessList || PermissionManagement.getAuthorAccessList(identity);
        vm.projectId = pageInfo.projectId;
        vm.isCollapsed = true;
        vm.isBtnSaveActive = false;
        vm.isSummary = false;
        vm.hasError = false;
        vm.loading = false;
        vm.isEditAllowed = true;
        vm.isCreateChildAllowed = true;


        vm.createExperiment = createExperiment;
        vm.showSummary = showSummary;
        vm.goToExp = goToExp;
        vm.repeatExperiment = repeatExperiment;
        vm.refresh = refresh;
        vm.save = save;

        init();

        function initPermissions() {
            PermissionManagement.setEntity('Notebook');
            PermissionManagement.setEntityId(vm.notebook.id);
            PermissionManagement.setParentId(vm.projectId);
            PermissionManagement.setAuthor(vm.notebook.author);
            PermissionManagement.setAccessList(vm.notebook.accessList);

            // isEditAllowed
            PermissionManagement.hasPermission('UPDATE_ENTITY').then(function(hasEditPermission) {
                vm.isEditAllowed = isContentEditor || hasEditAuthority && hasEditPermission;
            });
            // isCreateChildAllowed
            PermissionManagement.hasPermission('CREATE_SUB_ENTITY').then(function(hasCreateChildPermission) {
                vm.isCreateChildAllowed = isContentEditor || hasCreateChildAuthority && hasCreateChildPermission;
            });
        }

        function initDirtyListener() {
            $timeout(function() {
                var tabKind = $state.$current.data.tab.kind;
                if (pageInfo.dirty) {
                    $scope.createNotebookForm.$setDirty(pageInfo.dirty);
                }

                vm.dirtyListener = $scope.$watch(function() {
                    return vm.notebook;
                }, function() {
                    EntitiesBrowser.setCurrentForm($scope.createNotebookForm);
                    EntitiesBrowser.changeDirtyTab($stateParams, $scope.createNotebookForm.$dirty);
                    if (EntitiesBrowser.getActiveTab().name === 'New Notebook') {
                        vm.isBtnSaveActive = true;
                    } else {
                        $timeout(function() {
                            vm.isBtnSaveActive = EntitiesBrowser.getActiveTab().dirty;
                        }, 0);
                    }
                }, true);
                AutoRecoverEngine.trackEntityChanges(pageInfo.notebook, $scope.createNotebookForm, $scope, tabKind, vm);
            }, 0, false);
        }

        function init() {
            initPermissions();

            EntitiesBrowser.setSaveCurrentEntity(save);
            EntitiesBrowser.setUpdateCurrentEntity(refresh);
            EntitiesBrowser.setCurrentTabTitle(pageInfo.notebook.name, $stateParams);

            initDirtyListener();


            var onAccessListChangedEvent = $scope.$on('access-list-changed', function() {
                vm.notebook.accessList = PermissionManagement.getAccessList();
            });
            $scope.$on('$destroy', function() {
                onAccessListChangedEvent();
                vm.dirtyListener();
            });

            // Activate save button when change permission
            $scope.$on('activate button', function() {
                // If put 0, then save button isn't activated
                $timeout(function() {
                    vm.isBtnSaveActive = true;
                }, 10);
            });
        }
        function createExperiment() {
            var resolve = {
                notebookId: function() {
                    return vm.notebook.id;
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
            vm.loading = NotebookSummaryExperiments.query({
                notebookId: $stateParams.notebookId,
                projectId: $stateParams.projectId
            }).$promise.then(function(data) {
                if (!data.length) {
                    Alert.info('There are no experiments in this notebook');

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
                Alert.error('Cannot get summary right now due to server error');
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
            var mess = (result.status === 400) ? 'This Notebook name is already in use in the system' : 'Notebook is not saved due to server error';
            if (result.data.params.length > 1) {
                mess = 'This Notebook name cannot be changed because batches are created within its experiments';
            }
            $timeout(function() {
                vm.hasError = true;
            });
            Alert.error(mess);
        }

        function refresh() {
            vm.hasError = false;
            vm.loading = Notebook.get($stateParams).$promise
                .then(function(result) {
                    _.extend(vm.notebook, result);
                    $scope.createNotebookForm.$setPristine();
                    $scope.createNotebookForm.$dirty = false;
                    EntitiesBrowser.changeDirtyTab($stateParams, false);
                }, function() {
                    Alert.error('Notebook not refreshed due to server error!');
                });
        }

        function save() {
            vm.hasError = false;
            if (vm.notebook.id) {
                vm.loading = Notebook.update($stateParams, vm.notebook).$promise
                    .then(function(result) {
                        vm.notebook.version = result.version;
                        $scope.createNotebookForm.$setPristine();
                    }, onSaveError);

                return;
            }
            vm.loading = Notebook.save({
                projectId: vm.projectId
            }, vm.notebook, onSaveSuccess, onSaveError).$promise;
        }
    }
})();

