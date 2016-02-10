'use strict';

angular.module('indigoeln')
    .factory('errorHandlerInterceptor', function ($q, $log, $injector, AlertService) {
        return {
            'responseError': function (httpResponse) {
                var i;
                var addErrorAlert = function () {
                    AlertService.error(JSON.stringify(arguments));
                };
                switch (httpResponse.status) {
                    // connection refused, server not reachable
                    case 0:
                        addErrorAlert('Server not reachable', 'error.server.not.reachable');
                        break;

                    case 400:
                        var errorAlertHeader = httpResponse.headers('X-indigoeln-error-alert');
                        var errorHeader = httpResponse.headers('X-indigoeln-error');
                        var entityKey = httpResponse.headers('X-indigoeln-params');
                        if (angular.isString(errorAlertHeader)) {
                            AlertService.error(errorAlertHeader);
                            return $q.resolve(httpResponse);
                        } else if (errorHeader) {
                            addErrorAlert(errorHeader, {entityName: entityKey});
                        } else if (httpResponse.data && httpResponse.data.fieldErrors) {
                            for (i = 0; i < httpResponse.data.fieldErrors.length; i++) {
                                var fieldError = httpResponse.data.fieldErrors[i];
                                // convert 'something[14].other[4].id' to 'something[].other[].id' so translations can be written to it
                                var convertedField = fieldError.field.replace(/\[\d*]/g, '[]');
                                var fieldName = convertedField.charAt(0).toUpperCase() + convertedField.slice(1);
                                addErrorAlert('Field ' + fieldName + ' cannot be empty', 'error.' + fieldError.message, {fieldName: fieldName});
                            }
                        } else if (httpResponse.data && httpResponse.data.message) {
                            addErrorAlert(httpResponse.data.message, httpResponse.data.message, httpResponse.data);
                        } else {
                            addErrorAlert(httpResponse.data);
                        }
                        break;

                    case 401:
                        if (httpResponse.config.url !== 'login') {
                            var $location = $injector.get('$location');
                            $location.path('/login');
                        }
                        break;

                    default:
                        if (httpResponse.data && httpResponse.data.message) {
                            addErrorAlert(httpResponse.data.message);
                        } else {
                            addErrorAlert(JSON.stringify(httpResponse));
                        }
                }
                return $q.reject(httpResponse);
            }
        };
    });