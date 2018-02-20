var mathCalculation = require('./math-calculation');
var DEFAULT_PURITY = 100;

/* @ngInject */
function calculationHelper($http, $log, $q) {
    return {
        clone: clone,
        findChangedRow: findChangedRow,
        getFormula: getFormula,
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

function getId() {
    return Math.floor((1 + Math.random()) * 0x1000000)
        .toString(16)
        .substring(1);
}

module.exports = calculationHelper;
