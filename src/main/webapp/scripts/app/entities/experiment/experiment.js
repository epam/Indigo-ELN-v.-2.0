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
                url: '/project/{projectId}/notebook/{notebookId}/experiment/new',
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
                    entity: function () {
                        return {
                            name: null,
                            experimentNumber: null,
                            template: null,
                            id: null
                        };
                    },
                    templates: function (Template) {
                        return Template.query().$promise;
                    },
                    mode: function () {
                        return 'new';
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
                resolve: {
                    experiment: function ($stateParams, EntitiesBrowser) {
                        return EntitiesBrowser.getCurrentEntity($stateParams);
                    },
                    identity: function (Principal) {
                        return Principal.identity()
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
                    entity: function (Experiment, $stateParams) {
                        return Experiment.get({
                            experimentId: $stateParams.id,
                            notebookId: $stateParams.notebookId,
                            projectId: $stateParams.projectId
                        }).$promise;
                    },
                    templates: function (Template) {
                        return Template.query().$promise;
                    },
                    mode: function () {
                        return 'edit'
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
                    })
                }]
            });
    });
