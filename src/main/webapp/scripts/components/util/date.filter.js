/**
 * Created by Stepan_Litvinov on 2/15/2016.
 */
angular.module('indigoeln').config(function ($provide) {
    $provide.decorator('dateFilter', function ($delegate) {
        return function () {
            if (arguments[1] == 'myTzAbbr') {
                return moment.tz(arguments[0],
                    jstz.determine().name() //moment.tz.guess()
                ).format('MMM DD, YYYY HH:mm:ss z');
            } else {
                return $delegate.apply(this, arguments);
            }
        };
    });
});