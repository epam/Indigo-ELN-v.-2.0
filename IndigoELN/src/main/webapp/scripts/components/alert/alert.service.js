angular
    .module('indigoeln')
    .factory('Alert', alert);

/* @ngInject */
function alert(notify, $scope) {
    // TODO: move to file
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

    notify.config({
        container: document.getElementById('page-content'),
        startTop: 0
    });

    return {
        success: success,
        error: error,
        warning: warning,
        info: info
    };


    function success(msg) {
        $scope.success = 'success';
        notify({
            message: msg,
            templateUrl: 'scripts/components/alert/success-template.html'
        });
    }

    function error(msg) {
        $.notify({
            message: msg
        }, _.extend(common, {
            type: 'danger'
        }));
    }

    function warning(msg) {
        $.notify({
            message: msg
        }, _.extend(common, {
            type: 'warning'
        }));
    }

    function info(msg) {
        $.notify({
            message: msg
        }, _.extend(common, {
            type: 'info'
        }));
    }
}
