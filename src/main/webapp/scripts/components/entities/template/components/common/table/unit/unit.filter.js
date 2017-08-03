/**
 * Created by Stepan_Litvinov on 6/29/2016.
 */
angular.module('indigoeln')
    .filter('unit', function(UnitsConverter) {
        return function(value, to) {
            if (!value) {
                return value;
            }

            return +UnitsConverter.convert(value).as(to).val();
        };
    });
