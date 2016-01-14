'use strict';

angular.module('indigoeln')
    .factory('notificationInterceptor', function ($q, AlertService) {
        return {
            response: function (response) {
                var alertKey = response.headers('X-indigoeln-alert');
                if (angular.isString(alertKey)) {
                    AlertService.success(alertKey, {param: response.headers('X-indigoeln-params')});
                }
                return response;
            }
        };
    });
