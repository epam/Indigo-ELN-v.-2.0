var template = require('./../permissions-view/permission-view.html');
var editTemplate = require('./../component/permissions.html');

/* @ngInject */
function permissionsModal($uibModal) {
    return {
        showPopup: showPopup,
        showEditPopup: showEditPopup,
        close: close
    };

    var dlg;

    function showPopup() {
        close();
        dlg = $uibModal.open({
            template: template,
            controller: 'PermissionViewController',
            controllerAs: 'vm',
            size: 'lg'
        });

        return dlg.result;
    }

    function showEditPopup(permissions) {
        var infinity = 10000;
        close();

        dlg = $uibModal.open({
            template: editTemplate,
            controller: 'PermissionsController',
            controllerAs: 'vm',
            size: 'lg',
            resolve: {
                users: function(userWithAuthorityService) {
                    return userWithAuthorityService.query({page: 0, size: infinity}).$promise;
                },
                permissions: function() {
                    return permissions;
                }
            }
        });

        return dlg.result;
    }

    function close() {
        if (dlg) {
            dlg.dismiss();
        }
    }
}

module.exports = permissionsModal;
