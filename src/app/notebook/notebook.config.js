var template = require('./detail/notebook-detail.html');
var roles = require('../permissions/permission-roles.json');

notebookConfig.$inject = ['$stateProvider', 'permissionsConfig', 'permissionViewConfig',
    'userPermissions'];

function notebookConfig($stateProvider, permissionsConfig, permissionViewConfig, userPermissions) {
    var permissions = [
        userPermissions.VIEWER,
        userPermissions.USER,
        userPermissions.OWNER
    ];

    var data = {
        authorities: [roles.CONTENT_EDITOR, roles.NOTEBOOK_CREATOR],
        entityType: 'notebook'
    };

    $stateProvider
        .state('notebook', {
            abstract: true,
            parent: 'entity'
        })
        .state('entities.notebook-new', {
            url: '/project/{parentId}/notebook/new',
            views: {
                tabContent: {
                    template: template,
                    controller: 'NotebookDetailController',
                    controllerAs: 'vm'
                }
            },
            params: {
                isNewEntity: true
            },
            data: {
                authorities: [roles.CONTENT_EDITOR, roles.NOTEBOOK_CREATOR],
                pageTitle: 'indigoeln',
                tab: {
                    name: 'New Notebook',
                    service: 'notebookService',
                    kind: 'notebook',
                    type: 'entity',
                    state: 'entities.notebook-new'
                },
                isNew: true
            },
            resolve: {
                pageInfo: function($q, $stateParams, principalService) {
                    return $q.all([
                        principalService.checkIdentity(),
                        principalService.hasAuthorityIdentitySafe(roles.CONTENT_EDITOR),
                        principalService.hasAuthorityIdentitySafe(roles.NOTEBOOK_CREATOR),
                        principalService.hasAuthorityIdentitySafe(roles.EXPERIMENT_CREATOR)
                    ]).then(function(results) {
                        return {
                            notebook: {
                                description: '',
                                name: undefined
                            },
                            identity: results[0],
                            isContentEditor: results[1],
                            hasEditAuthority: results[2],
                            hasCreateChildAuthority: results[3],
                            experiments: {},
                            projectId: $stateParams.parentId
                        };
                    });
                }
            }
        })
        .state('entities.notebook-detail', {
            url: '/project/{projectId}/notebook/{notebookId}',
            views: {
                tabContent: {
                    template: template,
                    controller: 'NotebookDetailController',
                    controllerAs: 'vm'
                }
            },
            data: {
                authorities: [roles.CONTENT_EDITOR, roles.NOTEBOOK_READER, roles.NOTEBOOK_CREATOR],
                pageTitle: 'indigoeln',
                tab: {
                    name: 'Notebook',
                    service: 'notebookService',
                    kind: 'notebook',
                    type: 'entity',
                    state: 'entities.notebook-detail'
                }
            },
            resolve: {
                pageInfo: function($q, $stateParams, principalService, notebookService) {
                    return $q
                        .all([
                            notebookService.get($stateParams).$promise,
                            principalService.checkIdentity(),
                            principalService.hasAuthorityIdentitySafe(roles.CONTENT_EDITOR),
                            principalService.hasAuthorityIdentitySafe(roles.NOTEBOOK_CREATOR),
                            principalService.hasAuthorityIdentitySafe(roles.EXPERIMENT_CREATOR)
                        ])
                        .then(function(results) {
                            return {
                                notebook: results[0],
                                identity: results[1],
                                isContentEditor: results[2],
                                hasEditAuthority: results[3],
                                hasCreateChildAuthority: results[4],
                                projectId: $stateParams.projectId
                            };
                        });
                }
            }
        })
        .state('entities.notebook-detail.print', {
            parent: 'entities.notebook-detail',
            url: '/print',
            onEnter: function(printModal, $stateParams, $state) {
                'ngInject';

                printModal
                    .showPopup($stateParams, 'notebookService')
                    .finally(function() {
                        $state.go('^');
                    });
            },
            data: {
                authorities: [roles.CONTENT_EDITOR, roles.EXPERIMENT_READER, roles.EXPERIMENT_CREATOR]
            }
        })
        .state('entities.notebook-new.permissions', _.extend({}, permissionsConfig, {
            parent: 'entities.notebook-new',
            data: data,
            permissions: permissions
        }))
        .state('entities.notebook-new.permissions-view', _.extend({}, permissionViewConfig, {
            parent: 'entities.notebook-new',
            data: data,
            permissions: permissions
        }))
        .state('entities.notebook-detail.permissions', _.extend({}, permissionsConfig, {
            parent: 'entities.notebook-detail',
            data: data,
            permissions: permissions
        }))
        .state('entities.notebook-detail.permissions-view', _.extend({}, permissionViewConfig, {
            parent: 'entities.notebook-detail',
            data: data,
            permissions: permissions
        }));
}

module.exports = notebookConfig;
