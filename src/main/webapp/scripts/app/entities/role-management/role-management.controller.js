(function() {
    angular
        .module('indigoeln')
        .controller('RoleManagementController', RoleManagementController);

    /* @ngInject */
    function RoleManagementController($scope, Role, AccountRole, $filter, $uibModal, pageInfo, notifyService) {
        var ROLE_EDITOR_AUTHORITY = 'ROLE_EDITOR';
        var vm = this;
        vm.roles = pageInfo.roles;
        vm.accountRoles = pageInfo.accountRoles;
        vm.authorities = pageInfo.authorities;

        vm.search = search;
        vm.hasAuthority = hasAuthority;
        vm.updateAuthoritySelection = updateAuthoritySelection;
        vm.clear = clear;
        vm.save = prepareSave;
        vm.create = create;
        vm.edit = edit;
        vm.resetAuthorities = resetAuthorities;


        function search() {
            Role.query({}, function(result) {
                vm.roles = $filter('filter')(result, {
                    name: vm.searchText
                });
            });
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
                    templateUrl: 'scripts/app/entities/role-management/role-management-save-dialog.html',
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
                Role.update(vm.role, onSaveSuccess, onSaveError);
            } else {
                Role.save(vm.role, onSaveSuccess, onSaveError);
            }
        }


        function onSaveSuccess() {
            vm.isSaving = false;
            vm.role = null;
            loadAll();
        }

        function onSaveError() {
            vm.isSaving = false;
            notifyService.error('Role is not saved due to server error!');
            loadAll();
        }

        function loadAll() {
            AccountRole.query({}, function(result) {
                vm.accountRoles = result;
            });
            Role.query({}, function(result) {
                vm.roles = result;
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
            vm.accountRoles.forEach(function(accountRole) {
                if (accountRole.authorities.indexOf(ROLE_EDITOR_AUTHORITY) >= 0) {
                    roleEditorCount++;
                    if (roleEditorCount > 1) {
                        lastRoleWithRoleEditorAuthority = false;

                        return;
                    }
                    if (vm.role.id === accountRole.id &&
                        vm.role.authorities.indexOf(ROLE_EDITOR_AUTHORITY) === -1) {
                        lastRoleWithRoleEditorAuthority = true;
                    }
                }
            });

            return lastRoleWithRoleEditorAuthority;
        }

        var unsubscribe = $scope.$watch('vm.role', function(role) {
            initAuthorities(role);
        });

        $scope.$on('$destroy', function() {
            unsubscribe();
        });
    }
})();

