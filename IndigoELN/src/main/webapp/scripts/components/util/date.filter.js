/**
 * Created by Stepan_Litvinov on 2/15/2016.
 */
angular.module('indigoeln').config(function ($provide) {
    $provide.decorator('dateFilter', function ($delegate) {
        var tzName;
        return function () {
            var format = 'MMM DD, YYYY HH:mm:ss z';
            if (arguments[1] == format) {
                if (!tzName) {
                    tzName = jstz.determine().name();
                }
                return moment.tz(arguments[0],
                    tzName //moment.tz.guess()
                ).format(format);
            } else {
                return $delegate.apply(this, arguments);
            }
        };
    });
});