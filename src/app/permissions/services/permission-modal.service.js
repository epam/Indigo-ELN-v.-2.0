var template = require('./../permissions-view/permission-view.html');

/* @ngInject */
function permissionsModal($uibModal) {
    return {
        showPopup: showPopup,
        close: close
    };

    var dlg;

    function showPopup() {
        dlg = $uibModal.open({
            template: template,
            controller: 'PermissionViewController',
            controllerAs: 'vm',
            size: 'lg'
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
