(function() {
    angular
        .module('indigoeln')
        .controller('NotebookDialogController', NotebookDialogController);

    /* @ngInject */
    function NotebookDialogController($scope, $rootScope, $state, Notebook, Alert, PermissionManagement, modalHelper,
                                      ExperimentUtil, pageInfo, EntitiesBrowser, $timeout, $stateParams, TabKeyUtils,
                                      autorecoveryCache, notebookSummaryExperiments) {
        var vm = this;
        var identity = pageInfo.identity;
        var isContentEditor = pageInfo.isContentEditor;
        var hasEditAuthority = pageInfo.hasEditAuthority;
        var hasCreateChildAuthority = pageInfo.hasCreateChildAuthority;
        var originalNotebook;
        var updateRecovery = updateRecoveryDebounce();

        init();

        function init() {
            vm.stateData = $state.current.data;
            vm.notebook = pageInfo.notebook;
            originalNotebook = angular.copy(vm.notebook);

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
            vm.onChangedDescription = onChangedDescription;

            initPermissions();

            EntitiesBrowser.setSaveCurrentEntity(save);
            EntitiesBrowser.setUpdateCurrentEntity(refresh);
            EntitiesBrowser.setCurrentTabTitle(pageInfo.notebook.name, $stateParams);

            vm.onRestore = onRestore;

            bindEvents();
        }

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

        function onChangedDescription() {
            EntitiesBrowser.changeDirtyTab($stateParams, true);
        }

        function bindEvents() {
            $scope.$watch(function() {
                return vm.notebook;
            }, function(newEntity) {
                toggleDirty(originalNotebook && !angular.equals(originalNotebook, newEntity));
                updateRecovery();
                EntitiesBrowser.setCurrentForm($scope.createNotebookForm);
                // if (EntitiesBrowser.getActiveTab().name === 'New Notebook') {
                //     vm.isBtnSaveActive = true;
                // }
            }, true);

            $scope.$watch('createNotebookForm.$dirty', function(isDirty) {
                vm.isBtnSaveActive = isDirty;
            }, true);

            $scope.$on('access-list-changed', function() {
                vm.notebook.accessList = PermissionManagement.getAccessList();
            });
        }

        function onRestore(recovery) {
            vm.notebook = recovery.notebook;
        }

        function updateRecoveryDebounce() {
            return _.debounce(function() {
                if (vm.notebook) {
                    autorecoveryCache.put($stateParams, {
                        notebook: vm.notebook
                    });
                }
            }, 10);
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
            if (result.status === 400 && result.data.params) {
                var firstParam = _.first(result.data.params);
                if (result.data.params.length > 1 || firstParam.indexOf('-') > -1) {
                    Alert.error('This Notebook name cannot be changed because batches are created within its' +
                        ' experiments');
                } else {
                    Alert.error('This Notebook name is already in use in the system');
                }
                vm.hasError = false;
                partialRefresh();
                return;
            }

            $timeout(function() {
                vm.hasError = true;
            });
            Alert.error('Notebook is not saved due to server error');
        }

        function toggleDirty(isDirty) {
            if (!$scope.createNotebookForm) {
                return;
            }

            var isChanged = _.isBoolean(isDirty) ? isDirty : !$scope.createNotebookForm.$dirty;

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
            vm.notebook = originalNotebook;
            $scope.createNotebookForm.$setPristine();
        }

        function partialRefresh() {
            vm.notebook.name = originalNotebook.name;
            if (vm.notebook.description === originalNotebook.description &&
                _.isEqual(vm.notebook.accessList, originalNotebook.accessList)) {
                $scope.createNotebookForm.$setPristine();
            }
        }

        function save() {
            vm.hasError = false;
            if (vm.notebook.id) {
                vm.loading = Notebook.update($stateParams, vm.notebook).$promise
                    .then(function(result) {
                        vm.notebook.version = result.version;
                        originalNotebook = angular.copy(vm.notebook);
                        $scope.createNotebookForm.$setPristine();
                        EntitiesBrowser.changeDirtyTab($stateParams, false);
                        EntitiesBrowser.setCurrentTabTitle(vm.notebook.name, $stateParams);
                        $rootScope.$broadcast('notebook-changed', {
                            projectId: vm.projectId,
                            notebook: vm.notebook
                        });
                    }, onSaveError);

                return;
            }
            vm.loading = Notebook.save({
                projectId: vm.projectId
            }, vm.notebook, onSaveSuccess, onSaveError).$promise;
        }
    }
})();
