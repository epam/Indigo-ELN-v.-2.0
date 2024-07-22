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
