var ReagentRow = require('../domain/reagent/calculation-row/reagent-row');
var ProductRow = require('../domain/product/calculation-row/product-row');
var ProductViewRow = require('../domain/product/view-row/product-view-row');

/* @ngInject */
function stoichTableHelper(reagentsCalculation, productsCalculation, calculationHelper) {
    return {
        onReagentsChanged: onReagentsChanged,
        onProductsChanged: onProductsChanged,
        findLimitingRow: findLimitingRow,
        convertToProductViewRow: convertToProductViewRow,
        getPrecursors: getPrecursors,
        getReactantsWithMolfile: getReactantsWithMolfile,
        isReactantWithMolfile: isReactantWithMolfile
    };

    function onReagentsChanged(change) {
        var preparedReagentsData = prepareReagentsForCalculation(change);
        var calculatedRows = reagentsCalculation.calculate(preparedReagentsData);

        calculationHelper.updateViewRows(calculatedRows, change.rows);
    }

    function onProductsChanged(change) {
        var preparedProductData = prepareProductsForCalculation(change);
        var calculatedRows = productsCalculation.calculate(preparedProductData);

        calculationHelper.updateViewRows(calculatedRows, change.rows);
    }

    function findLimitingRow(reagents) {
        return _.find(reagents, ['limiting.value', true]);
    }

    function convertToProductViewRow(products) {
        return _.map(products, function(item) {
            return new ProductViewRow(item);
        });
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

    function prepareReagentsForCalculation(change) {
        var rows = convertReagentsRows(change.rows);

        return {
            rows: rows,
            idOfChangedRow: change.idOfChangedRow,
            changedField: change.changedField
        };
    }

    function prepareProductsForCalculation(change) {
        var rows = convertProductsRows(change.rows);

        return {
            rows: rows,
            idOfChangedRow: change.idOfChangedRow,
            changedField: change.changedField,
            limitingRow: change.limitingRow
        };
    }

    function convertReagentsRows(viewRows) {
        return _.map(viewRows, function(viewRow) {
            return new ReagentRow(viewRow);
        });
    }

    function convertProductsRows(viewRows) {
        return _.map(viewRows, function(viewRow) {
            return new ProductRow(viewRow);
        });
    }
}

module.exports = stoichTableHelper;
