var fieldTypes = require('../../directives/stoich-table/domain/field-types');
var mathCalculation = require('../../services/calculation/math-calculation');
var DEFAULT_PURITY = 100;

/* @ngInject */
function batchesCalculation(calculationHelper) {
    var rows;

    return {
        calculateAllRows: calculateAllRows,
        calculateRow: calculateRow
    };

    function calculateAllRows(batchesData) {
        rows = calculationHelper.clone(batchesData.rows);
        recalculateAllRows(batchesData.limitingRow);

        return rows;
    }

    function calculateRow(batchesData) {
        var changedRow = calculationHelper.clone(batchesData.changedRow);
        recalculateRow(changedRow, batchesData.changedField);

        return changedRow;
    }

    function recalculateAllRows(limitingRow) {
        _.forEach(rows, function(row) {
            calculationHelper.updateValuesDependingOnTheoMoles(row, limitingRow);
        });
    }

    function recalculateRow(row, fieldId) {
        switch (fieldId) {
            case fieldTypes.totalVolume:
                row.totalVolume.entered = true;
                onVolumeChanged(row);
                break;
            case fieldTypes.totalWeight:
                row.totalWeight.entered = true;
                onTotalWeightChanged(row);
                break;
            case fieldTypes.totalMoles:
                row.totalMoles.entered = true;
                onTotalMolesChanged(row);
                break;
            case fieldTypes.molWeight:
                onMolWeightChanged(row);
                break;
            case fieldTypes.saltCode:
                onSaltChanged(row);
                break;
            case fieldTypes.saltEq:
                row.saltEq.entered = true;
                onSaltChanged(row);
                break;
            default:
                break;
        }
    }

    function onVolumeChanged(row) {
        row.totalMoles.entered = false;
        row.totalMoles.value = 0;
        row.totalWeight.entered = false;
        row.totalWeight.value = 0;
        row.yield = 0;
    }

    function onTotalWeightChanged(row) {
        row.totalMoles.entered = false;
        row.totalMoles.value = 0;

        if (!row.molWeight.baseValue) {
            return;
        }

        row.totalMoles.value = mathCalculation.computeMol(row.totalWeight.value, row.molWeight.value, DEFAULT_PURITY);

        row.yield = row.theoMoles.value
            ? mathCalculation.computeYield(row.totalMoles.value, row.theoMoles.value)
            : 0;
    }

    function onTotalMolesChanged(row) {
        row.totalWeight.entered = false;
        row.totalWeight.value = 0;

        if (!row.molWeight.baseValue) {
            return;
        }

        row.totalWeight.value = mathCalculation.computeWeight(
            row.totalMoles.value, row.molWeight.value, DEFAULT_PURITY
        );

        row.yield = row.theoMoles.value
            ? mathCalculation.computeYield(row.totalMoles.value, row.theoMoles.value)
            : 0;
    }

    function onMolWeightChanged(row) {
        var theoWeight = row.theoMoles.value ?
            mathCalculation.computeWeight(row.theoMoles.value, row.molWeight.value, DEFAULT_PURITY)
            : 0;

        row.setTheoWeight(theoWeight);

        if (row.totalMoles.entered) {
            onTotalMolesChanged(row);
        } else if (row.totalWeight.entered) {
            onTotalWeightChanged(row);
        }
    }

    function onSaltChanged(row) {
        if (!row.molWeight.baseValue) {
            return;
        }

        if (row.saltCode.weight === 0) {
            row.saltEq.value = 0;
        }

        var molWeight = mathCalculation.computeMolWeightBySalt(
            row.molWeight.baseValue, row.saltCode.weight, row.saltEq.value
        );

        var formula = row.formula.baseValue
            ? calculationHelper.getFormula(row)
            : null;

        row.setMolWeight(molWeight);
        row.setFormula(formula);

        onMolWeightChanged(row);
    }
}

module.exports = batchesCalculation;
