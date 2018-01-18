var ReagentViewRow = require('../../../domain/reagent/view-row/reagent-view-row');
var ReagentRow = require('../../../domain/reagent/calculation-row/reagent-row');
var fieldTypes = require('../../../../../services/calculation/field-types');

function onDensityChanged() {
    describe('Change density', function() {
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
                changedField: fieldTypes.density
            };
        });

        it('weight is defined, should compute volume', function() {
            firstRow.weight.value = 20;
            firstRow.weight.entered = true;
            firstRow.density.value = 4;
            firstRow.density.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].volume.value).toBe(0.005);
        });

        it('volume is defined, should compute weight', function() {
            firstRow.volume.value = 0.005;
            firstRow.density.value = 4;
            firstRow.density.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(20);
        });

        it('set density 0, volume is entered, weight should be 0', function() {
            firstRow.molWeight.value = 1;
            firstRow.mol.value = 2;
            firstRow.volume.value = 0.005;
            firstRow.volume.entered = true;
            firstRow.weight.value = 2;
            firstRow.density.value = 0;
            firstRow.density.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].volume.value).toBe(0.005);
            expect(calculatedRows[0].volume.entered).toBeTruthy();
            expect(calculatedRows[0].weight.value).toBe(0);
            expect(calculatedRows[0].mol.value).toBe(0);
        });

        it('volume is entered, set density, should recalculate eq', function() {
            firstRow.molWeight.value = 1;
            firstRow.weight.value = 2;
            firstRow.weight.entered = true;
            firstRow.mol.value = 2;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 1;
            secondRow.volume.value = 4;
            secondRow.volume.entered = true;
            secondRow.density.value = 1;
            secondRow.density.entered = true;

            reagentsData.idOfChangedRow = secondRow.id;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[1].weight.value).toBe(4000);
            expect(calculatedRows[1].eq.value).toBe(2000);
        });

        it('set density 0, volume is 0, should update mol from limiting row', function() {
            firstRow.molWeight.value = 1;
            firstRow.weight.value = 2;
            firstRow.weight.entered = true;
            firstRow.mol.value = 2;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 1;
            secondRow.density.value = 0;
            secondRow.density.entered = true;

            reagentsData.idOfChangedRow = secondRow.id;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[1].weight.value).toBe(2);
            expect(calculatedRows[1].mol.value).toBe(2);
        });
    });
}

module.exports = onDensityChanged;
