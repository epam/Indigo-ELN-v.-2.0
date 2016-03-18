'use strict';

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
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/experiment/experiment-dialog.html',
                        controller: 'ExperimentDialogController'
                    }
                },
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
                            Principal.identity(),
                            Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                            Principal.hasAuthorityIdentitySafe('EXPERIMENT_CREATOR')
                        ]).then(function (results) {
                            deferred.resolve({
                                experiment: results[0],
                                notebook: results[1],
                                identity: results[2],
                                isContentEditor: results[3],
                                hasEditAuthority: results[4]
                            });
                        });
                        return deferred.promise;
                    }
                }
            })
            .state('experiment.edit', {
                parent: 'experiment',
                url: '/project/{projectId}/notebook/{notebookId}/experiment/{id}/edit',
                data: {
                    authorities: ['EXPERIMENT_CREATOR', 'CONTENT_EDITOR']
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/experiment/experiment-dialog.html',
                        controller: 'ExperimentDialogController'
                    }
                },
                resolve: {
                    pageInfo: function ($q, $stateParams, Experiment, Template) {
                        var deferred = $q.defer();
                        $q.all([
                            Experiment.get({
                                experimentId: $stateParams.id,
                                notebookId: $stateParams.notebookId,
                                projectId: $stateParams.projectId
                            }).$promise,
                            Template.query().$promise
                        ]).then(function (results) {
                            deferred.resolve({
                                entity: results[0],
                                templates: results[1],
                                mode: 'edit'
                            });
                        });
                        return deferred.promise;
                    }
                }
            })
            .state('experiment.delete', {
                parent: 'experiment.edit',
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
                    }).result.then(function (result) {
                        $state.go('experiment', null, {reload: true});
                    }, function () {
                        $state.go('^');
                    });
                }]
            })
            .state('experiment.select-notebook', {
                parent: 'experiment',
                url: 'experiment/select-notebook',
                data: {
                    authorities: ['CONTENT_EDITOR', 'EXPERIMENT_CREATOR'],
                    pageTitle: 'indigoeln'
                },
                onEnter: function ($state, $uibModal) {
                    $uibModal.open({
                        animation: true,
                        templateUrl: 'scripts/app/entities/experiment/experiment-select-parent.html',
                        controller: 'ExperimentSelectParentController',
                        size: 'lg',
                        resolve: {
                            parents: function (NotebooksForSubCreation) {
                                return NotebooksForSubCreation.query().$promise;
                            }
                        }
                    }).result.then(function (result) {
                        $state.go('experiment.new', {projectId: result.projectId, notebookId: result.notebookId});
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
            .state('experiment.edit.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'experiment.edit',
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
                        templateUrl: 'scripts/app/entities/experiment/experiment-print.html',
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
            //.state('entities.experiment-detail.preview-submit', {
                //parent: 'entities.experiment-detail',
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
                //templateUrl: 'scripts/app/entities/experiment/common/experiment-preview.html',
                //controller: 'ExperimentSubmitController',
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
