var types = require('../common/components/alert-modal/types.json');

/* @ngInject */
function roleManagementUtils(alertModal, i18en) {
    return {
        openRoleManagementDeleteDialog: openRoleManagementDeleteDialog
    };

    function openRoleManagementDeleteDialog() {
        // (title, message, size, okCallback, noCallback, okText, hideCancel, noText, type) {
        return alertModal.alert({
            title: i18en.CONFIRM_DELETE_OPERATION,
            message: i18en.ARE_YOU_SURE_YOU_WANT_TO_DELETE_THIS_ROLE,
            size: 'sm',
            okCallback: true,
            noCallback: null,
            okText: i18en.DELETE,
            hideCancel: false,
            type: types.WARNING
        });
    }
}

module.exports = roleManagementUtils;
