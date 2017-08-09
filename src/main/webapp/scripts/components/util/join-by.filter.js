angular.module('indigoeln')
    .filter('joinBy', function() {
        return function (targetArray, separator, key) {
            if (!separator) {
                separator = ", ";
            }

            return _.chain(targetArray)
                .map(function(element) {
                    return _.isObject(element) ? element[key] : element;
                })
                .join(separator)
                .value();
        };
    });