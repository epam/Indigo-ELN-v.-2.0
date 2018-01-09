/* @ngInject */
function calculationHelper() {
    return {
        getClonedRows: getClonedRows,
        findChangedRow: findChangedRow
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
}

module.exports = calculationHelper;
