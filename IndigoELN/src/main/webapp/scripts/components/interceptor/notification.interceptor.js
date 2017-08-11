angular
    .module('indigoeln')
    .factory('notificationInterceptor', notificationInterceptor);

/* @ngInject */
function notificationInterceptor($injector) {
    var SUCCESS_ALERT = 'X-indigoeln-success-alert',
        ERROR_ALERT = 'X-indigoeln-error-alert',
        WARNING_ALERT = 'X-indigoeln-warning-alert',
        INFO_ALERT = 'X-indigoeln-info-alert',
        ALERT_PARAMS = 'X-indigoeln-params';

    return {
        response: response
    };

    function response(response) {
        var notifyService = $injector.get('notifyService');
        if (_.isString(response.headers(SUCCESS_ALERT))) {
            notifyService.success(response.headers(SUCCESS_ALERT), {
                param: response.headers(ALERT_PARAMS)
            });
        } else if (_.isString(response.headers(ERROR_ALERT))) {
            notifyService.error(response.headers(ERROR_ALERT), {
                param: response.headers(ALERT_PARAMS)
            });
        } else if (_.isString(response.headers(WARNING_ALERT))) {
            notifyService.warning(response.headers(WARNING_ALERT), {
                param: response.headers(ALERT_PARAMS)
            });
        } else if (_.isString(response.headers(INFO_ALERT))) {
            notifyService.info(response.headers(INFO_ALERT), {
                param: response.headers(ALERT_PARAMS)
            });
        }

        return response;
    }
}
