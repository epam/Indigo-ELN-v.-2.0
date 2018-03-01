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

require('./edit-role.less');
var roles = require('../../permissions/permission-roles.json');
var template = require('./edit-role.html');
var authorities = require('../authorities.json');

function editRole() {
    return {
        template: template,
        scope: {
            role: '=',
            accountRoles: '=',
            onClose: '&'
        },
        controller: EditRoleController,
        controllerAs: 'vm',
        bindToController: true
    };
}

/* @ngInject */
function EditRoleController($scope, notifyService, roleService, alertModal, i18en, translateService, $q) {
    var vm = this;
    var ROLE_EDITOR_AUTHORITY = roles.ROLE_EDITOR;
    var originalRole = angular.copy(vm.role);

    init();

    function init() {
        vm.authorities = _.groupBy(authorities, 'group');

        vm.clear = clear;
        vm.save = prepareSave;
        vm.resetAuthorities = resetAuthorities;
        vm.roleExistValidation = roleExistValidation;
        vm.updateAuthoritySelection = updateAuthoritySelection;
        vm.modelChanged = modelChanged;

        $scope.$watch('vm.role', function(role) {
            initAuthorities(role);

            // Lock dependencies
            _.each(vm.authorities, function(authorityGroup) {
                _.each(authorityGroup, function(authority) {
                    if (authority.dependencies) {
                        checkDependenciesAuthorities(authority, vm.model[authority.name]);
                    }
                });
            });

            // Reset form state after role select
            $scope.editRoleForm.$setPristine();
            originalRole = angular.copy(role);
        });
    }

    function showWarning(dependence) {
        notifyService.warning(translateService.translate('ATTENTION_AUTHORITY_IS_SET_AUTOMATICALLY', {
            permission: _.find(authorities, {name: dependence}).description
        }));
    }

    function checkDependenciesAuthorities(authority, isChecked) {
        _.forEach(authority.dependencies, function(dependenceName) {
            var dependantAuthority = _.find(authorities, {name: dependenceName});
            var foundDependence = _.find(vm.authorities[dependantAuthority.group], {name: dependenceName});

            // Checking dependence only by isChecked === true
            if (isChecked) {
                if (!vm.model[dependenceName]) {
                    showWarning(dependenceName);
                }
                vm.model[dependenceName] = isChecked;

                // Check subsequent dependencies
                checkDependenciesAuthorities(dependantAuthority, isChecked);
            }

            if (foundDependence) {
                foundDependence.isDepended = isChecked || isAuthorityBeingDependent(foundDependence.name);
            }
        });
    }

    /**
     * Checks if there are checked authorities that has given dependency
     * @param { String } authorityName - dependency name
     * @returns { boolean }
     */
    function isAuthorityBeingDependent(authorityName) {
        return _.some(authorities, function(authorityItem) {
            var authorityHasThisDependence = authorityItem.dependencies &&
                authorityItem.dependencies.indexOf(authorityName) !== -1;
            var authorityIsChecked = vm.model[authorityItem.name];

            return authorityHasThisDependence && authorityIsChecked;
        });
    }

    function modelChanged(authority, isChecked) {
        checkDependenciesAuthorities(authority, isChecked);
    }

    function roleExistValidation(modelValue) {
        // Skip validation for saved notebook and empty value
        if (!modelValue || !$scope.editRoleForm.name.$dirty || originalRole.name === modelValue) {
            return $q.when(true);
        }

        return roleService.query({search: modelValue})
            .$promise
            .then(function(result) {
                if (result.data.length && _.find(result.data, {name: modelValue})) {
                    // Role with provided name already exist
                    return $q.reject('Role already exist');
                }

                // Nothing found, validation passes
                return true;
            });
    }

    function hasAuthority(role, authority) {
        return role && role.authorities.indexOf(authority.name) !== -1;
    }

    function updateAuthoritySelection(authority) {
        updateAuthorities(authority.checked, authority);
    }

    function updateAuthorities(isAdd, authority) {
        if (isAdd && !hasAuthority(vm.role, authority)) {
            vm.role.authorities.push(authority.name);
        }
        if (!isAdd && hasAuthority(vm.role, authority)) {
            vm.role.authorities.splice(
                vm.role.authorities.indexOf(authority.name), 1);
        }
    }

    function clear() {
        vm.role = null;
    }

    function prepareSave() {
        if (isLastRoleWithRoleEditor()) {
            return alertModal.alert({
                type: 'WARNING',
                title: i18en.CONFIRM_SAVE_OPERATION,
                message: i18en.YOU_ARE_TRYING_TO_SAVE_ROLE_WITHOUT_AUTHORITY,
                okText: i18en.SAVE,
                hideCancel: false
            }).then(save);
        }

        return save();
    }

    function isLastRoleWithRoleEditor() {
        return _.some(vm.accountRoles, function(role) {
            return vm.role.id === role.id && !vm.model[roles.ROLE_EDITOR]
                && _.includes(role.authorities, ROLE_EDITOR_AUTHORITY);
        });
    }

    function save() {
        var role = getRole();

        vm.isSaving = true;
        if (vm.role.id !== null) {
            vm.saving = roleService.update(role, onSaveSuccess, onSaveError).$promise;
        } else {
            vm.saving = roleService.save(role, onSaveSuccess, onSaveError).$promise;
        }

        vm.saving
            .then(vm.onClose, function() {
                notifyService.error('roleService is not saved due to server error!');
            })
            .finally(function() {
                vm.isSaving = false;
            });
    }

    function getRole() {
        return _.extend({}, vm.role, {
            authorities: _.reduce(vm.model, function(array, value, key) {
                if (value) {
                    array.push(key);
                }

                return array;
            }, [])
        });
    }

    function onSaveSuccess() {
        vm.role = null;
        vm.onClose();
    }

    function onSaveError() {
        notifyService.error('roleService is not saved due to server error!');
    }

    function resetAuthorities() {
        initAuthorities(vm.role);
    }

    function initAuthorities(role) {
        var roleAuthorities = (role && role.authorities) || [];

        vm.model = _.reduce(authorities, function(model, autority) {
            model[autority.name] = _.includes(roleAuthorities, autority.name);

            return model;
        }, {});
    }
}

module.exports = editRole;
