/* @ngInject */
function notificationInterceptor($injector) {
    var SUCCESS_ALERT = 'X-indigoeln-success-alert';
    var ERROR_ALERT = 'X-indigoeln-error-alert';
    var WARNING_ALERT = 'X-indigoeln-warning-alert';
    var INFO_ALERT = 'X-indigoeln-info-alert';
    var ALERT_PARAMS = 'X-indigoeln-params';

    return {
        response: response
    };

    function response(httpResponse) {
        var notifyService = $injector.get('notifyService');
        if (_.isString(httpResponse.headers(SUCCESS_ALERT))) {
            notifyService.success(httpResponse.headers(SUCCESS_ALERT), {
                param: httpResponse.headers(ALERT_PARAMS)
            });
        } else if (_.isString(httpResponse.headers(ERROR_ALERT))) {
            notifyService.error(httpResponse.headers(ERROR_ALERT), {
                param: httpResponse.headers(ALERT_PARAMS)
            });
        } else if (_.isString(httpResponse.headers(WARNING_ALERT))) {
            notifyService.warning(httpResponse.headers(WARNING_ALERT), {
                param: httpResponse.headers(ALERT_PARAMS)
            });
        } else if (_.isString(httpResponse.headers(INFO_ALERT))) {
            notifyService.info(httpResponse.headers(INFO_ALERT), {
                param: httpResponse.headers(ALERT_PARAMS)
            });
        }

        return httpResponse;
    }
}

module.exports = notificationInterceptor;
