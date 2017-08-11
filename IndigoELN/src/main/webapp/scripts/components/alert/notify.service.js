angular
    .module('indigoeln')
    .factory('notifyService', notifyService);

/* @ngInject */
function notifyService(notify) {
    notify.config({
        templateUrl: 'scripts/components/alert/alert-template.html',
        startTop: 0
    });

    return {
        success: success,
        error: error,
        warning: warning,
        info: info
    };

    function customNotify(msg, type){
        return notify({
            container: document.getElementById('content-wrapper'),
            message: msg,
            classes: type
        });
    }

    function success(msg) {
        customNotify(msg, "success-notify");
    }

    function error(msg) {
        customNotify(msg, "danger-notify");
    }

    function warning(msg) {
        customNotify(msg, "warning-notify");
    }

    function info(msg) {
        customNotify(msg);
    }
}
