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

var mathCalculation = require('./math-calculation');
var DEFAULT_PURITY = 100;

function getBaseFormula(formula) {
    if (!formula) {
        return formula;
    }
    var extendedFormulaIndex = formula.indexOf('*');
    if (extendedFormulaIndex !== -1) {
        return formula.substring(0, extendedFormulaIndex).trim();
    }

    return formula;
}

/* @ngInject */
function calculationHelper($http, $log, $q) {
    return {
        getId: getId,
        clone: clone,
        findChangedRow: findChangedRow,
        getFormula: getFormula,
        getBaseFormula: getBaseFormula,
        findLimitingRow: findLimitingRow,
        updateViewRows: updateViewRows,
        updateViewRow: updateViewRow,
        recalculateSalt: recalculateSalt,
        calculateValuesDependingOnTheoMoles: calculateValuesDependingOnTheoMoles
    };

    function clone(rows) {
        return _.cloneDeep(rows);
    }

    function findChangedRow(rows, id) {
        return _.find(rows, {id: id});
    }

    function getFormula(row) {
        var formula = row.formula.baseValue;
        var saltEq = row.saltEq.value;
        var saltCode = row.saltCode.name.split('-')[1];

        if (!saltEq) {
            return formula;
        }

        return formula + '*' + saltEq + '(' + saltCode.trim() + ')';
    }

    function updateViewRows(calculatedRows, viewRows) {
        _.forEach(viewRows, function(viewRow, index) {
            var calcRow = calculatedRows[index];
            updateViewRow(calcRow, viewRow);
        });
    }

    function updateViewRow(calculatedRow, viewRow) {
        var calcRowFields = _.keys(calculatedRow);

        _.forEach(calcRowFields, function(field) {
            if (_.isObject(viewRow[field])) {
                _.assign(viewRow[field], calculatedRow[field]);
            } else {
                viewRow[field] = calculatedRow[field];
            }
        });
    }

    function findLimitingRow(reagents) {
        return _.find(reagents, ['limiting.value', true]);
    }

    function recalculateSalt(batchRow) {
        if (batchRow.structure && batchRow.structure.molfile) {
            var config = getSaltConfig(batchRow);

            return $http.put(apiUrl + 'calculations/molecule/info', batchRow.structure.molfile, config)
                .then(function(result) {
                    var data = result.data;
                    batchRow.molWeight.value = data.molecularWeight;
                    batchRow.molWeight.baseValue = data.molecularWeight;
                    batchRow.formula.baseValue = data.molecularFormula;
                    batchRow.formula.value = getFormula(batchRow);

                    return batchRow;
                });
        }

        return $q.resolve(batchRow);
    }

    function getSaltConfig(batchRow) {
        var saltCode = batchRow.saltCode ? batchRow.saltCode.value : null;
        var saltEq = batchRow.saltEq ? batchRow.saltEq.value : null;

        return {
            params: {
                saltCode: saltCode && saltCode !== '0' ? saltCode : null,
                saltEq: saltEq
            }
        };
    }

    function calculateValuesDependingOnTheoMoles(row, limitingMol) {
        var theoMoles = limitingMol || 0;
        var theoWeight = theoMoles
            ? mathCalculation.computeWeight(theoMoles, row.molWeight.value, DEFAULT_PURITY)
            : 0;

        row.setTheoMoles(theoMoles);
        row.setTheoWeight(theoWeight);

        if (row.totalMoles.entered) {
            row.totalWeight.value = mathCalculation.computeWeight(
                row.totalMoles.value, row.molWeight.value, DEFAULT_PURITY
            );
        } else if (row.totalWeight.entered) {
            row.totalMoles.value = mathCalculation.computeMol(
                row.totalWeight.value, row.molWeight.value, DEFAULT_PURITY
            );
        }

        row.yield = row.theoMoles.value
            ? mathCalculation.computeYield(row.totalMoles.value, row.theoMoles.value)
            : 0;
    }
}

calculationHelper.getId = getId;
calculationHelper.getBaseFormula = getBaseFormula;

function getId() {
    return Math.floor((1 + Math.random()) * 0x1000000)
        .toString(16)
        .substring(1);
}

module.exports = calculationHelper;
