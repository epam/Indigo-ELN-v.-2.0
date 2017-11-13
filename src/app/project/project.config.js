var projectTemplate = require('./project.html');

projectConfig.$inject = ['$stateProvider', 'permissionManagementConfig', 'permissionViewManagementConfig',
    'userPermissions'];

function projectConfig($stateProvider, permissionManagementConfig, permissionViewManagementConfig, userPermissions) {
    var permissions = [
        userPermissions.VIEWER,
        userPermissions.USER,
        userPermissions.OWNER
    ];

    $stateProvider
        .state('project', {
            abstract: true,
            parent: 'entity'
        })
        .state('entities.project-new', {
            url: '/project/new',
            views: {
                tabContent: {
                    template: projectTemplate,
                    controller: 'ProjectController',
                    controllerAs: 'vm'
                }
            },
            params: {
                isNewEntity: true
            },
            data: {
                authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR'],
                pageTitle: 'indigoeln',
                tab: {
                    name: 'New Project',
                    service: 'projectService',
                    kind: 'project',
                    type: 'entity',
                    state: 'entities.project-new'
                },
                isNew: true
            },
            resolve: {
                pageInfo: function($q, principalService) {
                    return $q.all([
                        principalService.checkIdentity(),
                        principalService.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                        principalService.hasAuthorityIdentitySafe('PROJECT_CREATOR'),
                        principalService.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR')
                    ]).then(function(results) {
                        return {
                            project: {
                                name: '',
                                description: '',
                                keywords: '',
                                references: ''
                            },
                            identity: results[0],
                            isContentEditor: results[1],
                            hasEditAuthority: results[2],
                            hasCreateChildAuthority: results[3]
                        };
                    });
                }
            }
        })
        .state('entities.project-detail', {
            url: '/project/{projectId}',
            views: {
                tabContent: {
                    template: projectTemplate,
                    controller: 'ProjectController',
                    controllerAs: 'vm'
                }
            },
            data: {
                authorities: ['CONTENT_EDITOR', 'PROJECT_READER', 'PROJECT_CREATOR'],
                pageTitle: 'indigoeln',
                tab: {
                    name: 'Project',
                    service: 'projectService',
                    kind: 'project',
                    type: 'entity',
                    state: 'entities.project-detail'
                }
            },
            resolve: {
                pageInfo: function($q, $stateParams, principalService, projectService) {
                    return $q.all([
                        projectService.get($stateParams).$promise,
                        principalService.checkIdentity(),
                        principalService.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                        principalService.hasAuthorityIdentitySafe('PROJECT_CREATOR'),
                        principalService.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR')
                    ]).then(function(results) {
                        return {
                            project: results[0],
                            identity: results[1],
                            isContentEditor: results[2],
                            hasEditAuthority: results[3],
                            hasCreateChildAuthority: results[4]
                        };
                    });
                }
            }
        })
        .state('entities.project-detail.print', {
            parent: 'entities.project-detail',
            url: '/print',
            onEnter: function(printModal, $stateParams, $state) {
                printModal
                    .showPopup($stateParams, 'projectService')
                    .catch(function() {
                        $state.go('^');
                    });
            },
            data: {
                authorities: ['CONTENT_EDITOR', 'EXPERIMENT_READER', 'EXPERIMENT_CREATOR']
            }
        })
        .state('entities.project-new.permissions', _.extend({}, permissionManagementConfig, {
            parent: 'entities.project-new',
            data: {
                authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
            },
            permissions: permissions
        }))
        .state('entities.project-new.permissions-view', _.extend({}, permissionViewManagementConfig, {
            parent: 'entities.project-new',
            data: {
                authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
            },
            permissions: permissions
        }))
        .state('entities.project-detail.permissions-view', _.extend({}, permissionViewManagementConfig, {
            parent: 'entities.project-detail',
            data: {
                authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
            },
            permissions: permissions
        }))
        .state('entities.project-detail.permissions', _.extend({}, permissionManagementConfig, {
            parent: 'entities.project-detail',
            data: {
                authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
            },
            permissions: permissions
        }))
    ;
}

module.exports = projectConfig;
