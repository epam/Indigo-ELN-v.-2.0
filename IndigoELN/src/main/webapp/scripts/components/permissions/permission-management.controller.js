angular.module('indigoeln')
    .controller('PermissionManagementController',
        function ($scope, $uibModalInstance, PermissionManagement, users, permissions, Alert, AlertModal) {

            $scope.accessList = PermissionManagement.getAccessList();
            $scope.permissions = permissions;
            $scope.entity = PermissionManagement.getEntity();
            $scope.entityId = PermissionManagement.getEntityId();
            $scope.parentId = PermissionManagement.getParentId();
            $scope.author = PermissionManagement.getAuthor();
            $scope.users = _.filter(users, function(user) {
                return user.id !== $scope.author.id;
            });
            var unsubscribe = $scope.$watch('selectedMembers', function (user) {
                $scope.addMember(user);
            });
            $scope.$on('$destroy', function () {
                unsubscribe();
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
                var message;
                var callback = function() {
                    $scope.accessList = _.without($scope.accessList, member);
                };
                if ($scope.entity === 'Project') {
                    message = 'You are trying to remove USER who has access to notebooks or ' +
                        'experiments within this project. By removing this USER you block his (her) ' +
                        'access to notebook or experiments withing this project';
                } else if ($scope.entity === 'Notebook') {
                    message = 'You are trying to remove USER who has access to experiments within this notebook. ' +
                        'By removing this USER you block his (her) access to experiments withing this notebook';
                }
                PermissionManagement.isUserRemovableFromAccessList(member).then(function(isRemovable) {
                    if (isRemovable) {
                        callback();
                    } else {
                        AlertModal.confirm(message, null, callback);
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
