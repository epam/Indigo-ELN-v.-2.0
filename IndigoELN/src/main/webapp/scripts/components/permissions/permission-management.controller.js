(function() {
    angular
        .module('indigoeln')
        .controller('PermissionManagementController', PermissionManagementController);

    /* @ngInject */
    function PermissionManagementController($rootScope, $scope, $uibModalInstance, PermissionManagement, users, permissions, Alert, AlertModal) {
        var vm = this;

        vm.accessList = PermissionManagement.getAccessList();
        vm.permissions = permissions;
        vm.entity = PermissionManagement.getEntity();
        vm.entityId = PermissionManagement.getEntityId();
        vm.parentId = PermissionManagement.getParentId();
        vm.author = PermissionManagement.getAuthor();
        vm.users = filterUsers(users);

        vm.addMember = addMember;
        vm.removeMember = removeMember;
        vm.isAuthor = isAuthor;
        vm.show = show;
        vm.saveOldPermission = saveOldPermission;
        vm.checkAuthority = checkAuthority;
        vm.ok = ok;
        vm.clear = clear;

        init();

        function init() {
            var unsubscribe = $scope.$watch('vm.selectedMembers', function(user) {
                vm.addMember(user);
            });
            $scope.$on('$destroy', function() {
                unsubscribe();
            });
        }

        function addMember(member) {
            if (member) {
                var members = _.map(vm.accessList, 'user');
                var memberIds = _.map(members, 'id');
                if (!_.includes(memberIds, member.id)) {
                    vm.accessList.push({
                        user: member,
                        permissions: [],
                        permissionView: vm.permissions[0].id
                    });
                }
            }
        }

        function removeMember(member) {
            var message;
            var callback = function() {
                vm.accessList = _.without(vm.accessList, member);
            };

            if (vm.entity === 'Project') {
                message = 'You are trying to remove USER who has access to notebooks or ' +
                    'experiments within this project. By removing this USER you block his (her) ' +
                    'access to notebook or experiments withing this project';
            } else if (vm.entity === 'Notebook') {
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
        }

        function isAuthor(member) {
            return member.user.login === vm.author.login;
        }

        function show(form, member) {
            if (!vm.isAuthor(member)) {
                form.$show();
            }
        }

        function saveOldPermission(permission) {
            vm.oldPermission = permission;
        }

        function checkAuthority(member, permission) {
            if (!PermissionManagement.hasAuthorityForPermission(member, permission)) {
                Alert.warning('This user cannot be set as ' + permission + ' as he does not have ' +
                    'sufficient privileges in the system, please select another permissions level');
                member.permissionView = vm.oldPermission;
            }
        }

        function ok() {
            $uibModalInstance.close(vm.accessList);
            // broadcast for activate save button on page
            $rootScope.$broadcast('activate button', 0);
        }

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function filterUsers(users) {
            return _.filter(users, function(user) {
                if (user.id !== vm.author.id && PermissionManagement.hasAuthorityForPermission({
                    user: user
                }, vm.permissions[0].id)) {
                    return true;
                }

                return false;
            });
        }
    }
})();
