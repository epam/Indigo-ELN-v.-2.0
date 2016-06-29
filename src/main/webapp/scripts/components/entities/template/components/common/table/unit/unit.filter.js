/**
 * Created by Stepan_Litvinov on 6/29/2016.
 */
angular.module('indigoeln')
    .filter('unit', function () {
        return function (value, to) {
            if (!value || !to) {
                return;
            }
            return $u(value).as(to).val();
        };
    });