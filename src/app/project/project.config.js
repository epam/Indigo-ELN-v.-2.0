var projectTemplate = require('./component/project.html');
var roles = require('../permissions/permission-roles.json');

projectConfig.$inject = ['$stateProvider', 'permissionsConfig', 'permissionViewConfig',
    'userPermissions'];

function projectConfig($stateProvider, permissionsConfig, permissionViewConfig, userPermissions) {
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
                authorities: [roles.CONTENT_EDITOR, roles.PROJECT_CREATOR],
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
                        principalService.hasAuthorityIdentitySafe(roles.CONTENT_EDITOR),
                        principalService.hasAuthorityIdentitySafe(roles.PROJECT_CREATOR),
                        principalService.hasAuthorityIdentitySafe(roles.NOTEBOOK_CREATOR)
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
                authorities: [roles.CONTENT_EDITOR, roles.PROJECT_READER, roles.PROJECT_CREATOR],
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
                        principalService.hasAuthorityIdentitySafe(roles.CONTENT_EDITOR),
                        principalService.hasAuthorityIdentitySafe(roles.PROJECT_CREATOR),
                        principalService.hasAuthorityIdentitySafe(roles.NOTEBOOK_CREATOR)
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
                'ngInject';

                printModal
                    .showPopup($stateParams, 'Project')
                    .finally(function() {
                        $state.go('^', null, {notify: false});
                    });
            },
            data: {
                authorities: [roles.CONTENT_EDITOR, roles.PROJECT_READER, roles.PROJECT_CREATOR]
            }
        })
        .state('entities.project-new.permissions', _.extend({}, permissionsConfig, {
            parent: 'entities.project-new',
            data: {
                authorities: [roles.CONTENT_EDITOR, roles.PROJECT_CREATOR],
                entityType: 'project'
            },
            permissions: permissions
        }))
        .state('entities.project-new.permissions-view', _.extend({}, permissionViewConfig, {
            parent: 'entities.project-new',
            data: {
                authorities: [roles.CONTENT_EDITOR, roles.PROJECT_READER]
            },
            permissions: permissions
        }))
        .state('entities.project-detail.permissions-view', _.extend({}, permissionViewConfig, {
            parent: 'entities.project-detail',
            data: {
                authorities: [roles.CONTENT_EDITOR, roles.PROJECT_READER]
            },
            permissions: permissions
        }))
        .state('entities.project-detail.permissions', _.extend({}, permissionsConfig, {
            parent: 'entities.project-detail',
            data: {
                authorities: [roles.CONTENT_EDITOR, roles.PROJECT_CREATOR],
                entityType: 'project'
            },
            permissions: permissions
        }))
    ;
}

module.exports = projectConfig;
