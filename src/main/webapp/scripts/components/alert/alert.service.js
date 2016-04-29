angular.module('indigoeln')
    .factory('Alert', function () {
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
                from: 'bottom',
                align: 'right'
            }
        };
        return {
            success: function (msg) {
                $.notify({
                    message: msg
                }, _.extend(common, {type: 'success'}));
            },
            error: function (msg) {
                $.notify({
                    message: msg
                }, _.extend(common, {type: 'danger'}));

            },
            warning: function (msg) {
                $.notify({
                    message: msg
                }, _.extend(common, {type: 'warning'}));

            },

            info: function (msg) {
                $.notify({
                    message: msg
                }, _.extend(common, {type: 'info'}));
            }
        };

    });
