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
        });
    }

    function showWarning(dependence) {
        notifyService.warning(translateService.translate('ATTENTION_AUTHORITY_IS_SET_AUTOMATICALLY', {
            permission: _.find(authorities, {name: dependence}).description
        }));
    }

    function checkDependenciesAuthorities(authority, isChecked) {
        _.forEach(authority.dependencies, function(dependence) {
            // Checking dependence only by isChecked === true
            if (isChecked) {
                if (!vm.model[dependence]) {
                    showWarning(dependence);
                }
                vm.model[dependence] = isChecked;
            }

            var foundDependence = _.find(vm.authorities[authority.group], {name: dependence});
            if (foundDependence) {
                foundDependence.isDepended = isChecked;
            }
        });
    }

    function modelChanged(authority, isChecked) {
        checkDependenciesAuthorities(authority, isChecked);
    }

    function roleExistValidation(modelValue) {
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
