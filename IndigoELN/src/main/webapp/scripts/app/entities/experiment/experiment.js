'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
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
                url: 'notebook/{notebookId}/experiment/new',
                data: {
                    authorities: ['EXPERIMENT_READER', 'CONTENT_EDITOR'],
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/experiment/experiment-dialog.html',
                        controller: 'ExperimentDialogController'
                    }
                },
                resolve: {
                    entity: function () {
                        return {
                            title: null,
                            experimentNumber: null,
                            templateId: null,
                            id: null
                        };
                    }
                }
            })
            .state('experiment.detail', {
                parent: 'entity',
                url: '/notebook/{notebookId}/experiment/{id}',
                data: {
                    authorities: ['EXPERIMENT_READER', 'CONTENT_EDITOR'],
                    pageTitle: 'Experiment'
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/experiment/experiment-detail.html',
                        controller: 'ExperimentDetailController'
                    }
                },
                resolve: {
                    entity: ['$stateParams', 'Experiment', function ($stateParams, Experiment) {
                        return Experiment.get({experimentId: $stateParams.id, notebookId: $stateParams.notebookId});
                    }]
                }
            })
            .state('experiment.edit', {
                parent: 'experiment',
                url: 'notebook/{notebookId}/experiment/{id}/edit',
                data: {
                    authorities: ['EXPERIMENT_READER', 'CONTENT_EDITOR'],
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/experiment/experiment-dialog.html',
                        controller: 'ExperimentDialogController'
                    }
                },
                resolve: {
                    entity: ['Experiment', function (Experiment) {
                        return Experiment.get({experimentId: $stateParams.id, notebookId: $stateParams.notebookId});
                    }]
                }
            })
            .state('experiment.delete', {
                parent: 'experiment',
                url: 'notebook/{notebookId}/experiment/{id}/delete',
                data: {
                    authorities: ['EXPERIMENT_READER', 'CONTENT_EDITOR']
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
                                });
                            }]
                        }
                    }).result.then(function (result) {
                        $state.go('experiment', null, {reload: true});
                    }, function () {
                        $state.go('^');
                    })
                }]
            });
    });
