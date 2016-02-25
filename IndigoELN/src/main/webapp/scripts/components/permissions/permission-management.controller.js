'use strict';

angular.module('indigoeln')
    .controller('PermissionManagementController',
        function ($scope, $uibModalInstance, PermissionManagement, users, permissions, Alert) {

            $scope.accessList = PermissionManagement.getAccessList();
            $scope.permissions = permissions;
            $scope.entity = PermissionManagement.getEntity();
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
                $scope.accessList = _.without($scope.accessList, member);
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
                if (!PermissionManagement.hasAuthorityForProjectPermission(member, permission)) {
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
