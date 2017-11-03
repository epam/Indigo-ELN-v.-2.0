angular
    .module('indigoeln')
    .filter('capitalize', function() {
        return function(input) {
            if (input) {
                var lowerInput = input.toLowerCase();

                return lowerInput.substring(0, 1).toUpperCase() + lowerInput.substring(1);
            }

            return input;
        };
    });
