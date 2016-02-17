'use strict';

angular.module('indigoeln')
    .controller('PermissionManagementController', function ($scope, $uibModalInstance, PermissionManagement, users, permissions) {
        $scope.users = users;
        $scope.permissions = permissions;
        $scope.author = PermissionManagement.getAuthor();
        $scope.accessList = PermissionManagement.getAccessList();
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

        $scope.ok = function() {
            $uibModalInstance.close($scope.accessList);
        };

        $scope.clear = function() {
            $uibModalInstance.dismiss('cancel');
        };

    });
