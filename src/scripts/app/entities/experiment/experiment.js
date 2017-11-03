angular.module('indigoeln')
    .config(function($stateProvider, permissionManagementConfig, permissionViewManagementConfig, userPermissions) {
        var permissions = [
            userPermissions.VIEWER,
            userPermissions.OWNER
        ];

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
            .state('entities.experiment-detail', {
                url: '/project/{projectId}/notebook/{notebookId}/experiment/{experimentId}',
                data: {
                    authorities: ['EXPERIMENT_READER', 'EXPERIMENT_CREATOR', 'CONTENT_EDITOR'],
                    pageTitle: 'Experiment',
                    tab: {
                        name: 'Experiment',
                        service: 'experimentService',
                        kind: 'experiment',
                        type: 'entity',
                        state: 'entities.experiment-detail'
                    }
                },
                views: {
                    tabContent: {
                        template: '<experiment-detail></experiment-detail>'
                    }
                }
            })
            .state('experiment.delete', {
                parent: 'experiment',
                url: '/delete',
                data: {
                    authorities: ['EXPERIMENT_REMOVER', 'CONTENT_EDITOR']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'scripts/app/entities/experiment/delete-dialog/experiment-delete-dialog.html',
                        controller: 'ExperimentDeleteController',
                        controllerAs: 'vm',
                        size: 'md',
                        resolve: {
                            entity: ['experimentService', function(experimentService) {
                                return experimentService.get({
                                    experimentId: $stateParams.id,
                                    notebookId: $stateParams.notebookId
                                }).$promise;
                            }]
                        }
                    }).result.then(function() {
                        $state.go('experiment', null, {
                            reload: true
                        });
                    }, function() {
                        $state.go('^');
                    });
                }]
            })
            .state('entities.experiment-detail.permissions', _.extend({}, permissionManagementConfig, {
                parent: 'entities.experiment-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'EXPERIMENT_CREATOR']
                },
                permissions: permissions
            }))
            .state('entities.experiment-detail.permissions-view', _.extend({}, permissionViewManagementConfig, {
                parent: 'entities.experiment-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'EXPERIMENT_CREATOR']
                },
                permissions: permissions
            }))
            .state('entities.experiment-detail.print', {
                parent: 'entities.experiment-detail',
                url: '/print',
                onEnter: function(printModal, $stateParams) {
                    printModal.showPopup($stateParams, 'experimentService');
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'EXPERIMENT_READER', 'EXPERIMENT_CREATOR']
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
                    pageInfo: function($q, $stateParams, experimentService, notebookService, projectService) {
                        return $q.all([
                            experimentService.get($stateParams).$promise,
                            notebookService.get($stateParams).$promise,
                            projectService.get($stateParams).$promise
                        ]).then(function(results) {
                            return {
                                experiment: results[0],
                                notebook: results[1],
                                project: results[2]
                            };
                        });
                    }
                }
            });
    });
