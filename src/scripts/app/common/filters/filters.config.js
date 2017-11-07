var moment = require('moment-timezone');

/* @ngInject */
function filtersConfig($provide) {
    $provide.decorator('dateFilter', function($delegate) {
        var userTimeZone = moment.tz.guess();

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
