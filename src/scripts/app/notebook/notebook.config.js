var template = require('./dialog/notebook-dialog.html');

notebookConfig.$inject = ['$stateProvider', 'permissionManagementConfig', 'permissionViewManagementConfig',
    'userPermissions'];

function notebookConfig($stateProvider, permissionManagementConfig, permissionViewManagementConfig, userPermissions) {
    var permissions = [
        userPermissions.VIEWER,
        userPermissions.USER,
        userPermissions.OWNER
    ];

    var data = {
        authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR']
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
                    controller: 'NotebookDialogController',
                    controllerAs: 'vm'
                }
            },
            params: {
                isNewEntity: true
            },
            data: {
                authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR'],
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
                        principalService.identity(),
                        principalService.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                        principalService.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR'),
                        principalService.hasAuthorityIdentitySafe('EXPERIMENT_CREATOR')
                    ]).then(function(results) {
                        return {
                            notebook: {
                                description: '',
                                name: ''
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
                    controller: 'NotebookDialogController',
                    controllerAs: 'vm'
                }
            },
            data: {
                authorities: ['CONTENT_EDITOR', 'NOTEBOOK_READER', 'NOTEBOOK_CREATOR'],
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
                            principalService.identity(),
                            principalService.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                            principalService.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR'),
                            principalService.hasAuthorityIdentitySafe('EXPERIMENT_CREATOR')
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
            onEnter: function(printModal, $stateParams) {
                printModal.showPopup($stateParams, 'notebookService');
            },
            data: {
                authorities: ['CONTENT_EDITOR', 'EXPERIMENT_READER', 'EXPERIMENT_CREATOR']
            }
        })
        .state('entities.notebook-new.permissions', _.extend({}, permissionManagementConfig, {
            parent: 'entities.notebook-new',
            data: data,
            permissions: permissions
        }))
        .state('entities.notebook-new.permissions-view', _.extend({}, permissionViewManagementConfig, {
            parent: 'entities.notebook-new',
            data: data,
            permissions: permissions
        }))
        .state('entities.notebook-detail.permissions', _.extend({}, permissionManagementConfig, {
            parent: 'entities.notebook-detail',
            data: data,
            permissions: permissions
        }))
        .state('entities.notebook-detail.permissions-view', _.extend({}, permissionViewManagementConfig, {
            parent: 'entities.notebook-detail',
            data: data,
            permissions: permissions
        }));
}

module.exports = notebookConfig;
