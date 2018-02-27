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

function onLimitingMolChanged() {
    describe('Change limiting mol', function() {
        var service;
        var productsData;
        var firstRow;
        var secondRow;

        beforeEach(angular.mock.inject(function(_productsCalculation_) {
            service = _productsCalculation_;
        }));

        beforeEach(function() {
            var rows = [];

            firstRow = new ProductRow(new ProductViewRow());
            secondRow = new ProductRow(new ProductViewRow());

            rows.push(firstRow);
            rows.push(secondRow);

            firstRow.molWeight.value = 20;
            firstRow.eq.value = 2;
            secondRow.molWeight.value = 40;
            secondRow.eq.value = 4;

            var limitingRow = new ReagentRow(new ReagentViewRow());
            limitingRow.limiting = true;
            limitingRow.mol.value = 4;
            limitingRow.eq.value = 2;

            productsData = {
                rows: rows,
                idOfChangedRow: null,
                changedField: null,
                limitingRow: limitingRow
            };
        });

        it('should update theoMoles and calculate theoWeight', function() {
            var calculatedRows = service.calculate(productsData);

            expect(calculatedRows[0].theoMoles.value).toBe(4);
            expect(calculatedRows[0].theoWeight.value).toBe(80);
            expect(calculatedRows[1].theoMoles.value).toBe(8);
            expect(calculatedRows[1].theoWeight.value).toBe(320);
        });

        it('should reset theoMoles and theoWeight', function() {
            firstRow.theoMoles.value = 2;
            firstRow.theoWeight.value = 4;
            secondRow.theoMoles.value = 2;
            secondRow.theoWeight.value = 4;

            productsData.limitingRow = null;
            var calculatedRows = service.calculate(productsData);

            expect(calculatedRows[0].theoMoles.value).toBe(0);
            expect(calculatedRows[0].theoWeight.value).toBe(0);
            expect(calculatedRows[1].theoMoles.value).toBe(0);
            expect(calculatedRows[1].theoWeight.value).toBe(0);
        });
    });
}

module.exports = onLimitingMolChanged;
