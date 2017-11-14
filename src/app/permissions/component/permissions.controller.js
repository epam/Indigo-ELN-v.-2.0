/* @ngInject */
function PermissionsController($scope, $uibModalInstance, permissionService, users, permissions,
                                        permissionsConstant, notifyService, alertModal) {
    var vm = this;

    init();

    function init() {
        vm.accessList = permissionService.getAccessList();
        vm.permissions = permissions;
        vm.entity = permissionService.getEntity();
        vm.entityId = permissionService.getEntityId();
        vm.parentId = permissionService.getParentId();
        vm.author = permissionService.getAuthor();

        if (vm.author) {
            vm.users = filterUsers(users);
        }

        vm.addMember = addMember;
        vm.removeMember = removeMember;
        vm.isAuthor = isAuthor;
        vm.show = show;
        vm.saveOldPermission = saveOldPermission;
        vm.checkAuthority = checkAuthority;
        vm.ok = ok;
        vm.clear = clear;

        bindEvents();
    }

    function bindEvents() {
        $scope.$watch('vm.selectedMembers', function(user) {
            vm.addMember(user);
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
            message = permissionsConstant.removeProjectWarning;
        } else if (vm.entity === 'Notebook') {
            message = permissionsConstant.removeNotebookWarning;
        }

        permissionService.isUserRemovableFromAccessList(member).then(function(isRemovable) {
            if (isRemovable) {
                callback();
            } else {
                alertModal.confirm(message, null, callback);
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
        if (!permissionService.hasAuthorityForPermission(member, permission)) {
            notifyService.warning(permissionsConstant.checkAuthorityWarning(permission));
            member.permissionView = vm.oldPermission;
        }
    }

    function ok() {
        $uibModalInstance.close(vm.accessList);
    }

    function clear() {
        $uibModalInstance.dismiss('cancel');
    }

    function filterUsers(nonFilteredUsers) {
        return _.filter(nonFilteredUsers, function(user) {
            return user.id !== vm.author.id && permissionService.hasAuthorityForPermission(
                {user: user},
                vm.permissions[0].id
            );
        });
    }
}

module.exports = PermissionsController;
