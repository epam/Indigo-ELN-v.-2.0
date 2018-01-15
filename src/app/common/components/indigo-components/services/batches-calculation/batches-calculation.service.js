var fieldTypes = require('../../directives/stoich-table/domain/field-types');
var mathCalculation = require('../../services/calculation/math-calculation');

/* @ngInject */
function batchesCalculation(calculationHelper) {
    var rows;

    return {
        calculate: calculate
    };

    function calculate(batchesData) {
        rows = calculationHelper.getClonedRows(batchesData.rows);
        var changedRow = calculationHelper.findChangedRow(rows, batchesData.idOfChangedRow);

        recalculate(changedRow, batchesData.changedField);

        return rows;
    }

    function recalculate(row, fieldId) {
        switch (fieldId) {
            case fieldTypes.saltCode:
                onSaltChanged(row);
                break;
            case fieldTypes.saltEq:
                onSaltChanged(row);
                break;
            default:
                break;
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
    }
}

module.exports = batchesCalculation;
