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
}

calculationHelper.getId = getId;

function getId() {
    return Math.floor((1 + Math.random()) * 0x1000000)
        .toString(16)
        .substring(1);
}

module.exports = calculationHelper;
