/**
 * Created by Stepan_Litvinov on 6/29/2016.
 */
angular.module('indigoeln.componentsModule')
    .filter('unit', function(unitsConverter) {
        return function(value, to) {
            if (!value) {
                return value;
            }

            return +unitsConverter.convert(value).as(to).val();
        };
    });
