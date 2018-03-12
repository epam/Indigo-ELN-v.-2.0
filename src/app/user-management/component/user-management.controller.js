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

var userManagementPasswordDialogTemplate = require('./user-management-password-dialog.html');

/* @ngInject */
function UserManagementController($scope, $uibModal, userService, parseLinks, passwordRegex, notifyService,
                                  translateService, roleService, $q, principalService) {
    var vm = this;

    var rolesPaging = {
        pageNumber: 0,
        itemsPerPage: 20,
        isLoaded: false
    };

    vm.users = [];
    vm.roles = [];
    vm.page = 1;
    vm.groups = [{name: 'Group 1'}, {name: 'Group 2'}];
    vm.itemsPerPage = 10;
    vm.sortBy = {
        field: 'login',
        isAscending: true
    };
    vm.passwordRegex = passwordRegex;
    vm.passwordIsVisible = false;
    vm.loginValidationText = translateService.translate('LOGIN_HINT');
    vm.passwordValidationText = translateService.translate('PASSWORD_HINT');

    vm.loadAll = loadAll;
    vm.searchRoles = searchRoles;
    vm.loadRolesPage = loadRolesPage;
    vm.setActive = setActive;
    vm.clear = clear;
    vm.saveUser = saveUser;
    vm.create = create;
    vm.edit = edit;
    vm.search = search;
    vm.changePassword = changePassword;
    vm.sortUsers = sortUsers;
    vm.userExistValidation = userExistValidation;
    vm.changePasswordVisibility = function() {
        vm.passwordIsVisible = !vm.passwordIsVisible;
    };

    vm.loadAll();

    function loadAll() {
        userService.query({
            page: vm.page - 1,
            size: vm.itemsPerPage,
            search: vm.searchText,
            sort: vm.sortBy.field + ',' + (vm.sortBy.isAscending ? 'asc' : 'desc')
        }, function(result, headers) {
            vm.links = parseLinks.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.users = result;
        });
    }

    function loadRolesPage(query) {
        if (rolesPaging.isLoaded) {
            return $q.resolve();
        }

        rolesPaging.pageNumber += 1;

        return queryRoles(query);
    }

    function searchRoles(query) {
        rolesPaging.pageNumber = 0;
        vm.roles.length = 0;

        return queryRoles(query);
    }

    function queryRoles(query) {
        return roleService.query({
            page: rolesPaging.pageNumber,
            size: rolesPaging.itemsPerPage,
            search: query
        })
            .$promise
            .then(function(result) {
                vm.roles = vm.roles.concat(result.data);

                rolesPaging.isLoaded = result.totalItemsCount <= vm.roles.length;
            });
    }

    function setActive(user, isActivated) {
        user.activated = isActivated;
        userService.update(user, function() {
            loadAll();
            clear();
        });
    }

    function clear() {
        vm.user = null;
    }

    function saveUser() {
        save()
            .then(onSaveSuccess)
            .catch(onSaveError);
    }

    function onSaveSuccess() {
        vm.isSaving = false;
        vm.user = null;
        loadAll();
    }

    function onSaveError(result) {
        vm.isSaving = false;
        loadAll();

        if (!isEmailCorrect(result)) {
            notifyService.error(translateService.translate('NOTIFY_INCORRECT_EMAIL'));

            return;
        }

        notifyService.error(translateService.translate('NOTIFY_USER_SAVE_ERROR'));
    }

    function isEmailCorrect(result) {
        return !(result.data && _.find(result.data.fieldErrors, {field: 'email'}));
    }

    function save() {
        vm.isSaving = true;

        if (vm.user.id) {
            return userService.update(vm.user).$promise;
        }

        return userService.save(vm.user).$promise;
    }

    function create() {
        vm.user = {
            id: null,
            login: null,
            firstName: null,
            lastName: null,
            email: null,
            activated: true,
            roles: null,
            group: null
        };
    }

    function edit(user) {
        if (user.group) {
            user.group = {
                name: user.group
            };
        }
        loadAll();
        vm.user = _.extend({}, user);
        vm.isEditingSelf = vm.user.id === principalService.getUserId();
    }

    function search() {
        loadAll();
    }

    function changePassword() {
        $uibModal.open({
            animation: true,
            size: 'md',
            template: userManagementPasswordDialogTemplate,
            controllerAs: 'vm',
            controller: function($uibModalInstance, passwordValidationRegex, passwordValidationText) {
                var vm = this;
                vm.passwordIsVisible = false;
                vm.changePasswordVisibility = function() {
                    vm.passwordIsVisible = !vm.passwordIsVisible;
                };

                vm.passwordRegex = passwordValidationRegex;
                vm.passwordValidationText = passwordValidationText;

                vm.cancel = cancel;
                vm.ok = ok;

                function cancel() {
                    $uibModalInstance.dismiss('cancel');
                }

                function ok() {
                    $uibModalInstance.close(vm.password);
                }
            },
            resolve: {
                passwordValidationRegex: function() {
                    return vm.passwordRegex;
                },
                passwordValidationText: function() {
                    return vm.passwordValidationText;
                }
            }
        }).result.then(function(password) {
            vm.user.password = password;

            save()
                .then(function() {
                    notifyService.success(translateService.translate('PASSWORD_CHANGE_SUCCESS'));
                })
                .catch(function() {
                    notifyService.error(translateService.translate('NOTIFY_USER_SAVE_ERROR'));
                })
                .finally(function() {
                    vm.isSaving = false;
                });
        });
    }

    function sortUsers(predicate, isAscending) {
        vm.sortBy.field = predicate;
        vm.sortBy.isAscending = isAscending;
        loadAll();
    }

    function userExistValidation(modelValue) {
        // Skip validation for saved user and empty value
        if (!modelValue || !vm.editForm.login.$dirty || vm.user.login === modelValue) {
            return $q.when(true);
        }

        return userService.isNew({login: modelValue})
            .$promise
            .then(function(result) {
                if (!result.isNew) {
                    // Role with provided name already exist
                    return $q.reject('User login is already in use');
                }

                // Nothing found, validation passes
                return true;
            });
    }
}

module.exports = UserManagementController;
