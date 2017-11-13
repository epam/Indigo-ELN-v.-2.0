var template = require('./notify-service.html');

/* @ngInject */
function notifyService(notify, $document) {
    /* eslint angular/module-getter: 0*/
    notify.config({
        template: template,
        startTop: 0
    });

    return {
        success: success,
        error: error,
        warning: warning,
        info: info
    };

    function customNotify(msg, type) {
        return notify({
            container: $document[0].getElementById('content-wrapper'),
            message: msg,
            classes: type
        });
    }

    function success(msg) {
        customNotify(msg, 'success-notify');
    }

    function error(msg) {
        customNotify(msg, 'danger-notify');
    }

    function warning(msg) {
        customNotify(msg, 'warning-notify');
    }

    function info(msg) {
        customNotify(msg, 'info-notify');
    }
}

module.exports = notifyService;
