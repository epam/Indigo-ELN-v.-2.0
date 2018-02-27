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

var template = require('./../permissions-view/permission-view.html');
var editTemplate = require('./../component/permissions.html');

/* @ngInject */
function permissionsModal($uibModal) {
    var dlg;

    return {
        showPopup: showPopup,
        showEditPopup: showEditPopup,
        close: close
    };

    function showPopup() {
        close();
        dlg = $uibModal.open({
            template: template,
            controller: 'PermissionViewController',
            controllerAs: 'vm',
            size: 'lg'
        });

        return dlg.result;
    }

    function showEditPopup(permissions) {
        var infinity = 10000;
        close();

        dlg = $uibModal.open({
            template: editTemplate,
            controller: 'PermissionsController',
            controllerAs: 'vm',
            size: 'lg',
            resolve: {
                users: function(userWithAuthorityService) {
                    return userWithAuthorityService.query({page: 0, size: infinity}).$promise;
                },
                permissions: function() {
                    return permissions;
                }
            }
        });

        return dlg.result;
    }

    function close() {
        if (dlg) {
            dlg.dismiss();
            dlg = null;
        }
    }
}

module.exports = permissionsModal;
