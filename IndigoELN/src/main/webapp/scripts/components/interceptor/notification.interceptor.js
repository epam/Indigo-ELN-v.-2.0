'use strict';

angular.module('indigoeln')
    .factory('notificationInterceptor', function ($q, AlertService) {
        var SUCCESS_ALERT = 'X-indigoeln-success-alert',
            ERROR_ALERT = 'X-indigoeln-error-alert',
            WARNING_ALERT = 'X-indigoeln-warning-alert',
            INFO_ALERT = 'X-indigoeln-info-alert',
            ALERT_PARAMS = 'X-indigoeln-params';

        return {
            response: function (response) {
                var alert;
                if ((alert = response.headers(SUCCESS_ALERT)) && angular.isString(alert)) {
                    AlertService.success(alert, {param: response.headers(ALERT_PARAMS)});
                } else if ((alert = response.headers(ERROR_ALERT)) && angular.isString(alert)) {
                    AlertService.error(alert, {param: response.headers(ALERT_PARAMS)});
                } else if ((alert = response.headers(WARNING_ALERT)) && angular.isString(alert)) {
                    AlertService.warning(alert, {param: response.headers(ALERT_PARAMS)});
                } else if ((alert = response.headers(INFO_ALERT)) && angular.isString(alert)) {
                    AlertService.info(alert, {param: response.headers(ALERT_PARAMS)});
                }
                return response;
            }
        };
    });
