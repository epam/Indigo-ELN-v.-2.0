var experimentsTemplate = require('./component/experiment.html');
var experimentDeleteDialogTemplate = require('./delete-dialog/experiment-delete-dialog.html');
var roles = require('../permissions/permission-roles.json');

/* @ngInject */
function experimentConfig($stateProvider, permissionsConfig, permissionViewConfig,
                          userPermissions) {
    var permissions = [
        userPermissions.VIEWER,
        userPermissions.OWNER
    ];

    var permissionsViewData = {
        authorities: [roles.CONTENT_EDITOR, roles.EXPERIMENT_READER]
    };

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
                authorities: [roles.EXPERIMENT_READER, roles.EXPERIMENT_CREATOR, roles.CONTENT_EDITOR],
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
            },
            resolve: {
                saltCode: function(appValuesService) {
                    return appValuesService.fetchSaltCodes();
                }
            }
        })
        .state('experiment.delete', {
            parent: 'experiment',
            url: '/delete',
            data: {
                authorities: [roles.EXPERIMENT_REMOVER, roles.CONTENT_EDITOR]
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    template: experimentDeleteDialogTemplate,
                    controller: 'ExperimentDeleteDialogController',
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
        .state('entities.experiment-detail.permissions', _.extend({}, permissionsConfig, {
            parent: 'entities.experiment-detail',
            data: {
                authorities: [roles.CONTENT_EDITOR, roles.EXPERIMENT_CREATOR],
                entityType: 'experiment'
            },
            permissions: permissions
        }))
        .state('entities.experiment-detail.permissions-view', _.extend({}, permissionViewConfig, {
            parent: 'entities.experiment-detail',
            data: permissionsViewData,
            permissions: permissions
        }))
        .state('entities.experiment-detail.print', {
            parent: 'entities.experiment-detail',
            url: '/print',
            onEnter: function(printModal, $stateParams, $state) {
                'ngInject';

                printModal
                    .showPopup($stateParams, 'Experiment')
                    .finally(function() {
                        $state.go('^', null, {notify: false});
                    });
            },
            onExit: function(printModal) {
                'ngInject';

                printModal.close();
            },
            data: {
                authorities: [roles.CONTENT_EDITOR, roles.EXPERIMENT_READER, roles.EXPERIMENT_CREATOR]
            }
        });
}

module.exports = experimentConfig;
