/* @ngInject */
function calculationHelper() {
    return {
        getClonedRows: getClonedRows,
        findChangedRow: findChangedRow,
        getFormula: getFormula
    };

    function getClonedRows(rows) {
        return _.cloneDeep(rows);
    }

    // TODO: remove it use id for each row
    function findChangedRow(rows, changedRow) {
        return _.find(rows, function(row) {
            return _.isEqual(row, changedRow);
        });
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
}

module.exports = calculationHelper;
