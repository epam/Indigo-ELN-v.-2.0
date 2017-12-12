var userManagementPasswordDialogTemplate = require('./user-management-password-dialog.html');

/* @ngInject */
function UserManagementController($uibModal, userService, parseLinks, $filter, pageInfo, passwordRegex, notifyService) {
    var vm = this;
    var usersModel = [];

    vm.users = [];
    vm.roles = pageInfo.roles;
    vm.page = 1;
    vm.itemsPerPage = 10;
    vm.sortBy = {
        field: 'login',
        isAscending: true
    };
    vm.passwordRegex = passwordRegex;
    vm.loginValidationText = $filter('translate')('LOGIN_HINT');
    vm.passwordValidationText = $filter('translate')('PASSWORD_HINT');

    vm.loadAll = loadAll;
    vm.setActive = setActive;
    vm.clear = clear;
    vm.saveUser = saveUser;
    vm.create = create;
    vm.edit = edit;
    vm.search = search;
    vm.changePassword = changePassword;
    vm.sortUsers = sortUsers;

    vm.loadAll();

    function loadAll() {
        userService.query({
            page: vm.page - 1,
            size: vm.itemsPerPage,
            sort: vm.sortBy.field + ',' + (vm.sortBy.isAscending ? 'asc' : 'desc')
        }, function(result, headers) {
            vm.links = parseLinks.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.users = result;
            usersModel = result;
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
            notifyService.error('Email address is incorrect!');

            return;
        }

        notifyService.error('User is not saved due to server error!');
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
    }

    function search() {
        // Filtering through current table page
        vm.users = $filter('filter')(usersModel, vm.searchText);
    }

    function changePassword() {
        $uibModal.open({
            animation: true,
            size: 'md',
            template: userManagementPasswordDialogTemplate,
            controllerAs: 'vm',
            controller: function($scope, $uibModalInstance, passwordValidationRegex, passwordValidationText) {
                var vm = this;

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
                    vm.isSaving = false;
                    notifyService.success($filter('translate')('PASSWORD_CHANGE_SUCCESS'));
                })
                .catch(function() {
                    vm.isSaving = false;
                    notifyService.error('User is not saved due to server error!');
                });
        });
    }

    function sortUsers(predicate, isAscending) {
        vm.sortBy.field = predicate;
        vm.sortBy.isAscending = isAscending;
        loadAll();
    }
}

module.exports = UserManagementController;
