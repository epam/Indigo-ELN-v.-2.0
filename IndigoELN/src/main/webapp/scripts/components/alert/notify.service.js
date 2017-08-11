angular
    .module('indigoeln')
    .factory('notifyService', notifyService);

/* @ngInject */
function notifyService(notify) {
    notify.config({
        container: document.getElementById('notify-container'),
        templateUrl: 'scripts/components/alert/alert-template.html',
        offsetTop: -107
    });

    return {
        success: success,
        error: error,
        warning: warning,
        info: info
    };

    function customNotify(msg, type){
        return notify({
            container: document.getElementById('notify-container'),
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
