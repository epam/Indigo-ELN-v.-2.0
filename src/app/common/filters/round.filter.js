/* @ngInject */
function round(stoichTableCache, calculationService, unitsConverter) {
    var DEFAULT_PRECISION = 3;
    var MAX_PRECISION = 10;

    return function(value, sigDigits, column, row, targetUnit) {
        if (!value) {
            return value;
        }

        var precision = sigDigits || DEFAULT_PRECISION;

        if (column && row) {
            if (isMolColumn(column)) {
                precision = getCorrectPrecisionForMol(value, column, row, targetUnit);
            }

            if (isWeightColumn(column)) {
                precision = getCorrectPrecisionForWeight(row);
            }
        }

        return fixedNumber(value, precision);
    };

    function getCorrectPrecisionForWeight(row) {
        var numOfDigits = getNumberSignificantFigures(row['mol'].value);
        return numOfDigits > MAX_PRECISION ? DEFAULT_PRECISION : numOfDigits;
    }

    function getCorrectPrecisionForMol(value, column, row, targetUnit) {
        var numOfDigits = getPrecisionForMol(column, row);
        var precision = numOfDigits > MAX_PRECISION ? DEFAULT_PRECISION : numOfDigits;

        if (isUnitChanged(targetUnit)) {
            var numDigitsBeforeDot = String(value).indexOf('.');
            var correctPrecision = numDigitsBeforeDot > precision ? precision : precision - numDigitsBeforeDot;
            precision = isNumberStartsWithZero(value) ? precision : correctPrecision;
        }

        return precision;
    }

    function fixedNumber(value, precision) {
        var roundNumber = +Number(value).toFixed(precision);

        while (roundNumber === 0) {
            precision++;
            roundNumber = +Number(value).toFixed(precision);
        }

        return roundNumber;
    }

    function isUnitChanged(targetUnit) {
        if (targetUnit) {
            var baseUnit = getBaseUnit(targetUnit);
            return baseUnit && baseUnit !== targetUnit;
        }
        return false;
    }

    function getBaseUnit(targetUnit) {
        return _.get(unitsConverter.table[targetUnit], 'indigoBase');
    }

    function isMolColumn(column) {
        return column.id === 'mol';
    }

    function isWeightColumn(column) {
        return column.name === 'Weight';
    }

    function getPrecisionForMol(column, row) {
        var sigDigitsForMol = DEFAULT_PRECISION;
        var sourceBatch = row;
        // round for mol depends on entered weight/volume precision
        if (column.id === 'mol' && column.isIntended) {
            sourceBatch = calculationService.findLimiting(stoichTableCache.getStoicTable());
        }

        var weightOrVolume = angular.copy(sourceBatch.weight || sourceBatch.volume);
        if (weightOrVolume && weightOrVolume.value) {
            sigDigitsForMol = getNumberSignificantFigures(weightOrVolume.value);
        }

        return sigDigitsForMol;
    }

    function getNumberSignificantFigures(digits) {
        var sigFigNumber = _.toString(digits.toString().replace(/[^\d]/g, ''));

        return sigFigNumber.length;
    }

    function isNumberStartsWithZero(value) {
        return (value + '').startsWith('0.');
    }
}

module.exports = round;
