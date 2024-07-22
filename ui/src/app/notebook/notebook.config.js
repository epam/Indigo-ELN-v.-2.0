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

    var permissionsViewData = {
        authorities: [roles.CONTENT_EDITOR, roles.NOTEBOOK_READER],
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
                                projectId: $stateParams.projectId,
                                isNotHavePermissions: false
                            };
                        }, function() {
                            return {
                                notebook: null,
                                isNotHavePermissions: true
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
                    .showPopup($stateParams, 'Notebook')
                    .finally(function() {
                        $state.go('^', null, {notify: false});
                    });
            },
            onExit: function(printModal) {
                'ngInject';

                printModal.close();
            },
            data: {
                authorities: [roles.CONTENT_EDITOR, roles.NOTEBOOK_READER, roles.NOTEBOOK_CREATOR]
            }
        })
        .state('entities.notebook-new.permissions', _.extend({}, permissionsConfig, {
            parent: 'entities.notebook-new',
            data: data,
            permissions: permissions
        }))
        .state('entities.notebook-new.permissions-view', _.extend({}, permissionViewConfig, {
            parent: 'entities.notebook-new',
            data: permissionsViewData,
            permissions: permissions
        }))
        .state('entities.notebook-detail.permissions', _.extend({}, permissionsConfig, {
            parent: 'entities.notebook-detail',
            data: data,
            permissions: permissions
        }))
        .state('entities.notebook-detail.permissions-view', _.extend({}, permissionViewConfig, {
            parent: 'entities.notebook-detail',
            data: permissionsViewData,
            permissions: permissions
        }));
}

module.exports = notebookConfig;
