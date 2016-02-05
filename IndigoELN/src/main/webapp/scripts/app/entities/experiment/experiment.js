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
            .state('experiment.detail', {
                parent: 'entity',
                url: '/experiment/{id}',
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
                        return Experiment.get({id: $stateParams.id});
                    }]
                }
            })
            .state('experiment.new', {
                parent: 'experiment',
                url: 'new/{notebookId}',
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
                    entity: function ($stateParams) {
                        return {
                            title: null,
                            experimentNumber: null,
                            templateId: null,
                            notebookId: $stateParams.notebookId,
                            id: null
                        };
                    }
                }
            })
            .state('experiment.edit', {
                parent: 'experiment',
                url: '{id}/edit',
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
                        return Experiment.get({id: $stateParams.id});
                    }]
                }
            })
            .state('experiment.delete', {
                parent: 'experiment',
                url: '{id}/delete',
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
                                return Experiment.get({id: $stateParams.id});
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
