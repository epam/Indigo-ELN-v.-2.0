angular
    .module('indigoeln')
    .factory('Alert', alert);

/* @ngInject */
function alert() {

    //TODO: move to file
    var common = {
        allow_dismiss: true,
        offset: {
            x: 20,
            y: 10
        },
        spacing: 10,
        z_index: 1131,
        delay: 5000,
        timer: 1000,
        placement: {
            from: 'top',
            align: 'right'
        }
    };

    return {
        success: success,
        error: error,
        warning: warning,
        info: info
    };


    function success(msg) {
        $.notify({
            message: msg
        }, _.extend(common, {type: 'success'}));
    }

    function error(msg) {
        $.notify({
            message: msg
        }, _.extend(common, {type: 'danger'}));

    }

    function warning(msg) {
        $.notify({
            message: msg
        }, _.extend(common, {type: 'warning'}));

    }

    function info(msg) {
        $.notify({
            message: msg
        }, _.extend(common, {type: 'info'}));
    }

}
