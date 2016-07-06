angular.module('indigoeln')
    .config(function ($stateProvider, PermissionManagementConfig) {
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
                        controller: 'ExperimentController'
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
                        templateUrl: 'scripts/app/entities/experiment/experiment-new.html',
                        controller: 'ExperimentNewController',
                        size: 'lg',
                        resolve: {
                            pageInfo: function ($q, $stateParams, Template) {
                                var deferred = $q.defer();
                                $q.all([
                                    Template.query().$promise
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
                    pageTitle: 'Experiment'
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/experiment/experiment-detail.html',
                        controller: 'ExperimentDetailController'
                    }
                },
                params: {
                    statusChanged: false
                },
                resolve: {
                    pageInfo: function ($q, $stateParams, Principal, EntitiesBrowser) {
                        var deferred = $q.defer();
                        var params = {
                            projectId: $stateParams.projectId,
                            notebookId: $stateParams.notebookId,
                            experimentId: $stateParams.experimentId
                        };
                        if ($stateParams.statusChanged) {
                            EntitiesBrowser.updateCacheAndTab($stateParams);
                        }
                        $q.all([
                            EntitiesBrowser.getCurrentEntity(params),
                            EntitiesBrowser.getNotebookFromCache(params),
                            Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                            Principal.hasAuthorityIdentitySafe('EXPERIMENT_CREATOR')
                        ]).then(function (results) {
                            deferred.resolve({
                                experiment: results[0],
                                notebook: results[1],
                                isContentEditor: results[2],
                                hasEditAuthority: results[3],
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
                        templateUrl: 'scripts/app/entities/experiment/experiment-delete-dialog.html',
                        controller: 'ExperimentDeleteController',
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
                        templateUrl: 'scripts/app/entities/experiment/experiment-select-parent-template.html',
                        controller: 'ExperimentSelectParentTemplateController',
                        size: 'lg',
                        resolve: {
                            parents: function (NotebooksForSubCreation) {
                                return NotebooksForSubCreation.query().$promise;
                            },
                            templates: function (Template) {
                                return Template.query().$promise;
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
            .state('entities.experiment-detail.preview-print', {
                parent: 'entities.experiment-detail',
                url: '/preview-print',
                data: {
                    authorities: ['CONTENT_EDITOR', 'EXPERIMENT_READER', 'EXPERIMENT_CREATOR'],
                    pageTitle: 'indigoeln'
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/experiment/common/experiment-preview.html',
                        controller: 'ExperimentPrintController'
                    }
                },
                resolve: {
                    pageInfo: function($q, $stateParams, EntitiesBrowser) {
                        var deferred = $q.defer();
                        $q.all([
                            EntitiesBrowser.getCurrentEntity($stateParams),
                            EntitiesBrowser.getNotebookFromCache($stateParams),
                            EntitiesBrowser.getProjectFromCache($stateParams)
                        ]).then(function(results){
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
                    pageTitle: 'indigoeln'
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/experiment/common/experiment-preview.html',
                        controller: 'ExperimentSubmitController'
                    }
                },
                resolve: {
                    pageInfo: function ($q, $stateParams, EntitiesBrowser) {
                        var deferred = $q.defer();
                        $q.all([
                            EntitiesBrowser.getCurrentEntity($stateParams),
                            EntitiesBrowser.getNotebookFromCache($stateParams),
                            EntitiesBrowser.getProjectFromCache($stateParams)
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