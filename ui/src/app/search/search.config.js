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

var template = require('./component/search-panel.html');

searchConfig.$inject = ['$stateProvider'];

function searchConfig($stateProvider) {
    $stateProvider
        .state('entities.search-panel', {
            url: '/search',
            params: {query: null},
            views: {
                tabContent: {
                    template: template,
                    controller: 'SearchPanelController',
                    controllerAs: 'vm'
                }
            },
            data: {
                pageTitle: 'indigoeln',
                tab: {
                    name: 'Search',
                    kind: 'search-panel',
                    type: 'entity',
                    state: 'entities.search-panel',
                    isSingle: true
                }
            },
            resolve: {
                pageInfo: function($q, principalService, usersService) {
                    return $q.all([
                        usersService.get(),
                        principalService.checkIdentity()
                    ]).then(function(results) {
                        return {
                            users: results[0],
                            identity: results[1]
                        };
                    });
                }
            }
        });
}

module.exports = searchConfig;
