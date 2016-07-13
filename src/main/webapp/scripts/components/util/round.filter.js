angular.module('indigoeln')
    .filter('round', function () {
        function toStringWithSignificantDigits(value, sigDigits) {
            var delta = 0;
            if (!(value + '').startsWith('0.')) {
                delta = (value + '').indexOf('.');
            }
            sigDigits = sigDigits + delta;
            if (value === 0) {
                return value.toFixed(sigDigits - 1);
            }// makes little sense for 0
            var numDigits = Math.ceil(Math.log10(Math.abs(value)));
            var rounded = Math.round(value * Math.pow(10, sigDigits - numDigits)) * Math.pow(10, numDigits - sigDigits);
            return rounded.toFixed(Math.max(sigDigits - numDigits, 0));
        }

        return function (value, sigDigits, isEntered) {
            if (isEntered || !value) {
                return value;
            }
            return +toStringWithSignificantDigits(+value, sigDigits || 3);
        };
    });