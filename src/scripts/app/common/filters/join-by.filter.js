angular
    .module('indigoeln')
    .filter('joinBy', function() {
        return function(targetArray, separator, key) {
            return _.chain(targetArray)
                .map(function(element) {
                    return _.isObject(element) ? element[key] : element;
                })
                .join(getSeparator(separator))
                .value();
        };

        function getSeparator(separator) {
            return separator || ', ';
        }
    });
