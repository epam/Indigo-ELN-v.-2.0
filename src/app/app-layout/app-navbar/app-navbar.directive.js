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

var template = require('./app-navbar.html');
var roles = require('../../permissions/permission-roles.json');

function appNavbar() {
    return {
        restrict: 'E',
        controller: NavbarController,
        controllerAs: 'vm',
        template: template,
        bindToController: true,
        scope: {
            onToggleSidebar: '&'
        }
    };
}

NavbarController.$inject = ['$scope', '$state', 'principalService',
    'entitiesBrowserService', 'authService', 'entitiesCache'];

function NavbarController($scope, $state, principalService,
    entitiesBrowserService, authService, entitiesCache) {
    var vm = this;

    vm.GLOBAL_SEARCH = roles.GLOBAL_SEARCH;
    vm.logout = logout;
    vm.search = search;

    init();

    function init() {
        principalService.checkIdentity().then(function(user) {
            vm.user = user;
        });

        $scope.$on('$destroy', function() {
            entitiesCache.clearAll();
        });
    }

    function logout() {
        entitiesBrowserService.closeAllTabs()
        .then(function() {
            entitiesCache.clearAll();
            authService.logout();
        });
    }

    function search() {
        vm.query = (vm.query || '').trim().toLowerCase();
        $state.go('entities.search-panel', {query: vm.query});
    }
}

module.exports = appNavbar;
