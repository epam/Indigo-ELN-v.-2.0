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

function onPurityChanged() {
    describe('Change purity', function() {
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

            firstRow.molWeight.value = 2;
            firstRow.weight.value = 30;
            firstRow.mol.value = 15;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 3;
            secondRow.weight.value = 45;
            secondRow.mol.value = 15;

            reagentsData = {
                rows: rows,
                idOfChangedRow: firstRow.id,
                changedField: fieldTypes.stoicPurity
            };
        });

        describe('row is limiting', function() {
            beforeEach(function() {
                firstRow.stoicPurity.value = 10;
                firstRow.stoicPurity.entered = true;
            });

            it('weight is manually set, should compute mol and update mol in other rows', function() {
                firstRow.weight.entered = true;

                var calculatedRows = service.calculate(reagentsData);

                expect(calculatedRows[0].mol.value).toBe(1.5);
                expect(calculatedRows[0].weight.value).toBe(30);
                expect(calculatedRows[1].mol.value).toBe(1.5);
                expect(calculatedRows[1].weight.value).toBe(4.5);
            });

            it('mol is manually set, should compute only weight', function() {
                firstRow.mol.entered = true;

                var calculatedRows = service.calculate(reagentsData);

                expect(calculatedRows[0].mol.value).toBe(15);
                expect(calculatedRows[0].weight.value).toBe(300);
                expect(calculatedRows[1].mol.value).toBe(15);
                expect(calculatedRows[1].weight.value).toBe(45);
            });

            it('mol is manually set, should compute weight', function() {
                firstRow.mol.entered = true;
                firstRow.stoicPurity.value = 50;

                var calculatedRows = service.calculate(reagentsData);

                expect(calculatedRows[0].mol.value).toBe(15);
                expect(calculatedRows[0].mol.entered).toBeTruthy();
                expect(calculatedRows[0].weight.value).toBe(60);
            });
        });

        describe('row is not limiting', function() {
            beforeEach(function() {
                secondRow.stoicPurity.entered = true;
                secondRow.stoicPurity.value = 10;

                reagentsData.idOfChangedRow = secondRow.id;
            });

            it('mol and weight are not entered, should compute only weight', function() {
                var calculatedRows = service.calculate(reagentsData);

                expect(calculatedRows[1].mol.value).toBe(15);
                expect(calculatedRows[1].weight.value).toBe(450);
            });

            it('set purity to 50 and then to 100, weight should be original', function() {
                secondRow.stoicPurity.value = 50;

                rows = service.calculate(reagentsData);
                secondRow = rows[1];

                expect(secondRow.mol.value).toBe(15);
                expect(secondRow.weight.value).toBe(90);

                secondRow.stoicPurity.value = 100;

                reagentsData.rows = rows;
                reagentsData.idOfChangedRow = secondRow.id;

                rows = service.calculate(reagentsData);
                secondRow = rows[1];

                expect(secondRow.mol.value).toBe(15);
                expect(secondRow.weight.value).toBe(45);
            });

            it('weight is manually set, should compute mol and update eq', function() {
                secondRow.weight.entered = true;

                var calculatedRows = service.calculate(reagentsData);

                expect(calculatedRows[1].mol.value).toBe(1.5);
                expect(calculatedRows[1].weight.value).toBe(45);
                expect(calculatedRows[1].eq.value).toBe(0.1);
            });

            it('mol is manually set, should compute weight', function() {
                secondRow.mol.entered = true;
                secondRow.stoicPurity.value = 50;

                var calculatedRows = service.calculate(reagentsData);

                expect(calculatedRows[1].mol.value).toBe(15);
                expect(calculatedRows[1].mol.entered).toBeTruthy();
                expect(calculatedRows[1].weight.value).toBe(90);
            });
        });

        it('complex test ' +
            '1. Change Purity of non-limiting row to 50, should update weight' +
            '2. Change Purity of limiting row to 50, should update mol' +
            '3. Change Purity of non-limiting row to 100, should update', function() {
            firstRow.weight.entered = true;

            secondRow.stoicPurity.value = 50;
            secondRow.stoicPurity.entered = true;

            reagentsData.idOfChangedRow = secondRow.id;

            rows = service.calculate(reagentsData);
            firstRow = rows[0];
            secondRow = rows[1];

            expect(secondRow.mol.value).toBe(15);
            expect(secondRow.weight.value).toBe(90);

            firstRow.stoicPurity.value = 50;
            firstRow.stoicPurity.entered = true;

            reagentsData.rows = rows;
            reagentsData.idOfChangedRow = firstRow.id;

            rows = service.calculate(reagentsData);
            firstRow = rows[0];
            secondRow = rows[1];

            expect(firstRow.mol.value).toBe(7.5);
            expect(firstRow.weight.value).toBe(30);
            expect(secondRow.mol.value).toBe(7.5);
            expect(secondRow.weight.value).toBe(45);

            secondRow.stoicPurity.value = 100;
            secondRow.stoicPurity.entered = true;

            reagentsData.rows = rows;
            reagentsData.idOfChangedRow = secondRow.id;

            rows = service.calculate(reagentsData);
            secondRow = rows[1];

            expect(secondRow.mol.value).toBe(7.5);
            expect(secondRow.weight.value).toBe(22.5);
        });
    });
}

module.exports = onPurityChanged;
