var experimentsTemplate = require('./component/experiment.html');
var experimentDeleteDialogTemplate = require('./delete-dialog/experiment-delete-dialog.html');

/* @ngInject */
function experimentConfig($stateProvider, permissionManagementConfig, permissionViewManagementConfig,
                          userPermissions) {
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
                    template: experimentsTemplate,
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
                    template: experimentDeleteDialogTemplate,
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
        });
}

module.exports = experimentConfig;
