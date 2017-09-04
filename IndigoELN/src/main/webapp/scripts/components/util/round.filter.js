angular.module('indigoeln')
    .filter('round', function(StoichTableCache, CalculationService) {
        var DEFAULT_SIG_DIGITS = 3;

        return function(value, sigDigits, column, row) {
            var significantDigits = sigDigits;
            if (column && row) {
                significantDigits = getSigDigitsForMol(column, row);
            }

            return +toStringWithSignificantDigits(+value, significantDigits || DEFAULT_SIG_DIGITS);
        };

        /**
         * This method gets number, desired accuracy of the number and converts a number with a given accuracy
         * @param value
         * @param sigDigits
         * @returns {*}
         */

        function toStringWithSignificantDigits(value, sigDigits) {
            var delta = 0;
            if (isFractionalNumber(value) && !isNumberStartsWithZero(value)) {
                delta = (value + '').indexOf('.');
            }

            sigDigits += delta;
            if (value === 0) {
                return value.toFixed(sigDigits - 1);
            }
            // makes little sense for 0
            var numDigits = Math.ceil(Math.log10(Math.abs(value)));
            var rounded = Math.round(value * Math.pow(10, sigDigits - numDigits)) * Math.pow(10, numDigits - sigDigits);
            var fixNumber = fixNumberToGivenAccuracy(sigDigits, numDigits);

            return rounded.toFixed(Math.max(fixNumber ? sigDigits : sigDigits - numDigits, 0));
        }

        function getSigDigitsForMol(column, row) {
            var sigDigitsForMol = DEFAULT_SIG_DIGITS;
            var sourceBatch = row;
            // round for mol depends on entered weight/volume precision
            if (column.id === 'mol' && column.isIntended) {
                sourceBatch = CalculationService.findLimiting(StoichTableCache.getStoicTable());
            }

            if (column.id !== 'molWeight') {
                var weightOrVolume = angular.copy(sourceBatch.weight || sourceBatch.volume);
                if (weightOrVolume && weightOrVolume.value && weightOrVolume.entered) {
                    sigDigitsForMol = getNumberSignificantFigures(weightOrVolume.value);
                }
            }

            return sigDigitsForMol;
        }

        /**
         * This method gets value of Weight or Volume and returns length of a number
         * @param digits
         * @returns {Number}
         */
        function getNumberSignificantFigures(digits) {
            var sigFigNumber = _.toString(parseInt(digits.toString().replace(/[^\d]/g, ''), 10));

            return sigFigNumber.length;
        }

        function isFractionalNumber(value) {
            return (value + '').indexOf('.') >= 0;
        }

        function isNumberStartsWithZero(value) {
            return (value + '').startsWith('0.');
        }

        function fixNumberToGivenAccuracy(sigDigits, numDigits) {
            return sigDigits + numDigits > 0;
        }
    });
