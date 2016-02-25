'use strict';

angular.module('indigoeln')
    .factory('notificationInterceptor', function ($q, Alert) {
        var SUCCESS_ALERT = 'X-indigoeln-success-alert',
            ERROR_ALERT = 'X-indigoeln-error-alert',
            WARNING_ALERT = 'X-indigoeln-warning-alert',
            INFO_ALERT = 'X-indigoeln-info-alert',
            ALERT_PARAMS = 'X-indigoeln-params';

        return {
            response: function (response) {
                var alert;
                if ((alert = response.headers(SUCCESS_ALERT)) && angular.isString(alert)) {
                    Alert.success(alert, {param: response.headers(ALERT_PARAMS)});
                } else if ((alert = response.headers(ERROR_ALERT)) && angular.isString(alert)) {
                    Alert.error(alert, {param: response.headers(ALERT_PARAMS)});
                } else if ((alert = response.headers(WARNING_ALERT)) && angular.isString(alert)) {
                    Alert.warning(alert, {param: response.headers(ALERT_PARAMS)});
                } else if ((alert = response.headers(INFO_ALERT)) && angular.isString(alert)) {
                    Alert.info(alert, {param: response.headers(ALERT_PARAMS)});
                }
                return response;
            }
        };
    });
