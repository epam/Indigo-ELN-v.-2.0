angular.module('indigoeln')
    .factory('notificationInterceptor', function ($q, Alert) {
        var SUCCESS_ALERT = 'X-indigoeln-success-alert',
            ERROR_ALERT = 'X-indigoeln-error-alert',
            WARNING_ALERT = 'X-indigoeln-warning-alert',
            INFO_ALERT = 'X-indigoeln-info-alert',
            ALERT_PARAMS = 'X-indigoeln-params';

        return {
            response: function (response) {
                if (angular.isString(response.headers(SUCCESS_ALERT))) {
                    Alert.success(response.headers(SUCCESS_ALERT), {param: response.headers(ALERT_PARAMS)});
                } else if (angular.isString(response.headers(ERROR_ALERT))) {
                    Alert.error(response.headers(ERROR_ALERT), {param: response.headers(ALERT_PARAMS)});
                } else if (angular.isString(response.headers(WARNING_ALERT))) {
                    Alert.warning(response.headers(WARNING_ALERT), {param: response.headers(ALERT_PARAMS)});
                } else if (angular.isString(response.headers(INFO_ALERT))) {
                    Alert.info(response.headers(INFO_ALERT), {param: response.headers(ALERT_PARAMS)});
                }
                return response;
            }
        };
    });
