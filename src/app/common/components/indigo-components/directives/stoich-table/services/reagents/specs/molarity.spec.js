var ReagentViewRow = require('../../../domain/reagent/view-row/reagent-view-row');
var ReagentRow = require('../../../domain/reagent/calculation-row/reagent-row');
var fieldTypes = require('../../../domain/field-types');

function onMolarityChanged() {
    describe('Change molarity', function() {
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
                changedRow: firstRow,
                changedField: fieldTypes.molarity
            };
        });

        it('volume is defined, should compute mol, weight; eq should be default, therefore there is not limiting' +
            ' row', function() {
            firstRow.molWeight.value = 2;
            firstRow.weight.value = 22;
            firstRow.mol.value = 11;
            firstRow.volume.value = 4;
            firstRow.molarity.value = 3;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].mol.value).toBe(12);
            expect(calculatedRows[0].weight.value).toBe(24);
            expect(calculatedRows[0].limiting.value).toBeTruthy();
            expect(calculatedRows[0].eq.value).toBe(1);
            expect(calculatedRows[0].molWeight.value).toBe(2);
            expect(calculatedRows[0].volume.value).toBe(4);
        });

        it('volume is defined, should compute mol, weight; eq should be 6,' +
            ' therefore there is limiting row', function() {
            firstRow.molWeight.value = 5;
            firstRow.weight.value = 10;
            firstRow.mol.value = 2;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 2;
            secondRow.volume.value = 4;
            secondRow.molarity.value = 3;

            reagentsData.changedRow = secondRow;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[1].mol.value).toBe(12);
            expect(calculatedRows[1].weight.value).toBe(24);
            expect(calculatedRows[1].limiting.value).toBeFalsy();
            expect(calculatedRows[1].eq.value).toBe(6);
            expect(calculatedRows[1].molWeight.value).toBe(2);
            expect(calculatedRows[1].volume.value).toBe(4);
        });

        it('volume is computed, mol is manually entered, volume should be 0', function() {
            firstRow.molWeight.value = 2;
            firstRow.weight.value = 30;
            firstRow.mol.value = 15;
            firstRow.mol.entered = true;
            firstRow.volume.value = 5;
            firstRow.molarity.value = 0;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].volume.value).toBe(0);
            expect(calculatedRows[0].mol.value).toBe(15);
            expect(calculatedRows[0].mol.entered).toBeTruthy();
        });

        it('mol is computed, volume is manually entered, mol should be 0', function() {
            firstRow.molWeight.value = 2;
            firstRow.weight.value = 30;
            firstRow.mol.value = 15;
            firstRow.volume.value = 5;
            firstRow.volume.entered = true;
            firstRow.molarity.value = 0;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].volume.value).toBe(5);
            expect(calculatedRows[0].volume.entered).toBeTruthy();
            expect(calculatedRows[0].mol.value).toBe(0);
            expect(calculatedRows[0].weight.value).toBe(0);
        });

        it('volume is not defined, mol is defined, should compute volume', function() {
            firstRow.molWeight.value = 2;
            firstRow.weight.value = 30;
            firstRow.mol.value = 15;

            firstRow.molarity.value = 3;
            firstRow.molarity.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].volume.value).toBe(5);
        });

        it('set molarity 0, volume is 0, should update mol from limiting row', function() {
            firstRow.molWeight.value = 1;
            firstRow.weight.value = 2;
            firstRow.weight.entered = true;
            firstRow.mol.value = 2;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 1;
            secondRow.density.value = 0;

            reagentsData.changedRow = secondRow;
            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[1].weight.value).toBe(2);
            expect(calculatedRows[1].mol.value).toBe(2);
        });
    });
}

module.exports = onMolarityChanged;
