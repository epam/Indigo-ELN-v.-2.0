var ReagentRow = require('../domain/reagent/calculation-row/reagent-row');

/* @ngInject */
function stoichTableHelper(reagentsCalculation) {
    return {
        onReagentsChanged: onReagentsChanged,
        getPrecursors: getPrecursors,
        getReactantsWithMolfile: getReactantsWithMolfile,
        isReactantWithMolfile: isReactantWithMolfile
    };

    function onReagentsChanged(change) {
        var preparedReagentsData = prepareReagentsForCalculation(change);

        var calculatedRows = reagentsCalculation.calculate(preparedReagentsData);
        updateReagents(calculatedRows, change.rows);
    }

    function getPrecursors(rows) {
        var reactants = getReactantsWithMolfile(rows);

        return _.compact(_.map(reactants, function(r) {
            return r.compoundId || r.fullNbkBatch;
        })).join(', ');
    }

    function isReactantWithMolfile(item) {
        return item.structure && item.structure.molfile && isReactant(item);
    }

    function getReactantsWithMolfile(stoichReactants) {
        return _.filter(stoichReactants, function(item) {
            return isReactantWithMolfile(item);
        });
    }

    function isReactant(item) {
        return item.rxnRole.name === 'REACTANT';
    }

    function convertReagentsRows(viewRows) {
        return _.map(viewRows, convertReagentRow);
    }

    function convertReagentRow(viewRow) {
        return new ReagentRow(viewRow);
    }

    function prepareReagentsForCalculation(change) {
        var index = _.findIndex(change.rows, function(row) {
            return _.isEqual(row, change.changedRow);
        });
        var rows = convertReagentsRows(change.rows);

        return {
            rows: rows,
            changedRow: rows[index],
            changedField: change.changedField
        };
    }

    function updateReagents(calculatedRows, viewRows) {
        _.forEach(viewRows, function(row, index) {
            var rowKeys = _.without(_.keys(row), '$$hashKey');
            var calcRow = calculatedRows[index];

            _.forEach(rowKeys, function(rowKey) {
                var viewRowField = row[rowKey];
                var calcRowField = calcRow[rowKey];

                if (!_.isObject(viewRowField)) {
                    row[rowKey] = calcRow[rowKey];
                } else {
                    var fieldKeys = _.keys(viewRowField);
                    _.forEach(fieldKeys, function(key) {
                        if (key in calcRowField) {
                            viewRowField[key] = calcRowField[key];
                        }
                    });
                }
            });
        });
    }
}

module.exports = stoichTableHelper;
