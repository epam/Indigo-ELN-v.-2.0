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

var authorities = require('../authorities.json');

/* @ngInject */
function RoleManagementController(roleService, accountRoleService, i18en,
                                  accountRoles, notifyService, roleManagementUtils) {
    var vm = this;

    init();

    function init() {
        vm.accountRoles = accountRoles;
        // TODO: remove
        vm.authorities = authorities;

        vm.page = 1;
        vm.itemsPerPage = 10;
        vm.sortBy = {
            field: 'name',
            isAscending: true
        };

        vm.search = search;
        vm.hasAuthority = hasAuthority;
        vm.updateAuthoritySelection = updateAuthoritySelection;
        vm.clear = clear;
        vm.create = create;
        vm.deleteRole = deleteRole;
        vm.editRole = editRole;
        vm.sortRoles = sortRoles;
        vm.onCloseEditRole = onCloseEditRole;
        vm.loadRoles = loadRoles;

        loadRoles();
    }

    function loadRoles() {
        return roleService.query({
            page: vm.page - 1,
            size: vm.itemsPerPage,
            search: vm.searchText,
            sort: vm.sortBy.field.toLowerCase() + ',' + (vm.sortBy.isAscending ? 'asc' : 'desc')
        })
            .$promise
            .then(function(result) {
                vm.totalItems = result.totalItemsCount;
                vm.roles = result.data;
            });
    }

    function deleteRole(role) {
        roleManagementUtils.openRoleManagementDeleteDialog()
            .then(function() {
                return roleService.delete({id: role.id})
                    .$promise
                    .then(loadRoles, function() {
                        notifyService.error(i18en.THE_ROLE_ALREADY_IN_USE);
                    });
            });
    }

    function search() {
        loadRoles();
    }

    function hasAuthority(role, authority) {
        return role && role.authorities.indexOf(authority.name) !== -1;
    }

    function updateAuthoritySelection(authority) {
        var action = (authority.checked ? 'add' : 'remove');
        updateAuthorities(action, authority);
    }

    function updateAuthorities(action, authority) {
        if (action === 'add' && !vm.hasAuthority(vm.role, authority)) {
            vm.role.authorities.push(authority.name);
        }
        if (action === 'remove' && vm.hasAuthority(vm.role, authority)) {
            vm.role.authorities.splice(
                vm.role.authorities.indexOf(authority.name), 1);
        }
    }

    function clear() {
        vm.role = null;
    }

    function onCloseEditRole() {
        vm.role = null;
        accountRoleService.query({}, function(result) {
            vm.accountRoles = result;
        });

        loadRoles();
    }

    function create() {
        vm.role = {
            id: null, name: null, authorities: ['PROJECT_READER']
        };
    }

    function editRole(role) {
        vm.role = role;
    }

    function sortRoles(predicate, isAscending) {
        vm.sortBy.field = predicate;
        vm.sortBy.isAscending = isAscending;
        loadRoles();
    }
}

module.exports = RoleManagementController;
