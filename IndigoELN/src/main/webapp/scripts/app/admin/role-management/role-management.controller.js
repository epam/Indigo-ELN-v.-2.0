'use strict';

angular.module('indigoeln')
    .controller('RoleManagementController', function ($scope, Role, AccountRole, ParseLinks, $filter, $uibModal,
                                                      roles, accountRoles) {
        var ROLE_EDITOR_AUTHORITY = 'ROLE_EDITOR';

        $scope.roles = roles;
        $scope.accountRoles = accountRoles;
        $scope.authorities = [
            {name: 'USER_EDITOR', description: 'User editor', tooltip: 'Write some tooltip'},
            {name: 'ROLE_EDITOR', description: 'Role editor', tooltip: 'Write some tooltip'},
            {name: 'CONTENT_EDITOR', description: 'Content editor', tooltip: 'Write some tooltip'},
            {name: 'TEMPLATE_EDITOR', description: 'Template editor', tooltip: 'Write some tooltip'},
			{name: 'DICTIONARY_EDITOR', description: 'Dictionary editor', tooltip: 'Write some tooltip'},
            {name: 'PROJECT_READER', description: 'Project reader', tooltip: 'This is required minimal authority for each Role, which cannot be removed', readonly: true, checked: true},
            {name: 'PROJECT_CREATOR', description: 'Project creator', tooltip: 'Write some tooltip'},
            {name: 'PROJECT_REMOVER', description: 'Project remover', tooltip: 'Write some tooltip'},
            {name: 'NOTEBOOK_READER', description: 'Notebook reader', tooltip: 'Write some tooltip'},
            {name: 'NOTEBOOK_CREATOR', description: 'Notebook creator', tooltip: 'Write some tooltip'},
            {name: 'NOTEBOOK_REMOVER', description: 'Notebook remover', tooltip: 'Write some tooltip'},
            {name: 'EXPERIMENT_READER', description: 'Experiment reader', tooltip: 'Write some tooltip'},
            {name: 'EXPERIMENT_CREATOR', description: 'Experiment creator', tooltip: 'Write some tooltip'},
            {name: 'EXPERIMENT_REMOVER', description: 'Experiment remover', tooltip: 'Write some tooltip'}
        ];

        function isLastRoleWithRoleEditor() {
            var roleEditorCount = 0;
            var lastRoleWithRoleEditorAuthority = false;
            $scope.accountRoles.forEach(function(accountRole) {
                if (accountRole.authorities.indexOf(ROLE_EDITOR_AUTHORITY) >= 0) {
                    roleEditorCount++;
                    if (roleEditorCount > 1) {
                        lastRoleWithRoleEditorAuthority = false;
                        return;
                    }
                    if ($scope.role.id === accountRole.id &&
                        $scope.role.authorities.indexOf(ROLE_EDITOR_AUTHORITY) === -1) {
                        lastRoleWithRoleEditorAuthority = true;
                    }
                }
            });

            return lastRoleWithRoleEditorAuthority;
        }

        var loadAll = function () {
            AccountRole.query({}, function (result) {
                $scope.accountRoles = result;
            });
            Role.query({}, function (result) {
                $scope.roles = result;
            });

        };

        $scope.hasAuthority = function(role, authority) {
            return role != null && role.authorities.indexOf(authority.name) !== -1;
        };

        var updateAuthorities = function(action, authority) {
            if (action === 'add' && !$scope.hasAuthority($scope.role, authority)) {
                $scope.role.authorities.push(authority.name);
            }
            if (action === 'remove' && $scope.hasAuthority($scope.role, authority)) {
                $scope.role.authorities.splice(
                    $scope.role.authorities.indexOf(authority.name), 1);
            }
        };

        $scope.updateAuthoritySelection = function($event, authority) {
            var checkbox = $event.target;
            var action = (checkbox.checked ? 'add' : 'remove');
            updateAuthorities(action, authority);
        };

        $scope.isAuthoritySelected = function(authority) {
            return $scope.hasAuthority($scope.role, authority);
        };

        $scope.clear = function () {
            $scope.role = null;
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };

        var onSaveSuccess = function (result) {
            $scope.isSaving = false;
            $scope.role = null;
            loadAll();
        };

        var onSaveError = function (result) {
            $scope.isSaving = false;
            loadAll();
        };

        var save = function () {
            $scope.isSaving = true;
            if ($scope.role.id != null) {
                Role.update($scope.role, onSaveSuccess, onSaveError);
            } else {
                Role.save($scope.role, onSaveSuccess, onSaveError);
            }
        };

        $scope.save = function () {
            if (isLastRoleWithRoleEditor()) {
                $uibModal.open({
                    animation: true,
                    templateUrl: 'scripts/app/admin/role-management/role-management-save-dialog.html',
                    controller: 'role-managementSaveController',
                    size: 'md',
                    resolve: {}
                }).result.then(function (result) {
                    if (result === true) {
                        save();
                    }
                });
            } else {
                save();
            }
        };

        $scope.create = function () {
            $scope.role = {
                id: null, name: null, authorities: []
            };
        };

        $scope.edit = function (role) {
            loadAll();
            $scope.role = _.extend({}, role);
        };

        $scope.resetAuthorities = function () {
            $scope.role.authorities = [];
        };

        $scope.search = function () {
            Role.query({}, function (result) {
                $scope.roles = $filter('filter')(result, {name: $scope.searchText});
            });
        };
    });