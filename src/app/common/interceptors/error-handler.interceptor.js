/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/* @ngInject */
function errorHandlerInterceptor($q, $injector, $rootScope, $log) {
    return {
        responseError: responseError
    };

    function responseError(httpResponse) {
        var addErrorAlert = function() {
            $log.error(angular.toJson(arguments));
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
                if (_.isString(errorAlertHeader)) {
                    $log.error(errorAlertHeader);
                } else if (errorHeader) {
                    addErrorAlert(errorHeader, {
                        entityName: entityKey
                    });
                } else if (httpResponse.data && httpResponse.data.fieldErrors) {
                    _.forEach(httpResponse.data.fieldErrors, function(error) {
                        var convertedField = error.field.replace(/\[\d*\]/g, '[]');
                        var fieldName = convertedField.charAt(0).toUpperCase() + convertedField.slice(1);
                        addErrorAlert('Field ' + fieldName + ' cannot be empty', 'error.' + error.message, {
                            fieldName: fieldName
                        });
                    });
                } else if (httpResponse.data && httpResponse.data.message) {
                    $log.error(httpResponse.data.message);
                } else {
                    addErrorAlert(httpResponse.data);
                }
                break;

            case 401:
                if (httpResponse.config.url !== 'login') {
                    var authService = $injector.get('authService');
                    var $state = $injector.get('$state');
                    var params = $rootScope.toStateParams;
                    authService.logout();
                    $rootScope.previousStateName = $rootScope.toState;
                    $rootScope.previousStateNameParams = params;
                    $state.go('login');
                }
                break;

            default:
                if (httpResponse.data && httpResponse.data.message) {
                    addErrorAlert(httpResponse.data.message);
                } else {
                    addErrorAlert(angular.toJson(httpResponse));
                }
        }

        return $q.reject(httpResponse);
    }
}

module.exports = errorHandlerInterceptor;
