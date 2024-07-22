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

function onRxnRoleChanged() {
    describe('Change rxnRole', function() {
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

            firstRow.molWeight.value = 10;
            firstRow.weight.value = 110;
            firstRow.mol.value = 10;

            secondRow.molWeight.value = 5;
            secondRow.rxnRole = {name: 'REACTANT'};

            reagentsData = {
                rows: rows,
                idOfChangedRow: firstRow.id,
                changedField: fieldTypes.rxnRole
            };
        });

        it('set solvent role, should reset and disable weight, mol, eq, density, stoicPurity, molarity,' +
            ' saltCode, saltEq, loadFactor', function() {
            firstRow.weight.value = 10;
            firstRow.mol.value = 11;
            firstRow.eq.value = 2;
            firstRow.density.value = 3;
            firstRow.stoicPurity.value = 11;
            firstRow.molarity.value = 11;
            firstRow.saltCode = {name: '01 - HYDROCHLORIDE', weight: 1};
            firstRow.saltEq.value = 11;
            firstRow.rxnRole = {name: 'SOLVENT'};
            firstRow.loadFactor.value = 11;
            firstRow.limiting.value = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(0);
            expect(calculatedRows[0].weight.readonly).toBeTruthy();
            expect(calculatedRows[0].mol.value).toBe(0);
            expect(calculatedRows[0].mol.readonly).toBeTruthy();
            expect(calculatedRows[0].eq.value).toBe(1);
            expect(calculatedRows[0].eq.readonly).toBeTruthy();
            expect(calculatedRows[0].density.value).toBe(0);
            expect(calculatedRows[0].density.readonly).toBeTruthy();
            expect(calculatedRows[0].stoicPurity.value).toBe(100);
            expect(calculatedRows[0].stoicPurity.readonly).toBeTruthy();
            expect(calculatedRows[0].molarity.value).toBe(0);
            expect(calculatedRows[0].molarity.readonly).toBeTruthy();
            expect(calculatedRows[0].saltCode.weight).toBe(0);
            expect(calculatedRows[0].saltCode.readonly).toBeTruthy();
            expect(calculatedRows[0].saltEq.value).toBe(0);
            expect(calculatedRows[0].saltEq.readonly).toBeTruthy();
            expect(calculatedRows[0].loadFactor.value).toBe(1);
            expect(calculatedRows[0].loadFactor.readonly).toBeTruthy();
            expect(calculatedRows[0].limiting.value).toBeFalsy();
            expect(calculatedRows[0].limiting.readonly).toBeTruthy();
        });

        it('row is limiting, set solvent role, should reset and disable fields' +
            ' and set limiting to the next line', function() {
            firstRow.limiting.value = true;
            firstRow.rxnRole = {name: 'SOLVENT'};

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].limiting.value).toBeFalsy();
            expect(calculatedRows[1].limiting.value).toBeTruthy();
        });

        it('previous role was solvent, set reactant, should reset volume, enable fields and set mol of limiting' +
            ' row', function() {
            var readonlyFields = [
                fieldTypes.weight,
                fieldTypes.mol,
                fieldTypes.eq,
                fieldTypes.molarity,
                fieldTypes.density,
                fieldTypes.stoicPurity,
                fieldTypes.saltCode,
                fieldTypes.saltEq,
                fieldTypes.loadFactor,
                fieldTypes.limiting
            ];

            firstRow.limiting.value = true;
            secondRow.volume.value = 5;
            secondRow.prevRxnRole = {name: 'SOLVENT'};
            secondRow.setReadonly(readonlyFields, true);

            reagentsData.idOfChangedRow = secondRow.id;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[1].volume.value).toBe(0);
            expect(calculatedRows[1].weight.value).toBe(50);
            expect(calculatedRows[1].weight.readonly).toBeFalsy();
            expect(calculatedRows[1].mol.value).toBe(10);
            expect(calculatedRows[1].mol.readonly).toBeFalsy();
            expect(calculatedRows[1].eq.value).toBe(1);
            expect(calculatedRows[1].eq.readonly).toBeFalsy();
            expect(calculatedRows[1].density.value).toBe(0);
            expect(calculatedRows[1].density.readonly).toBeFalsy();
            expect(calculatedRows[1].stoicPurity.value).toBe(100);
            expect(calculatedRows[1].stoicPurity.readonly).toBeFalsy();
            expect(calculatedRows[1].molarity.value).toBe(0);
            expect(calculatedRows[1].molarity.readonly).toBeFalsy();
            expect(calculatedRows[1].saltCode.weight).toBe(0);
            expect(calculatedRows[1].saltCode.readonly).toBeFalsy();
            expect(calculatedRows[1].saltEq.value).toBe(0);
            expect(calculatedRows[1].saltEq.readonly).toBeFalsy();
            expect(calculatedRows[1].loadFactor.readonly).toBeFalsy();
            expect(calculatedRows[1].limiting.readonly).toBeFalsy();
        });
    });
}

module.exports = onRxnRoleChanged;
