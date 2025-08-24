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

var moment = require('moment-timezone');

/* @ngInject */
function filtersConfig($provide) {
    $provide.decorator('dateFilter', function($delegate) {
        var userTimeZone = null;
        try {
            userTimeZone = moment.tz.guess();
        } catch (e) {
            try {
                userTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
            } catch (e2) {
                console.warn('Cannot determine user time zone');
            }
        }
        console.log('Using timezone: ' + userTimeZone);

        return function(date, dateFormat) {
            var format = 'MMM DD, YYYY HH:mm:ss z';

            if (dateFormat === format) {
                return moment.tz(date, userTimeZone).format(format);
            }

            return $delegate.apply(this, arguments);
        };
    });
}

module.exports = filtersConfig;
