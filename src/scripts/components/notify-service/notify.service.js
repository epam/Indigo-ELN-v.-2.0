angular
    .module('indigoeln')
    .factory('notifyService', notifyService);

/* @ngInject */
function notifyService(notify, $document) {
    notify.config({
        templateUrl: 'scripts/components/notify-service/notify-service.html',
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
