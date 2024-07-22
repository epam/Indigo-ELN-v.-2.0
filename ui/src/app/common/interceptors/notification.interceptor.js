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
