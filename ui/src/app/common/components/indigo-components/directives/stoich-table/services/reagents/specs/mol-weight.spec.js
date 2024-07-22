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

function onMolWeightChanged() {
    describe('Change mol weight', function() {
        var service;
        var reagentsData;
        var firstRow;

        beforeEach(angular.mock.inject(function(_reagentsCalculation_) {
            service = _reagentsCalculation_;
        }));

        beforeEach(function() {
            var rows = [];
            firstRow = new ReagentRow(new ReagentViewRow());
            rows.push(firstRow);

            reagentsData = {
                rows: rows,
                idOfChangedRow: firstRow.id,
                changedField: fieldTypes.molWeight
            };
        });

        it('weight is defined, row is limiting, should compute mol', function() {
            firstRow.molWeight.value = 23;
            firstRow.weight.value = 23;
            firstRow.weight.entered = true;
            firstRow.limiting.value = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].mol.value).toBe(1);
        });

        it('mol is defined, row is not limiting, should compute weight', function() {
            firstRow.molWeight.value = 23;
            firstRow.mol.value = 2;
            firstRow.mol.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(46);
        });
    });
}

module.exports = onMolWeightChanged;
