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

require('./component/template-management.less');

var entitiesTemplate = require('./component/template-management.html');
var templateDetail = require('./detail/template-detail.html');
var templateModal = require('./modal/template-modal.html');
var templateDeleteDialog = require('./delete-dialog/template-delete-dialog.html');

templateManagementConfig.$inject = ['$stateProvider'];

function templateManagementConfig($stateProvider) {
    $stateProvider
        .state('entities.template', {
            parent: 'entities',
            url: '/templates',
            data: {
                authorities: ['TEMPLATE_EDITOR'],
                pageTitle: 'Templates',
                tab: {
                    name: 'Templates',
                    kind: 'management',
                    state: 'entities.template',
                    type: 'entity',
                    isSingle: true
                }
            },
            views: {
                tabContent: {
                    template: entitiesTemplate,
                    controller: 'TemplateManagementController',
                    controllerAs: 'vm'
                }
            },
            resolve: {}
        })
        .state('entities.template-new', {
            parent: 'entities',
            url: '/template/new',
            data: {
                authorities: ['TEMPLATE_EDITOR'],
                tab: {
                    name: 'New Template',
                    kind: 'management',
                    state: 'entities.template-new',
                    type: 'entity'
                }
            },
            views: {
                tabContent: {
                    template: templateModal,
                    controller: 'TemplateModalController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                pageInfo: function() {
                    return {
                        name: null,
                        id: null
                    };
                }
            }
        })
        .state('entities.template-detail', {
            parent: 'entities',
            url: '/template/{id}',
            data: {
                authorities: ['TEMPLATE_EDITOR'],
                pageTitle: 'Template',
                tab: {
                    name: 'Templates',
                    kind: 'management',
                    state: 'template.detail',
                    type: '',
                    id: null,
                    cache: true,
                    service: 'templateService',
                    params: null,
                    resource: null
                }
            },
            views: {
                tabContent: {
                    template: templateDetail,
                    controller: 'TemplateDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                pageInfo: pageInfo
            }
        })
        .state('entities.template-edit', {
            parent: 'entities',
            url: '/template/{id}/edit',
            data: {
                authorities: ['TEMPLATE_EDITOR'],
                tab: {
                    name: 'Edit Template',
                    kind: 'management',
                    type: 'entity',
                    state: 'entities.template-edit'
                }
            },
            views: {
                tabContent: {
                    template: templateModal,
                    controller: 'TemplateModalController',
                    controllerAs: 'vm'

                }
            },
            resolve: {
                pageInfo: pageInfo
            }
        })
        .state('entities.template.delete', {
            parent: 'entities.template',
            url: '/{id}/delete',
            data: {
                authorities: ['TEMPLATE_EDITOR'],
                tab: {
                    type: ''
                }
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    template: templateDeleteDialog,
                    controller: 'TemplateDeleteController',
                    controllerAs: 'vm',
                    size: 'md'
                }).result.then(function() {
                    $state.go('entities.template', null, {
                        reload: true
                    });
                }, function() {
                    $state.go('^');
                });
            }]
        });

    function pageInfo($stateParams, templateService) {
        return templateService
            .get({id: $stateParams.id})
            .$promise
            .then(function(template) {
                return {
                    entity: template
                };
            });
    }
}

module.exports = templateManagementConfig;
