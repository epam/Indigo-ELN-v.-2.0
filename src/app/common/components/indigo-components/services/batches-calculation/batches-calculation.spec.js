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

var BatchViewRow = require('../../directives/indigo-components/domain/batch-row/view-row/batch-view-row');
var ReagentViewRow = require('../../directives/stoich-table/domain/reagent/view-row/reagent-view-row');
var ReagentRow = require('../../directives/stoich-table/domain/reagent/calculation-row/reagent-row');
var fieldTypes = require('../../services/calculation/field-types');

describe('service: batchesCalculation', function() {
    var service;
    var batchesData;
    var firstRow;
    var secondRow;
    var rows;

    beforeEach(angular.mock.module('indigoeln.indigoComponents'));

    beforeEach(angular.mock.inject(function(_batchesCalculation_) {
        service = _batchesCalculation_;
    }));

    beforeEach(function() {
        rows = [];

        firstRow = new BatchViewRow();
        secondRow = new BatchViewRow();

        rows.push(firstRow);
        rows.push(secondRow);

        firstRow.molWeight.value = 20;
        secondRow.molWeight.value = 40;
    });

    it('should be defined', function() {
        expect(service).toBeDefined();
    });

    describe('calculateAllRows function', function() {
        beforeEach(function() {
            var limitingRow = new ReagentRow(new ReagentViewRow());
            limitingRow.limiting = true;
            limitingRow.mol.value = 4;

            batchesData = {
                rows: rows,
                limitingRow: limitingRow
            };
        });

        it('should update theoMoles and calculate theoWeight', function() {
            firstRow.totalMoles.value = 2;
            firstRow.totalMoles.entered = true;

            var calculatedRows = service.calculateAllRows(batchesData);

            expect(calculatedRows[0].theoMoles.value).toBe(4);
            expect(calculatedRows[0].theoWeight.value).toBe(80);
            expect(calculatedRows[0].molWeight.value).toBe(20);
            expect(calculatedRows[0].totalWeight.value).toBe(40);
            expect(calculatedRows[0].yield).toBe(50);
            expect(calculatedRows[1].theoMoles.value).toBe(4);
            expect(calculatedRows[1].theoWeight.value).toBe(160);
            expect(calculatedRows[1].molWeight.value).toBe(40);
        });


        it('totalMoles is entered, should calculate totalWeight and yield', function() {
            firstRow.totalMoles.value = 2;
            firstRow.totalMoles.entered = true;

            var calculatedRows = service.calculateAllRows(batchesData);

            expect(calculatedRows[0].totalWeight.value).toBe(40);
            expect(calculatedRows[0].yield).toBe(50);
        });

        it('totalWeight is entered, should calculate totalMoles and yield', function() {
            firstRow.totalWeight.value = 2;
            firstRow.totalWeight.entered = true;

            var calculatedRows = service.calculateAllRows(batchesData);

            expect(calculatedRows[0].totalMoles.value).toBe(0.1);
            expect(calculatedRows[0].yield).toBe(3);
        });
    });

    describe('calculateRow function', function() {
        describe('Change salt code/eq', function() {
            beforeEach(function() {
                firstRow.molWeight.value = 3;
                firstRow.molWeight.baseValue = 3;
                firstRow.formula.baseValue = 'C6 H6';
                firstRow.theoWeight.value = 12;
                firstRow.theoMoles.value = 4;
                firstRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
                firstRow.saltEq.value = 12;

                batchesData = {
                    changedRow: firstRow,
                    changedField: fieldTypes.saltCode
                };
            });

            it('should update molWeight and formula, then set theoMoles and compute theoWeight', function() {
                var calculatedRow = service.calculateRow(batchesData);

                expect(calculatedRow.molWeight.value).toBe(15);
                expect(calculatedRow.formula.value).toBe('C6 H6*12(HYDROCHLORIDE)');
                expect(calculatedRow.theoMoles.value).toBe(4);
                expect(calculatedRow.theoWeight.value).toBe(60);
            });

            it('should set original mol weight and formula', function() {
                firstRow = service.calculateRow(batchesData);

                expect(firstRow.molWeight.value).toBe(15);
                expect(firstRow.theoMoles.value).toBe(4);
                expect(firstRow.theoWeight.value).toBe(60);

                firstRow.saltCode = {name: '00 - Parent Structure', value: '0', regValue: '00', weight: 0};

                batchesData.changedRow = firstRow;
                firstRow = service.calculateRow(batchesData);

                expect(firstRow.molWeight.value).toBe(3);
                expect(firstRow.theoMoles.value).toBe(4);
                expect(firstRow.theoWeight.value).toBe(12);
                expect(firstRow.formula.value).toBe('C6 H6');
            });
        });

        describe('Change molWeight', function() {
            beforeEach(function() {
                firstRow.molWeight.value = 3;
                firstRow.molWeight.baseValue = 3;
                firstRow.formula.baseValue = 'C6 H6';
                firstRow.theoMoles.value = 4;

                batchesData = {
                    changedRow: firstRow,
                    changedField: fieldTypes.molWeight
                };
            });

            it('should update molWeight and formula, then set theoMoles and compute theoWeight', function() {
                var calculatedRow = service.calculateRow(batchesData);

                expect(calculatedRow.molWeight.value).toBe(3);
                expect(calculatedRow.theoMoles.value).toBe(4);
                expect(calculatedRow.theoWeight.value).toBe(12);
            });

            it('totalWeight is entered, should compute totalMoles and yield', function() {
                firstRow.totalWeight.value = 6;
                firstRow.totalWeight.entered = true;

                var calculatedRow = service.calculateRow(batchesData);

                expect(calculatedRow.totalMoles.value).toBe(2);
                expect(calculatedRow.yield).toBe(50);
            });

            it('totalMoles is entered, should compute totalWeight and yield', function() {
                firstRow.totalMoles.value = 6;
                firstRow.totalMoles.entered = true;

                var calculatedRow = service.calculateRow(batchesData);

                expect(calculatedRow.totalWeight.value).toBe(18);
                expect(calculatedRow.yield).toBe(150);
            });
        });

        describe('Change totalWeight/totalMoles', function() {
            beforeEach(function() {
                firstRow.molWeight.value = 3;
                firstRow.molWeight.baseValue = 3;
                firstRow.theoMoles.value = 4;
                firstRow.totalWeight.value = 0;
                firstRow.totalMoles.value = 0;
                firstRow.totalMoles.entered = false;
                firstRow.totalWeight.entered = false;

                batchesData = {
                    changedRow: firstRow
                };
            });

            it('should reset entered of totalMoles, then compute totalMoles and yield', function() {
                batchesData.changedField = fieldTypes.totalWeight;
                firstRow.totalWeight.value = 6;
                firstRow.totalMoles.value = 77;
                firstRow.totalMoles.entered = true;

                var calculatedRow = service.calculateRow(batchesData);

                expect(calculatedRow.totalMoles.value).toBe(2);
                expect(calculatedRow.totalMoles.entered).toBeFalsy();
                expect(calculatedRow.yield).toBe(50);
            });


            it('should reset entered of totalWeight, then compute totalWeight and yield', function() {
                batchesData.changedField = fieldTypes.totalMoles;
                firstRow.totalWeight.value = 66;
                firstRow.totalWeight.entered = true;
                firstRow.totalMoles.value = 6;

                var calculatedRow = service.calculateRow(batchesData);

                expect(calculatedRow.totalWeight.value).toBe(18);
                expect(calculatedRow.totalWeight.entered).toBeFalsy();
                expect(calculatedRow.yield).toBe(150);
            });
        });

        describe('Change volume', function() {
            beforeEach(function() {
                firstRow.totalWeight.value = 34;
                firstRow.totalMoles.value = 321;
                firstRow.totalMoles.entered = true;
                firstRow.totalWeight.entered = true;

                batchesData = {
                    changedRow: firstRow
                };
            });

            it('should reset entered and value of totalMoles, totalWeight and yield', function() {
                batchesData.changedField = fieldTypes.totalVolume;

                var calculatedRow = service.calculateRow(batchesData);

                expect(calculatedRow.totalMoles.value).toBe(0);
                expect(calculatedRow.totalMoles.entered).toBeFalsy();
                expect(calculatedRow.totalWeight.value).toBe(0);
                expect(calculatedRow.totalWeight.entered).toBeFalsy();
                expect(calculatedRow.yield).toBe(0);
            });
        });
    });
});
