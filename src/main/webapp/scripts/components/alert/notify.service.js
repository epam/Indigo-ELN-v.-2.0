angular
    .module('indigoeln')
    .factory('notifyService', notifyService);

/* @ngInject */
function notifyService(notify) {
    notify.config({
        container: document.getElementById('page-wrapper'),
        templateUrl: 'scripts/components/alert/alert-template.html',
        startTop: document.getElementById('notify-container').offsetTop
    });

    return {
        success: success,
        error: error,
        warning: warning,
        info: info
    };

    function customNotify(msg, type){ var offset = document.getElementById('page-wrapper').offsetTop;
        return notify({
            container: document.getElementById('page-wrapper'),
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
