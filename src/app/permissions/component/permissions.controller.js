/* @ngInject */
function PermissionsController($uibModalInstance, permissionService, users, permissions,
                               permissionsConstant, notifyService, alertModal, i18en, $state) {
    var vm = this;

    init();

    function init() {
        vm.accessList = buildList();
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
        vm.show = show;
        vm.saveOldPermission = saveOldPermission;
        vm.checkAuthority = checkAuthority;
        vm.ok = ok;
        vm.clear = clear;
    }

    function findUserInAccessList(user) {
        return _.find(vm.accessList, function(permission) {
            return permission.user.id === user.id;
        });
    }

    function addMember(user) {
        if (!user) {
            return;
        }

        if (findUserInAccessList(user)) {
            notifyService.info(i18en.USER_ALREADY_ADDED);

            return;
        }

        var views = permissionService.getPossiblePermissionViews(user, $state.current.data.entityType);
        var permissionView = _.first(views);

        vm.accessList.push({
            user: user,
            permissions: [],
            permissionView: permissionView.id,
            views: views,
            isContentEditor: permissionService.isContentEditor(user),
            isAuthor: permissionService.isAuthor(user)
        });
    }

    function buildList() {
        var accessList = angular.copy(permissionService.getAccessList());

        _.forEach(accessList, function(permission) {
            permission.views = permissionService.getPossiblePermissionViews(permission.user, 'experiment');
            permission.isContentEditor = permissionService.isContentEditor(permission.user);
            permission.isAuthor = permissionService.isAuthor(permission.user);
        });

        return accessList;
    }

    function removeMember(member) {
        var message;

        if (vm.entity === 'Project') {
            message = permissionsConstant.removeProjectWarning;
        } else if (vm.entity === 'Notebook') {
            message = permissionsConstant.removeNotebookWarning;
        }

        permissionService
            .isUserRemovableFromAccessList(member)
            .catch(function() {
                return alertModal.confirm(message, null, angular.noop);
            })
            .then(function() {
                _.remove(vm.accessList, member);
            });
    }

    function show(form, member) {
        if (!member.isAuthor && !member.isContentEditor && member.views.length > 1) {
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

    function convertToAccessList(list) {
        return _.map(list, function(member) {
            return {
                user: member.user,
                permissions: member.permissions,
                permissionView: member.permissionView
            };
        });
    }

    function ok() {
        $uibModalInstance.close(convertToAccessList(vm.accessList));
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
