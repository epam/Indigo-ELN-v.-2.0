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

/* @ngInject */
var permissionsConfig = {
    url: '/permissions',
    onEnter: ['$rootScope', '$state', 'permissionModal', 'permissionService',
        function($rootScope, $state, permissionModal, permissionService) {
            var that = this;

            permissionModal.showEditPopup(that.permissions)
                .then(function(result) {
                    permissionService.setAccessList(result);
                    $rootScope.$broadcast('access-list-changed');
                })
                .finally(function() {
                    $state.go('^');
                });
        }],
    onExit: ['permissionModal',
        function(permissionModal) {
            permissionModal.close();
        }]
};

module.exports = permissionsConfig;
