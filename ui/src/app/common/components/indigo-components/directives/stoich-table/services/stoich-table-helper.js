/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
        isReactantWithMolfile: isReactantWithMolfile,
        hasMolfile: hasMolfile
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

    function hasMolfile(item) {
        return item.structure && item.structure.molfile;
    }

    function isReactantWithMolfile(item) {
        return hasMolfile(item) && isReactant(item);
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
