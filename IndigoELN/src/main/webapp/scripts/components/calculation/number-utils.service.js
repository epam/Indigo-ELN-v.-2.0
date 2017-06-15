angular
    .module('indigoeln')
    .factory('NumberUtil', NumberUtil);

/* @ngInject */
function NumberUtil() {
    return {
        floatEquals: floatEquals
    };

    function floatEquals(n1, n2, decimals) {
        var equal = false;
        if (decimals === undefined) {
            decimals = 15;
        }
        if (decimals) {
            var factor = Math.pow(10, decimals);
            var a = Math.round(n1 * factor) / factor, b = Math.round(n2 * factor) / factor;
            equal = a === b;
        } else {
            equal = n1 === n2;
        }
        return equal;
    }
}

