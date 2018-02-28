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

function onSaltCodeChanged() {
    describe('Change salt code/eq', function() {
        var service;
        var reagentsData;
        var rows;
        var firstRow;
        var secondRow;

        beforeEach(angular.mock.inject(function(_reagentsCalculation_) {
            service = _reagentsCalculation_;
        }));

        beforeEach(function() {
            rows = [];

            firstRow = new ReagentRow(new ReagentViewRow());
            secondRow = new ReagentRow(new ReagentViewRow());

            rows.push(firstRow);
            rows.push(secondRow);

            firstRow.molWeight.value = 3;
            firstRow.molWeight.originalValue = 3;
            firstRow.formula.baseValue = 'C6 H6';
            firstRow.weight.value = 45;
            firstRow.mol.value = 15;
            firstRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
            firstRow.saltEq.value = 12;

            reagentsData = {
                rows: rows,
                idOfChangedRow: firstRow.id,
                changedField: fieldTypes.saltEq
            };
        });

        it('should update molWeight and formula', function() {
            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].molWeight.value).toBe(15);
            expect(calculatedRows[0].formula.value).toBe('C6 H6*12(HYDROCHLORIDE)');
        });

        it('row is limiting, should compute mol, then weight and update mol in other rows', function() {
            firstRow.limiting.value = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].molWeight.value).toBe(15);
            expect(calculatedRows[0].mol.value).toBe(3);
            expect(calculatedRows[1].mol.value).toBe(3);
        });

        it('row is not limiting, should compute weight', function() {
            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].molWeight.value).toBe(15);
            expect(calculatedRows[0].weight.value).toBe(225);
            expect(calculatedRows[0].mol.value).toBe(15);
        });

        it('row is not limiting, should set original mol weight', function() {
            rows = service.calculate(reagentsData);
            firstRow = rows[0];

            expect(firstRow.molWeight.value).toBe(15);
            expect(firstRow.formula.value).toBe('C6 H6*12(HYDROCHLORIDE)');
            expect(firstRow.weight.value).toBe(225);
            expect(firstRow.mol.value).toBe(15);

            firstRow.saltCode = {name: '00 - Parent Structure', value: '0', regValue: '00', weight: 0};

            reagentsData.rows = rows;
            reagentsData.idOfChangedRow = firstRow.id;

            rows = service.calculate(reagentsData);
            firstRow = rows[0];

            expect(firstRow.molWeight.value).toBe(3);
            expect(firstRow.formula.value).toBe('C6 H6');
            expect(firstRow.weight.value).toBe(45);
            expect(firstRow.mol.value).toBe(15);
        });
    });
}

module.exports = onSaltCodeChanged;
