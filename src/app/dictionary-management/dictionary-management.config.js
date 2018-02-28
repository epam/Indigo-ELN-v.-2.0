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

require('./component/dictionary-management.less');

var dictionaryManagementTemplate = require('./component/dictionary-management.html');
var dictionaryManagementDeleteDialogTemplate = require('./delete-dialog/dictionary-management-delete-dialog.html');

dictionaryManagementConfig.$inject = ['$stateProvider'];

function dictionaryManagementConfig($stateProvider) {
    $stateProvider
        .state('entities.dictionary-management', {
            url: '/dictionary-management',
            data: {
                authorities: ['DICTIONARY_EDITOR'],
                pageTitle: 'indigoeln',
                tab: {
                    name: 'Dictionaries',
                    kind: 'management',
                    state: 'entities.dictionary-management',
                    type: 'entity'
                }
            },
            views: {
                tabContent: {
                    template: dictionaryManagementTemplate,
                    controller: 'DictionaryManagementController',
                    controllerAs: 'vm'
                }
            }
        })
        .state('entities.dictionary-management.delete', {
            url: '/dictionary/{id}/delete',
            data: {
                authorities: ['DICTIONARY_EDITOR'],
                tab: {
                    type: ''
                }
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    template: dictionaryManagementDeleteDialogTemplate,
                    controller: 'DictionaryManagementDeleteDialogController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: function(dictionaryService) {
                            return dictionaryService.get({
                                id: $stateParams.id
                            }).$promise;
                        }
                    }
                }).result.then(function() {
                    $state.go('entities.dictionary-management', null, {
                        reload: true
                    });
                }, function() {
                    $state.go('entities.dictionary-management');
                });
            }]
        });
}

module.exports = dictionaryManagementConfig;
