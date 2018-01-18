var fieldTypes = require('../../../../services/calculation/field-types');
var mathCalculation = require('../../../../services/calculation/math-calculation');
var DEFAULT_PURITY = 100;

/* @ngInject */
function productsCalculation(calculationHelper) {
    var rows;

    return {
        calculate: calculate
    };

    function calculate(productsData) {
        rows = calculationHelper.clone(productsData.rows);
        var changedRow = calculationHelper.findChangedRow(rows, productsData.idOfChangedRow);

        recalculate(changedRow, productsData.changedField, productsData.limitingRow);

        return rows;
    }

    function recalculate(row, fieldId, limitingRow) {
        switch (fieldId) {
            case fieldTypes.saltCode:
                onSaltChanged(row, limitingRow);
                break;
            case fieldTypes.saltEq:
                onSaltChanged(row, limitingRow);
                break;
            case fieldTypes.eq:
                row.eq.entered = true;
                onEqChanged(row, limitingRow);
                break;
            default:
                onLimitingMolChanged(limitingRow);
                break;
        }
    }

    function onSaltChanged(row, limitingRow) {
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

        updateRow(row, limitingRow);
    }

    function onEqChanged(row, limitingRow) {
        updateRow(row, limitingRow);
    }

    function onLimitingMolChanged(limitingRow) {
        _.forEach(rows, function(row) {
            updateRow(row, limitingRow);
        });
    }

    function updateRow(row, limitingRow) {
        var theoMoles = 0;
        var theoWeight = 0;
        var shouldRecalculateTheoValues = limitingRow && limitingRow.mol;

        if (shouldRecalculateTheoValues) {
            theoMoles = mathCalculation.computeNonLimitingMolByEq(
                limitingRow.mol.value, row.eq.value, limitingRow.eq.value
            );

            theoWeight = mathCalculation.computeWeight(theoMoles, row.molWeight.value, DEFAULT_PURITY);
        }

        row.setTheoMoles(theoMoles);
        row.setTheoWeight(theoWeight);
    }
}

module.exports = productsCalculation;
