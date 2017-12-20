var authorities = require('../authorities.json');

/* @ngInject */
function RoleManagementController($scope, roleService, accountRoleService, i18en,
                                  $filter, accountRoles, notifyService, roleManagementUtils) {
    var vm = this;

    init();

    function init() {
        vm.accountRoles = accountRoles;
        // TODO: remove
        vm.authorities = authorities;

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
        vm.sortByAuthorities = sortByAuthorities;
        vm.roleExistValidation = roleExistValidation;
        vm.onCloseEditRole = onCloseEditRole;

        updateRoles();
    }

    function updateRoles() {
        return roleService.query()
            .$promise
            .then(function(allRoles) {
                vm.roles = allRoles;
                search();
            });
    }

    function deleteRole(role) {
        roleManagementUtils.openRoleManagementDeleteDialog()
            .then(function() {
                return roleService.delete({id: role.id})
                    .$promise
                    .then(updateRoles, function() {
                        notifyService.error(i18en.THE_ROLE_ALREADY_IN_USE);
                    });
            });
    }

    function roleExistValidation(modelValue) {
        return !_.find(vm.filteredRoles, {name: modelValue});
    }

    function search() {
        // Filtering through current table page
        var searchResult = $filter('filter')(vm.roles, {
            name: vm.searchText
        });

        vm.filteredRoles = $filter('orderBy')(searchResult, 'name');
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
        loadAll();
    }

    function loadAll() {
        accountRoleService.query({}, function(result) {
            vm.accountRoles = result;
        });
        roleService.query({}, function(result) {
            vm.filteredRoles = result;
        });
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
        vm.roles = $filter('orderBy')(vm.roles, predicate, !isAscending);

        $scope.$digest();
    }

    function sortByAuthorities(authority, isAscending) {
        vm.sortBy.field = authority;
        vm.sortBy.isAscending = isAscending;
        vm.roles = _.sortBy(vm.roles, function(role) {
            return isAscending
                ? role.authorities.indexOf(authority) === -1
                : role.authorities.indexOf(authority) !== -1;
        });

        $scope.$digest();
    }
}

module.exports = RoleManagementController;
