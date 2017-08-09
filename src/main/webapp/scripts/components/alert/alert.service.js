angular
    .module('indigoeln')
    .factory('Alert', alert);

/* @ngInject */
function alert(notify) {
    notify.config({
        container: document.getElementById('page-content'),
        templateUrl: 'scripts/components/alert/alert-template.html',
        startTop: -10
    });

    return {
        success: success,
        error: error,
        warning: warning,
        info: info
    };


    function success(msg) {
        notify({
            message: msg,
            classes: "success-notify"
        });
    }

    function error(msg) {
        notify({
            message: msg,
            classes: "danger-notify"
        });
    }

    function warning(msg) {
        notify({
            message: msg,
            classes: "warning-notify"
        });
    }

    function info(msg) {
        notify({
            message: msg
        });
    }
}
