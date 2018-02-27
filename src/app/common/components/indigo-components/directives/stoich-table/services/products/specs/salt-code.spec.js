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

function onSaltChanged() {
    describe('Change salt code/eq', function() {
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
            firstRow.formula.baseValue = 'C6 H6';
            firstRow.theoWeight.value = 12;
            firstRow.theoMoles.value = 4;
            firstRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
            firstRow.saltEq.value = 12;

            var limitingRow = new ReagentRow(new ReagentViewRow());
            limitingRow.limiting = true;
            limitingRow.mol.value = 4;

            productsData = {
                rows: rows,
                idOfChangedRow: firstRow.id,
                changedField: fieldTypes.saltCode,
                limitingRow: limitingRow
            };
        });

        it('should update molWeight and formula, then compute theoMoles and theoWeight', function() {
            var calculatedRows = service.calculate(productsData);

            expect(calculatedRows[0].molWeight.value).toBe(15);
            expect(calculatedRows[0].formula.value).toBe('C6 H6*12(HYDROCHLORIDE)');
            expect(calculatedRows[0].theoMoles.value).toBe(4);
            expect(calculatedRows[0].theoWeight.value).toBe(60);
        });

        it('should set original mol weight and formula', function() {
            rows = service.calculate(productsData);
            firstRow = rows[0];

            expect(firstRow.molWeight.value).toBe(15);
            expect(firstRow.theoMoles.value).toBe(4);
            expect(firstRow.theoWeight.value).toBe(60);

            firstRow.saltCode = {name: '00 - Parent Structure', value: '0', regValue: '00', weight: 0};

            productsData.rows = rows;
            productsData.idOfChangedRow = firstRow.id;

            rows = service.calculate(productsData);
            firstRow = rows[0];

            expect(firstRow.molWeight.value).toBe(3);
            expect(firstRow.theoMoles.value).toBe(4);
            expect(firstRow.theoWeight.value).toBe(12);
            expect(firstRow.formula.value).toBe('C6 H6');
        });
    });
}

module.exports = onSaltChanged;
