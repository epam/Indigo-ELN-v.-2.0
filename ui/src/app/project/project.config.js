/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
                            hasCreateChildAuthority: results[4],
                            isNotHavePermissions: false
                        };
                    }, function() {
                        return {
                            project: null,
                            isNotHavePermissions: true
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
            onExit: function(printModal) {
                'ngInject';

                printModal.close();
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
