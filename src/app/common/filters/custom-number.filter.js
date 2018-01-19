function customNumber() {
    var TEN = 10;
    var MAX_VALUE = 1e300;
    var MIN_VALUE = 1e-300;
    // max symbols for decimal representation
    var MAX_SYMBOLS = 6;
    // max symbols for exponential representation
    var MAX_E_SYMBOLS = 0;

    // -2 symbols for digit and dot, -2 symbols for sign and 'e', -1 symbol for power
    var MAX_E_DECIMALS = 0;

    return function(value, emptyValue, maxSymbols) {
        // max symbols for decimal representation
        MAX_SYMBOLS = maxSymbols || 6;
        // max symbols for exponential representation
        MAX_E_SYMBOLS = MAX_SYMBOLS + 1;

        // -2 symbols for digit and dot, -2 symbols for sign and 'e', -1 symbol for power
        MAX_E_DECIMALS = MAX_E_SYMBOLS - 2 - 2 - 1;
        if (MAX_E_DECIMALS < 1) {
            MAX_E_DECIMALS = 1;
        }

        var sign = value < 0 ? '-' : '';
        var absValue = Math.abs(value);

        if (!value || absValue < MIN_VALUE) {
            if (angular.isDefined(emptyValue)) {
                return angular.isNumber(value) ? '0' : emptyValue;
            }
            if (value === 0) {
                return '0';
            }

            return '';
        }
        if (value > MAX_VALUE) {
            return 'Too Long';
        }

        return sign + numberToString(absValue);
    };

    /**
     * @param {Number} value number
     * @returns {String} string
     */
    function numberToString(value) {
        var pow = Math.log(value) / Math.log(TEN);

        if (pow < MAX_SYMBOLS && pow >= (2 - MAX_SYMBOLS)) {
            pow = (pow < 0) ? 0 : pow;
            var tenPow = Math.pow(TEN, MAX_SYMBOLS - Math.floor(pow) - 2);

            // normal case
            return tenPow > 1 ? (Math.round(value * tenPow) / tenPow).toString() : Math.round(value).toString();
        }
        if (pow >= MAX_SYMBOLS + 1) {
            // exp positive
            return value.toExponential(pow >= TEN ? MAX_E_DECIMALS - 1 : MAX_E_DECIMALS);
        }

        // exp negative
        return value.toExponential(pow <= -TEN + 1 ? MAX_E_DECIMALS - 1 : MAX_E_DECIMALS);
    }
}

module.exports = customNumber;
