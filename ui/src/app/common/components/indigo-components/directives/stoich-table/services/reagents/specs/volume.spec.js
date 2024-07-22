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

function onVolumeChanged() {
    describe('Change volume', function() {
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

            reagentsData = {
                rows: rows,
                idOfChangedRow: firstRow.id,
                changedField: fieldTypes.volume
            };
        });

        it('should set mol, weight to 0, limiting to false and eq to 1, set limiting to next line with' +
            ' reactant/reaction role and eq=1, should not update rows with new limiting mol', function() {
            firstRow.molWeight.value = 10;
            firstRow.weight.value = 220;
            firstRow.weight.entered = true;
            firstRow.mol.value = 22;
            firstRow.eq.value = 2;
            firstRow.volume.value = 4;
            firstRow.volume.entered = true;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 1;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(0);
            expect(calculatedRows[0].mol.value).toBe(0);
            expect(calculatedRows[0].eq.value).toBe(1);
            expect(calculatedRows[0].limiting.value).toBeFalsy();
            expect(calculatedRows[0].molWeight.value).toBe(10);

            expect(calculatedRows[1].limiting.value).toBeTruthy();
        });

        it('row is not limiting; should set mol, weight to 0', function() {
            firstRow.molWeight.value = 10;
            firstRow.weight.value = 22;
            firstRow.mol.value = 11;

            firstRow.volume.value = 4;
            firstRow.volume.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(0);
            expect(calculatedRows[0].mol.value).toBe(0);
            expect(calculatedRows[0].limiting.value).toBeFalsy();
            expect(calculatedRows[0].molWeight.value).toBe(10);
        });

        it('row is not limiting; weight, mol are 0, set volume to 0 or remove,' +
            ' should set mol from limiting row', function() {
            firstRow.molWeight.value = 2;
            firstRow.weight.value = 22;
            firstRow.mol.value = 11;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 3;
            secondRow.mol.value = 0;
            secondRow.weight.value = 0;

            reagentsData.idOfChangedRow = secondRow.id;
            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].limiting.value).toBeTruthy();
            expect(calculatedRows[0].mol.value).toBe(11);

            expect(calculatedRows[1].limiting.value).toBeFalsy();
            expect(calculatedRows[1].mol.value).toBe(11);
            expect(calculatedRows[1].weight.value).toBe(33);
        });

        it('row is limiting; volume is entered for non limiting row, should not update mol of this row', function() {
            firstRow.molWeight.value = 2;
            firstRow.weight.value = 22;
            firstRow.mol.value = 11;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 3;
            secondRow.volume.value = 3;
            secondRow.volume.entered = true;

            reagentsData.idOfChangedRow = secondRow.id;
            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].limiting.value).toBeTruthy();
            expect(calculatedRows[0].mol.value).toBe(11);

            expect(calculatedRows[1].limiting.value).toBeFalsy();
            expect(calculatedRows[1].mol.value).toBe(0);
            expect(calculatedRows[1].weight.value).toBe(0);
        });

        it('molarity, molWeight are defined, should compute mol, weight and eq', function() {
            firstRow.molWeight.value = 2;
            firstRow.molarity.value = 4;
            firstRow.volume.value = 5;
            firstRow.volume.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].molarity.value).toBe(4);
            expect(calculatedRows[0].volume.value).toBe(5);
            expect(calculatedRows[0].mol.value).toBe(20);
            expect(calculatedRows[0].weight.value).toBe(40);
            expect(calculatedRows[0].eq.value).toBe(1);
        });

        it('molarity, molWeight, mol are defined, set volume to 0, should remove mol, weight', function() {
            firstRow.molWeight.value = 2;
            firstRow.molarity.value = 4;
            firstRow.mol.value = 20;
            firstRow.weight.value = 40;
            firstRow.volume.value = 0;
            firstRow.volume.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].molarity.value).toBe(4);
            expect(calculatedRows[0].volume.value).toBe(0);
            expect(calculatedRows[0].mol.value).toBe(0);
            expect(calculatedRows[0].weight.value).toBe(0);
            expect(calculatedRows[0].eq.value).toBe(1);
        });

        it('density is defined, should compute weight', function() {
            firstRow.volume.value = 5;
            firstRow.density.value = 20;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(100000);
        });

        it('density is defined, should compute weight and mol', function() {
            firstRow.molWeight.value = 1000;
            firstRow.volume.value = 5;
            firstRow.density.value = 20;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].mol.value).toBe(100);
            expect(calculatedRows[0].weight.value).toBe(100000);
        });

        it('density is defined, set volume 0, should set weight 0', function() {
            firstRow.volume.value = 0;
            firstRow.weight.value = 3;
            firstRow.density.value = 20;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(0);
        });
    });
}

module.exports = onVolumeChanged;
