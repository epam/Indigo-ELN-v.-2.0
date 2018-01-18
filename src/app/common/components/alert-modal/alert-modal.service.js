var template = require('./alert-modal.html');
var types = require('./types.json');

alertModal.$inject = ['$uibModal'];

function alertModal($uibModal) {
    return {
        alert: alert,
        error: error,
        warning: warning,
        info: info,
        confirm: confirm,
        save: save
    };

    function alert(options) {
        return openAlertModal(options);
    }

    function error(message, size) {
        return openAlertModal({
            title: 'Error',
            message: message,
            size: size
        });
    }

    function warning(message, size) {
        return openAlertModal({
            title: 'Warning',
            message: message,
            size: size
        });
    }

    function info(message, size, okCallback) {
        return openAlertModal({
            title: 'Info',
            message: message,
            size: size,
            okCallback: okCallback,
            noCallback: null
        });
    }

    function confirm(message, size, okCallback) {
        return openAlertModal({
            title: 'Confirm',
            message: message,
            size: size,
            okCallback: okCallback,
            noCallback: null
        });
    }

    function save(message, size, okCallback) {
        return openAlertModal({
            title: 'Save',
            message: message,
            size: size,
            okCallback: okCallback.bind(null, true),
            noCallback: okCallback.bind(null, false),
            okText: 'Yes'
        });
    }

    function openAlertModal(options) {
        return $uibModal.open({
            size: options.size || 'md',
            resolve: {
                type: function() {
                    return options.type || types.NORMAL;
                },
                title: function() {
                    return options.title;
                },
                message: function() {
                    return options.message;
                },
                okText: function() {
                    return options.okText;
                },
                noText: function() {
                    return options.noText;
                },
                cancelVisible: function() {
                    return _.isBoolean(options.hideCancel)
                        ? !options.hideCancel
                        : options.okCallback || options.noCallback;
                },
                okCallback: function() {
                    return options.okCallback;
                },
                noCallback: function() {
                    return options.noCallback;
                }
            },
            template: template,
            controller: 'AlertModalController',
            bindToController: true,
            controllerAs: 'vm'
        }).result;
    }
}

module.exports = alertModal;
