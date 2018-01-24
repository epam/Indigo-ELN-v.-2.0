/* @ngInject */
function PermissionsController($uibModalInstance, permissionService, users, permissions, $state,
                               permissionsConstant, notifyService, alertModal, i18en, userPermissions) {
    var vm = this;

    init();

    function init() {
        vm.accessList = buildList();
        vm.permissions = permissions;
        vm.entity = permissionService.getEntity();
        vm.entityId = permissionService.getEntityId();
        vm.parentId = permissionService.getParentId();
        vm.author = permissionService.getAuthor();
        vm.users = users;

        vm.addMember = addMember;
        vm.removeMember = removeMember;
        vm.show = show;
        vm.saveOldPermission = saveOldPermission;
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

        var views = permissionService.getPossiblePermissionViews($state.current.data.entityType);
        // VIEWER as default permission
        var permissionView = _.find(views, {id: userPermissions.VIEWER.id});

        vm.accessList.push({
            user: user,
            permissions: [],
            permissionView: permissionView.id,
            views: views,
            isAuthor: permissionService.isAuthor(user)
        });
    }

    function buildList() {
        var accessList = angular.copy(permissionService.getAccessList());

        _.forEach(accessList, function(permission) {
            permission.views = permissionService.getPossiblePermissionViews($state.current.data.entityType);
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
        if (!member.isAuthor) {
            form.$show();
        }
    }

    function saveOldPermission(permission) {
        vm.oldPermission = permission;
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
}

module.exports = PermissionsController;
