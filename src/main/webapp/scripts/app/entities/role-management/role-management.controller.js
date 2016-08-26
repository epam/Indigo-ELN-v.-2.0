angular.module('indigoeln')
    .controller('RoleManagementController', function ($scope, Role, AccountRole, ParseLinks, $filter, $uibModal, pageInfo) {
        var ROLE_EDITOR_AUTHORITY = 'ROLE_EDITOR';

        $scope.roles = pageInfo.roles;
        $scope.accountRoles = pageInfo.accountRoles;
        $scope.authorities = [
            {
                name: 'USER_EDITOR',
                description: 'User editor',
                tooltip: 'Allow to read / create / update / remove users'
            },
            {
                name: 'ROLE_EDITOR',
                description: 'Role editor',
                tooltip: 'Allow to read / create / update / remove roles'
            },
            {
                name: 'CONTENT_EDITOR',
                description: 'Content editor',
                tooltip: 'Allow to read / create / update / remove entity (Project, Notebook or Experiment) in spite of absence in ACL for this entity or some restrictions by ACL for this entity'
            },
            {
                name: 'TEMPLATE_EDITOR',
                description: 'Template editor',
                tooltip: 'Allow to read / create / update / remove template'
            },
            {
                name: 'DICTIONARY_EDITOR',
                description: 'Dictionary editor',
                tooltip: 'Allow to read / create / update / remove dictionary'
            },
            {
                name: 'PROJECT_READER',
                description: 'Project reader',
                tooltip: 'Allow to read Project. This is required minimal authority for each Role, which cannot be removed',
                readonly: true,
                checked: true
            },
            {name: 'PROJECT_CREATOR', description: 'Project creator', tooltip: 'Allow to create / update Project'},
            {name: 'PROJECT_REMOVER', description: 'Project remover', tooltip: 'Allow to remove Project'},
            {name: 'NOTEBOOK_READER', description: 'Notebook reader', tooltip: 'Allow to read Notebook'},
            {name: 'NOTEBOOK_CREATOR', description: 'Notebook creator', tooltip: 'Allow to create / update Notebook'},
            {name: 'NOTEBOOK_REMOVER', description: 'Notebook remover', tooltip: 'Allow to remove Notebook'},
            {name: 'EXPERIMENT_READER', description: 'Experiment reader', tooltip: 'Allow to read Experiment'},
            {
                name: 'EXPERIMENT_CREATOR',
                description: 'Experiment creator',
                tooltip: 'Allow to create / update Experiment'
            },
            {name: 'EXPERIMENT_REMOVER', description: 'Experiment remover', tooltip: 'Allow to remove Experiment'}
        ];

        function initAuthorities(role) {
            _.each($scope.authorities, function (authority) {
                authority.checked = $scope.hasAuthority(role, authority) || authority.name === 'PROJECT_READER';
            });
        }

        var unsubscribe = $scope.$watch('role', function (role) {
            initAuthorities(role);
        });
        $scope.$on('$destroy', function () {
            unsubscribe();
        });
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
            return role && role.authorities.indexOf(authority.name) !== -1;
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

        $scope.updateAuthoritySelection = function (authority) {
            var action = (authority.checked ? 'add' : 'remove');
            updateAuthorities(action, authority);
        };


        $scope.clear = function () {
            $scope.role = null;
        };

        var onSaveSuccess = function () {
            $scope.isSaving = false;
            $scope.role = null;
            loadAll();
        };

        var onSaveError = function () {
            $scope.isSaving = false;
            loadAll();
        };

        var save = function () {
            $scope.isSaving = true;
            if ($scope.role.id !== null) {
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
                id: null, name: null, authorities: ['PROJECT_READER']
            };
        };

        $scope.edit = function (role) {
            loadAll();
            $scope.role = _.extend({}, role);
        };

        $scope.resetAuthorities = function () {
            $scope.role.authorities = ['PROJECT_READER'];
            initAuthorities($scope.role);
        };

        $scope.search = function () {
            Role.query({}, function (result) {
                $scope.roles = $filter('filter')(result, {name: $scope.searchText});
            });
        };
    });