/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
