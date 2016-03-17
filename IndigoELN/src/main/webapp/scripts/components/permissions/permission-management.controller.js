'use strict';

angular.module('indigoeln')
    .controller('PermissionManagementController',
        function ($scope, $uibModalInstance, PermissionManagement, users, permissions, Alert, AlertModal, UserRemovableFromProject, UserRemovableFromNotebook) {

            $scope.accessList = PermissionManagement.getAccessList();
            $scope.permissions = permissions;
            $scope.entity = PermissionManagement.getEntity();
            $scope.entityId = PermissionManagement.getEntityId();
            $scope.parentId = PermissionManagement.getParentId();
            $scope.author = PermissionManagement.getAuthor();
            $scope.users = _.filter(users, function(user) {
                return user.id !== $scope.author.id;
            });
            $scope.$watch('selectedMembers', function (user) {
                $scope.addMember(user);
            });

            $scope.addMember = function(member) {
                if (member) {
                    var members = _.pluck($scope.accessList, 'user');
                    var memberIds = _.pluck(members, 'id');
                    if (!_.contains(memberIds, member.id)) {
                        $scope.accessList.push({user: member, permissions: [], permissionView: $scope.permissions[0].id});
                    }
                }
            };

            $scope.removeMember = function(member) {
                var agent, params, message;
                var callback = function() {
                    $scope.accessList = _.without($scope.accessList, member);
                };

                if ($scope.entity.toLowerCase() === 'experiment' || !$scope.entityId) {
                    callback();
                    return;
                }

                if ($scope.entity.toLowerCase() === 'project') {
                    agent = UserRemovableFromProject;
                    params = {projectId: $scope.entityId, userId: member.user.id };
                    message = 'You are trying to remove USER who has access to notebooks or ' +
                        'experiments within this project. By removing this USER you block his (her) ' +
                        'access to notebook or experiments withing this project';
                } else if ($scope.entity.toLowerCase() === 'notebook') {
                    agent = UserRemovableFromNotebook;
                    params = {projectId: $scope.parentId, notebookId: $scope.entityId, userId: member.user.id };
                    message = 'You are trying to remove USER who has access to experiments within this notebook. ' +
                        'By removing this USER you block his (her) access to experiments withing this notebook';
                }

                agent.get(params).$promise.then(function(result) {
                    if (result.isUserRemovable === true) {
                        callback();
                    } else {
                        AlertModal.confirm(message, callback);
                    }
                });
            };

            $scope.isAuthor = function(member) {
                return member.user.login === $scope.author.login;
            };

            $scope.show = function(form, member) {
                if (!$scope.isAuthor(member)) {
                    form.$show();
                }
            };

            $scope.saveOldPermission = function(permission) {
                $scope.oldPermission = permission;
            };

            $scope.checkAuthority = function(member, permission) {
                if (!PermissionManagement.hasAuthorityForPermission(member, permission)) {
                    Alert.warning('This user cannot be set as ' + permission + ' as he does not have ' +
                        'sufficient privileges in the system, please select another permissions level');
                    member.permissionView = $scope.oldPermission;
                }
            };

            $scope.ok = function() {
                $uibModalInstance.close($scope.accessList);
            };

            $scope.clear = function() {
                $uibModalInstance.dismiss('cancel');
            };

    });
