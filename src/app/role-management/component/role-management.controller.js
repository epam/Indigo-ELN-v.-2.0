var roleManagementSaveDialogTemplate = require('../save-dialog/role-management-save-dialog.html');
var roles = require('../../permissions/permission-roles.json');

/* @ngInject */
function RoleManagementController($scope, roleService, accountRoleService, i18en,
                                  $filter, $uibModal, pageInfo, notifyService, roleManagementUtils) {
    var vm = this;
    var ROLE_EDITOR_AUTHORITY = roles.ROLE_EDITOR;

    init();

    function init() {
        vm.accountRoles = pageInfo.accountRoles;
        vm.authorities = pageInfo.authorities;

        vm.search = search;
        vm.hasAuthority = hasAuthority;
        vm.updateAuthoritySelection = updateAuthoritySelection;
        vm.clear = clear;
        vm.save = prepareSave;
        vm.create = create;
        vm.deleteRole = deleteRole;
        vm.edit = edit;
        vm.resetAuthorities = resetAuthorities;
        vm.roleExistValidation = roleExistValidation;

        updateRoles();

        $scope.$watch('vm.role', function(role) {
            initAuthorities(role);
        });
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

    function prepareSave() {
        if (isLastRoleWithRoleEditor()) {
            $uibModal.open({
                animation: true,
                template: roleManagementSaveDialogTemplate,
                controller: 'RoleManagementSaveController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {}
            }).result.then(function(result) {
                if (result === true) {
                    save();
                }
            });
        } else {
            save();
        }
    }

    function save() {
        vm.isSaving = true;
        if (vm.role.id !== null) {
            roleService.update(vm.role, onSaveSuccess, onSaveError);
        } else {
            roleService.save(vm.role, onSaveSuccess, onSaveError);
        }
    }

    function onSaveSuccess() {
        vm.isSaving = false;
        vm.role = null;
        loadAll();
    }

    function onSaveError() {
        vm.isSaving = false;
        notifyService.error('roleService is not saved due to server error!');
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

    function edit(role) {
        loadAll();
        vm.role = _.extend({}, role);
    }

    function resetAuthorities() {
        vm.role.authorities = ['PROJECT_READER'];
        initAuthorities(vm.role);
    }

    function initAuthorities(role) {
        _.each(vm.authorities, function(authority) {
            authority.checked = hasAuthority(role, authority) || authority.name === 'PROJECT_READER';
        });
    }

    function isLastRoleWithRoleEditor() {
        var roleEditorCount = 0;
        var lastRoleWithRoleEditorAuthority = false;
        vm.accountRoles.forEach(function(role) {
            if (role.authorities.indexOf(ROLE_EDITOR_AUTHORITY) >= 0) {
                roleEditorCount++;
                if (roleEditorCount > 1) {
                    lastRoleWithRoleEditorAuthority = false;

                    return;
                }
                if (vm.role.id === role.id &&
                    vm.role.authorities.indexOf(ROLE_EDITOR_AUTHORITY) === -1) {
                    lastRoleWithRoleEditorAuthority = true;
                }
            }
        });

        return lastRoleWithRoleEditorAuthority;
    }
}

module.exports = RoleManagementController;
