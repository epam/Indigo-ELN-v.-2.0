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

var ReagentViewRow = require('../../../domain/reagent/view-row/reagent-view-row');
var ReagentRow = require('../../../domain/reagent/calculation-row/reagent-row');
var ProductViewRow = require('../../../domain/product/view-row/product-view-row');
var ProductRow = require('../../../domain/product/calculation-row/product-row');
var fieldTypes = require('../../../../../services/calculation/field-types');

function onEqChanged() {
    describe('Change eq', function() {
        var service;
        var productsData;
        var rows;
        var firstRow;
        var secondRow;

        beforeEach(angular.mock.inject(function(_productsCalculation_) {
            service = _productsCalculation_;
        }));

        beforeEach(function() {
            rows = [];

            firstRow = new ProductRow(new ProductViewRow());
            secondRow = new ProductRow(new ProductViewRow());

            rows.push(firstRow);
            rows.push(secondRow);

            firstRow.molWeight.value = 3;
            firstRow.molWeight.baseValue = 3;
            firstRow.eq.value = 4;

            var limitingRow = new ReagentRow(new ReagentViewRow());
            limitingRow.limiting = true;
            limitingRow.mol.value = 4;

            productsData = {
                rows: rows,
                idOfChangedRow: firstRow.id,
                changedField: fieldTypes.eq,
                limitingRow: limitingRow
            };
        });

        it('should recalculate theoMoles and then theoWeight', function() {
            var calculatedRows = service.calculate(productsData);

            expect(calculatedRows[0].theoWeight.value).toBe(48);
            expect(calculatedRows[0].theoMoles.value).toBe(16);
        });
    });
}

module.exports = onEqChanged;
