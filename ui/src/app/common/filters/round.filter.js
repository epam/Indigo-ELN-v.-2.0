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
        var numOfDigits = getNumberSignificantFigures(row.mol.value);

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
            sourceBatch = calculationService.findLimiting(stoichTableCache.getStoicTable().reactants) || row;
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
