angular.module('indigoeln')
    .config(function ($stateProvider, PermissionManagementConfig, PermissionViewManagementConfig) {
        $stateProvider
            .state('experiment', {
                parent: 'entity',
                url: '/',
                data: {
                    authorities: [],
                    pageTitle: 'Experiments'
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/experiment/experiments.html',
                        controller: 'ExperimentController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {}
            })
            .state('experiment.new', {
                parent: 'experiment',
                url: 'project/{projectId}/notebook/{notebookId}/experiment/new',
                data: {
                    authorities: ['EXPERIMENT_CREATOR', 'CONTENT_EDITOR']
                },
                onEnter: function ($state, $uibModal) {
                    $uibModal.open({
                        animation: true,
                        templateUrl: 'scripts/app/entities/experiment/creation-from-notebook/experiment-creation-from-notebook.html',
                        controller: 'ExperimentCreationFromNotebookController',
                        controllerAs: 'vm',
                        size: 'lg',
                        resolve: {
                            pageInfo: function ($q, $stateParams, Template) {
                                var deferred = $q.defer();
                                $q.all([
                                    Template.query({
                                        //prevent paging on backend
                                        size: 100000
                                    }).$promise
                                ]).then(function (results) {
                                    deferred.resolve({
                                        entity: {name: null, experimentNumber: null, template: null, id: null},
                                        templates: results[0],
                                        mode: 'new'
                                    });
                                });
                                return deferred.promise;
                            }
                        }
                    }).result.then(function (result) {
                        $state.go('entities.experiment-detail', {
                            notebookId: result.notebookId,
                            projectId: result.projectId,
                            experimentId: result.id
                        });
                    }, function () {
                        $state.go('^');
                    });
                }
            })
            .state('entities.experiment-detail', {
                url: '/project/{projectId}/notebook/{notebookId}/experiment/{experimentId}',
                data: {
                    authorities: ['EXPERIMENT_READER', 'EXPERIMENT_CREATOR', 'CONTENT_EDITOR'],
                    pageTitle: 'Experiment',
                    tab: {
                        name: 'Experiment',
                        service: 'Experiment',
                        kind: 'experiment',
                        type: 'entity',
                        state: 'entities.experiment-detail'
                    }
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/experiment/detail/experiment-detail.html',
                        controller: 'ExperimentDetailController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    pageInfo: function ($q, $stateParams, Principal, Experiment, Notebook, EntitiesCache,
                                        AutoSaveEntitiesEngine, EntitiesBrowser) {

                        var deferred = $q.defer();
                        var params = {
                            projectId: $stateParams.projectId,
                            notebookId: $stateParams.notebookId,
                            experimentId: $stateParams.experimentId
                        };

                        if (!EntitiesCache.get(params)) {
                            EntitiesCache.put(params, AutoSaveEntitiesEngine.autoRecover(Experiment, params));
                        }

                        var notebookParams = {
                            projectId: $stateParams.projectId,
                            notebookId: $stateParams.notebookId
                        };

                        if (!EntitiesCache.get(notebookParams)) {
                            EntitiesCache.put(notebookParams, Notebook.get(notebookParams).$promise);
                        }

                        $q.all([
                            EntitiesCache.get(params),
                            EntitiesCache.get(notebookParams),
                            Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                            Principal.hasAuthorityIdentitySafe('EXPERIMENT_CREATOR'),
                            EntitiesBrowser.getTabByParams($stateParams)
                        ]).then(function (results) {
                            deferred.resolve({
                                experiment: results[0],
                                notebook: results[1],
                                isContentEditor: results[2],
                                hasEditAuthority: results[3],
                                dirty: results[4] ? results[4].dirty : false,
                                experimentId: $stateParams.experimentId,
                                notebookId: $stateParams.notebookId,
                                projectId: $stateParams.projectId
                            });
                        });
                        return deferred.promise;
                    }
                }
            })
            .state('experiment.delete', {
                parent: 'experiment',
                url: '/delete',
                data: {
                    authorities: ['EXPERIMENT_REMOVER', 'CONTENT_EDITOR']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'scripts/app/entities/experiment/delete-dialog/experiment-delete-dialog.html',
                        controller: 'ExperimentDeleteController',
                        controllerAs: 'vm',
                        size: 'md',
                        resolve: {
                            entity: ['Experiment', function (Experiment) {
                                return Experiment.get({
                                    experimentId: $stateParams.id,
                                    notebookId: $stateParams.notebookId
                                }).$promise;
                            }]
                        }
                    }).result.then(function () {
                        $state.go('experiment', null, {reload: true});
                    }, function () {
                        $state.go('^');
                    });
                }]
            })
            .state('experiment.select-notebook-template', {
                parent: 'experiment',
                url: 'experiment/select-notebook-template',
                data: {
                    authorities: ['CONTENT_EDITOR', 'EXPERIMENT_CREATOR'],
                    pageTitle: 'indigoeln'
                },
                onEnter: function ($state, $uibModal) {
                    $uibModal.open({
                        animation: true,
                        templateUrl: 'scripts/app/entities/experiment/creation-from-entities-controls/experiment-creation-from-entities-controls.html',
                        controller: 'ExperimentCreationFromEntitiesControlsController',
                        controllerAs: 'vm',
                        size: 'lg',
                        resolve: {
                            parents: function (NotebooksForSubCreation) {
                                return NotebooksForSubCreation.query().$promise;
                            },
                            templates: function (Template) {
                                return Template.query({
                                    //prevent paging on backend
                                    size: 100000
                                }).$promise;
                            }
                        }
                    }).result.then(function (result) {
                        $state.go('entities.experiment-detail', {
                            notebookId: result.notebookId,
                            projectId: result.projectId,
                            experimentId: result.id
                        });
                    }, function () {
                        $state.go('^');
                    });
                }
            })
            .state('entities.experiment-detail.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'entities.experiment-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'EXPERIMENT_CREATOR']
                },
                permissions: [
                    {id: 'VIEWER', name: 'VIEWER (read experiment)'},
                    {id: 'OWNER', name: 'OWNER (read and update experiment)'}
                ]
            }))
            .state('entities.experiment-detail.permissions-view', _.extend({}, PermissionViewManagementConfig, {
                parent: 'entities.experiment-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'EXPERIMENT_CREATOR']
                },
                permissions: [
                    {id: 'VIEWER', name: 'VIEWER (read experiment)'},
                    {id: 'OWNER', name: 'OWNER (read and update experiment)'}
                ]
            }))
            .state('experiment-preview-print', {
                parent: 'entity',
                url: '/project/{projectId}/notebook/{notebookId}/experiment/{experimentId}/experiment-preview-print',
                data: {
                    authorities: ['CONTENT_EDITOR', 'EXPERIMENT_READER', 'EXPERIMENT_CREATOR'],
                    pageTitle: 'indigoeln',
                    isFullPrint: false
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/experiment/preview/experiment-preview.html',
                        controller: 'ExperimentPreviewController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    pageInfo: function ($q, $stateParams, Experiment, Notebook, Project) {
                        var deferred = $q.defer();
                        $q.all([
                            //EntitiesBrowser.getCurrentEntity($stateParams),
                            //EntitiesBrowser.getNotebookFromCache($stateParams),
                            //EntitiesBrowser.getProjectFromCache($stateParams)

                            Experiment.get($stateParams).$promise,
                            Notebook.get($stateParams).$promise,
                            Project.get($stateParams).$promise


                        ]).then(function (results) {
                            deferred.resolve({
                                experiment: results[0],
                                notebook: results[1],
                                project: results[2]
                            });
                        });
                        return deferred.promise;
                    }
                }
            })
            .state('experiment-preview-submit', {
                parent: 'entity',
                url: '/project/{projectId}/notebook/{notebookId}/experiment/{experimentId}/preview-submit',
                data: {
                    authorities: ['CONTENT_EDITOR', 'EXPERIMENT_READER', 'EXPERIMENT_CREATOR'],
                    pageTitle: 'indigoeln',
                    isFullPrint: true
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/experiment/preview/experiment-preview.html',
                        controller: 'ExperimentPreviewController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    pageInfo: function ($q, $stateParams, Experiment, Notebook, Project) {
                        var deferred = $q.defer();
                        $q.all([
                            //EntitiesBrowser.getCurrentEntity($stateParams),
                            //EntitiesBrowser.getNotebookFromCache($stateParams),
                            //EntitiesBrowser.getProjectFromCache($stateParams)
                            Experiment.get($stateParams).$promise,
                            Notebook.get($stateParams).$promise,
                            Project.get($stateParams).$promise
                        ]).then(function (results) {
                            deferred.resolve({
                                experiment: results[0],
                                notebook: results[1],
                                project: results[2]
                            });
                        });
                        return deferred.promise;
                    }
                }
            });
    });
