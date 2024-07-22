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

var userManagementTemplate = require('./component/user-management.html');
var userManagementDeleteDialogTemplate = require('./delete-dialog/user-management-delete-dialog.html');

userManagementConfig.$inject = ['$stateProvider'];

function userManagementConfig($stateProvider) {
    $stateProvider
        .state('entities.user-management', {
            url: '/user-management',
            data: {
                authorities: ['USER_EDITOR'],
                pageTitle: 'indigoeln',
                tab: {
                    name: 'Users',
                    kind: 'management',
                    state: 'entities.user-management',
                    type: 'entity'
                }
            },
            views: {
                tabContent: {
                    template: userManagementTemplate,
                    controller: 'UserManagementController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                passwordRegex: function(userPasswordValidationService) {
                    return userPasswordValidationService.get().$promise
                        .then(function(response) {
                            return response.data;
                        });
                }
            }
        })
        .state('entities.user-management.delete', {
            parent: 'entities.user-management',
            url: '/{login}/delete',
            data: {
                authorities: ['USER_EDITOR'],
                tab: {
                    type: ''
                }
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    template: userManagementDeleteDialogTemplate,
                    controller: 'UserManagementDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['userService', function(userService) {
                            return userService.get({
                                login: $stateParams.login
                            });
                        }]
                    }
                }).result.then(function() {
                    $state.go('entities.user-management', null, {
                        reload: true
                    });
                }, function() {
                    $state.go('^');
                });
            }]
        });
}

module.exports = userManagementConfig;
