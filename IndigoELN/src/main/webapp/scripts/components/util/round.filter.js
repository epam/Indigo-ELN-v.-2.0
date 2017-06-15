angular.module('indigoeln')
    .filter('round', function (StoichTableCache, CalculationService) {
        var DEFAULT_SIG_DIGITS = 3;

        function getNumberSignificantFigures(digits) {
            var sigFigNumber = getSigFigsFromNumberString(digits.toString());
            return sigFigNumber.length;
        }

        function getSigFigsFromNumberString(val) {
            var stringVal = val.toString();
            var result = '';
            var indexOfDecimal = stringVal.indexOf('.');
            // March through string to find nonzero and sig zero characters.
            var finished = false;
            for (var i = 0; !finished && i < stringVal.length; i++) {
                if (i !== indexOfDecimal) {
                    var number = parseInt(stringVal.charAt(i));
                    // handle numbers like 123: item 1
                    if (number > 0) {
                        result = result + number;
                    } else if (result.length > 0 && number === 0) {
                        // handle numbers like 1100
                        result = result + number;
                    } else if (indexOfDecimal >= 0 && i > indexOfDecimal) {
                        // handle numbers with values after decimal point
                        if (result.length > 0 && number === 0) {
                            result = result + number;
                        }
                    }
                }
            }
            return result;
        }

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

        return function (value, sigDigits, column, row) {
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