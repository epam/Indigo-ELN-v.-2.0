angular.module('indigoeln')
    .filter('round', function(StoichTableCache, CalculationService) {
        var DEFAULT_SIG_DIGITS = 3;

        function getNumberSignificantFigures(digits) {
            var sigFigNumber = _.toString(parseInt(digits.toString().replace(/[^\d]/g, ''), 10));

            return sigFigNumber.length;
        }

        function toStringWithSignificantDigits(value, sigDigits) {
            var delta = 0;
            if (!(value + '').startsWith('0.')) {
                delta = (value + '').indexOf('.');
            }
            sigDigits += delta;
            if (value === 0) {
                return value.toFixed(sigDigits - 1);
            }
            // makes little sense for 0
            var numDigits = Math.ceil(Math.log10(Math.abs(value)));
            var rounded = Math.round(value * Math.pow(10, sigDigits - numDigits)) * Math.pow(10, numDigits - sigDigits);

            return rounded.toFixed(Math.max(sigDigits - numDigits, 0));
        }

        function getSigDigitsForMol(column, row) {
            var sigDigitsForMol = DEFAULT_SIG_DIGITS;
            var sourceBatch = row;
            // round for mol depends on entered weight/volume precision
            if (column.id === 'mol' && column.isIntended) {
                sourceBatch = CalculationService.findLimiting(StoichTableCache.getStoicTable());
            }
            var weightOrVolume = angular.copy(sourceBatch.weight || sourceBatch.volume);
            if (weightOrVolume && weightOrVolume.value && weightOrVolume.entered) {
                sigDigitsForMol = getNumberSignificantFigures(weightOrVolume.value);
            }

            return sigDigitsForMol;
        }

        return function(value, sigDigits, column, row) {
            var isEntered = false;
            var significantDigits = sigDigits;
            if (column && row) {
                isEntered = row[column.id].entered;
                significantDigits = getSigDigitsForMol(column, row);
            }
            if (isEntered || !value) {
                return value;
            }

            return +toStringWithSignificantDigits(+value, significantDigits || DEFAULT_SIG_DIGITS);
        };
    });
