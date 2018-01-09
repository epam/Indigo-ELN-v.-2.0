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
        vm.roleExistValidation = roleExistValidation;
        vm.onCloseEditRole = onCloseEditRole;
        vm.loadAll = loadAll;

        loadAll();
    }

    function loadAll() {
        return roleService.query({
            page: vm.page - 1,
            size: vm.itemsPerPage,
            search: vm.searchText,
            sort: vm.sortBy.field.toLowerCase() + ',' + (vm.sortBy.isAscending ? 'asc' : 'desc')
        }, function(allRoles, headers) {
            vm.totalItems = headers('X-Total-Count');
            vm.roles = allRoles;
        });
    }

    function deleteRole(role) {
        roleManagementUtils.openRoleManagementDeleteDialog()
            .then(function() {
                return roleService.delete({id: role.id})
                    .$promise
                    .then(loadAll, function() {
                        notifyService.error(i18en.THE_ROLE_ALREADY_IN_USE);
                    });
            });
    }

    function roleExistValidation(modelValue) {
        return !_.find(vm.roles, {name: modelValue});
    }

    function search() {
        loadAll();
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

        loadAll();
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
        loadAll();
    }
}

module.exports = RoleManagementController;
