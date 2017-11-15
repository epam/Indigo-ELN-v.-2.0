var template = require('./alert-modal.html');

alertModalService.$inject = ['$uibModal'];

function alertModalService($uibModal) {
    return {
        alert: alert,
        error: error,
        warning: warning,
        info: info,
        confirm: confirm,
        save: save,
        autorecover: autorecover
    };

    function alert(title, message, size, okCallback, noCallback, okText, hideCancel, noText) {
        openAlertModal(title, message, size, okCallback, noCallback, okText, hideCancel, noText);
    }

    function error(msg, size) {
        openAlertModal('Error', msg, size);
    }

    function warning(msg, size) {
        openAlertModal('Warning', msg, size);
    }

    function info(msg, size, okCallback) {
        openAlertModal('Info', msg, size, okCallback, null);
    }

    function confirm(msg, size, okCallback) {
        openAlertModal('Confirm', msg, size, okCallback, null);
    }

    function save(msg, size, callback) {
        openAlertModal('Save', msg, size, callback.bind(null, true), callback.bind(null, false), 'Yes');
    }

    function autorecover(msg, size, okCallback, noCallback) {
        openAlertModal('Info', msg, size, okCallback, noCallback, 'Yes', true);
    }

    function openAlertModal(title, message, size, okCallback, noCallback, okText, hideCancel, noText) {
        $uibModal.open({
            size: size || 'md',
            resolve: {
                title: function() {
                    return title;
                },
                message: function() {
                    return message;
                },
                okText: function() {
                    return okText;
                },
                noText: function() {
                    return noText;
                },
                cancelVisible: function() {
                    return hideCancel ? false : okCallback || noCallback;
                },
                okCallback: function() {
                    return okCallback;
                },
                noCallback: function() {
                    return noCallback;
                }
            },
            template: template,
            controller: 'AlertModalController',
            bindToController: true,
            controllerAs: 'vm'
        });
    }
}

module.exports = alertModalService;
