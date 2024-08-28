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
var fieldTypes = require('../../../../../services/calculation/field-types');

function onMolChanged() {
    describe('Change mol', function() {
        var service;
        var reagentsData;
        var firstRow;
        var secondRow;

        beforeEach(angular.mock.inject(function(_reagentsCalculation_) {
            service = _reagentsCalculation_;
        }));

        beforeEach(function() {
            var rows = [];

            firstRow = new ReagentRow(new ReagentViewRow());
            secondRow = new ReagentRow(new ReagentViewRow());

            rows.push(firstRow);
            rows.push(secondRow);

            reagentsData = {
                rows: rows,
                idOfChangedRow: firstRow.id,
                changedField: fieldTypes.mol
            };
        });

        it('mol weight is defined, row is not limiting; should compute weight, it should be 110', function() {
            firstRow.molWeight.value = 10;
            firstRow.mol.value = 11;
            firstRow.mol.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(110);
        });

        it('mol weight is defined, row is not limiting; mol is removed or 0, weight should be 0', function() {
            firstRow.molWeight.value = 10;
            firstRow.mol.value = 0;
            firstRow.mol.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(0);
            expect(calculatedRows[0].mol.entered).toBeFalsy();
        });

        it('mol weight is defined, row is limiting; should compute weight and update mol in other rows', function() {
            firstRow.molWeight.value = 10;
            firstRow.mol.value = 11;
            firstRow.mol.entered = true;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 2;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(110);
            expect(calculatedRows[1].mol.value).toBe(11);
            expect(calculatedRows[1].weight.value).toBe(22);
        });

        it('mol weight is defined, row is limiting, mol is removed or 0; weight of limiting row should be 0 and' +
            ' mol of other rows are the same', function() {
            firstRow.molWeight.value = 10;
            firstRow.weight.value = 22;
            firstRow.weight.entered = true;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 1;
            secondRow.mol.value = 10;
            secondRow.weight.value = 10;

            firstRow.mol.value = 0;
            firstRow.mol.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(0);
            expect(calculatedRows[1].mol.value).toBe(10);
            expect(calculatedRows[1].weight.value).toBe(10);
        });

        it('molarity is entered, volume is computed, mol set to 0, should reset volume and weight', function() {
            firstRow.molWeight.value = 10;
            firstRow.weight.value = 22;
            firstRow.volume.value = 2;
            firstRow.molarity.value = 2;
            firstRow.molarity.entered = true;

            firstRow.mol.value = 0;
            firstRow.mol.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(0);
            expect(calculatedRows[0].volume.value).toBe(0);
        });

        it('mol weight is not defined, but weight is defined, should compute mol weight', function() {
            firstRow.weight.value = 11;
            firstRow.mol.value = 11;
            firstRow.mol.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].molWeight.value).toBe(1);
        });

        it('mol weight, volume, weight, molarity, eq are defined, should recompute volume, weight and eq', function() {
            firstRow.molWeight.value = 2;
            firstRow.weight.value = 40;
            firstRow.volume.value = 10;
            firstRow.molarity.value = 2;
            firstRow.molarity.entered = true;
            firstRow.limiting.value = true;

            firstRow.mol.value = 10;
            firstRow.mol.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(20);
            expect(calculatedRows[0].volume.value).toBe(5);
            expect(calculatedRows[0].mol.value).toBe(10);
            expect(calculatedRows[0].molarity.value).toBe(2);
            expect(calculatedRows[0].eq.value).toBe(1);
            expect(calculatedRows[0].molWeight.value).toBe(2);
        });

        it('mol weight, purity are defined, should compute weight', function() {
            firstRow.molWeight.value = 2;
            firstRow.stoicPurity.value = 50;
            firstRow.stoicPurity.entered = true;
            firstRow.limiting.value = true;

            firstRow.mol.value = 10;
            firstRow.mol.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(40);
            expect(calculatedRows[0].mol.value).toBe(10);
        });
    });
}

module.exports = onMolChanged;
