/* @ngInject */
function calculationHelper($http) {
    return {
        getClonedRows: getClonedRows,
        findChangedRow: findChangedRow,
        getFormula: getFormula,
        findLimitingRow: findLimitingRow,
        getMolFromLimitingRow: getMolFromLimitingRow,
        updateViewRows: updateViewRows,
        recalculateSalt: recalculateSalt
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

    function updateViewRows(calculatedRows, viewRows) {
        _.forEach(viewRows, function(viewRow, index) {
            var calcRow = calculatedRows[index];
            var calcRowFields = _.keys(calcRow);

            _.forEach(calcRowFields, function(field) {
                _.assign(viewRow[field], calcRow[field]);
            });
        });
    }

    function findLimitingRow(reagents) {
        return _.find(reagents, ['limiting.value', true]);
    }

    function getMolFromLimitingRow(reagents) {
        var limitingRow = findLimitingRow(reagents);

        return limitingRow && limitingRow.mol.value
            ? limitingRow.mol.value
            : 0;
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
                    // for product batch summary
                    // batchRow.lastUpdatedType = 'weight';

                    return batchRow;
                });
        }
        $log.error('Batch does not contain structure or molfile');

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
}

calculationHelper.getId = getId;

function getId() {
    return Math.floor((1 + Math.random()) * 0x1000000)
        .toString(16)
        .substring(1);
}

module.exports = calculationHelper;
