var ReagentField = require('../../reagent/reagent-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changeDensity() {
    describe('Change density', function() {
        var service;

        beforeEach(function() {
            var config = {
                table: {product: [], reactants: []}
            };

            service = stoichTable(config);
        });

        it('weight is defined, should compute volume', function() {
            var reagentRow = new ReagentField();
            reagentRow.weight.value = 20;
            reagentRow.density.value = 4;

            service.onFieldValueChanged(reagentRow, fieldTypes.density);

            expect(reagentRow.volume.value).toBe(0.005);
        });

        it('volume is defined, should compute weight', function() {
            var reagentRow = new ReagentField();
            reagentRow.volume.value = 0.005;
            reagentRow.density.value = 4;

            service.onFieldValueChanged(reagentRow, fieldTypes.density);

            expect(reagentRow.weight.value).toBe(20);
        });

        it('set density 0, volume is entered, weight should be 0', function() {
            var reagentRow = new ReagentField();
            reagentRow.molWeight.value = 1;
            reagentRow.mol.value = 2;
            reagentRow.volume.value = 0.005;
            reagentRow.volume.entered = true;
            reagentRow.weight.value = 2;
            reagentRow.density.value = 0;

            service.onFieldValueChanged(reagentRow, fieldTypes.density);

            expect(reagentRow.volume.value).toBe(0.005);
            expect(reagentRow.volume.entered).toBeTruthy();
            expect(reagentRow.weight.value).toBe(0);
            expect(reagentRow.mol.value).toBe(0);
        });

        it('volume is entered, set density, should recalculate eq', function() {
            var limitingRow = new ReagentField();
            limitingRow.molWeight.value = 1;
            limitingRow.weight.value = 2;
            limitingRow.weight.entered = true;
            limitingRow.mol.value = 2;
            service.addRow(limitingRow);

            var otherRow = new ReagentField();
            otherRow.molWeight.value = 1;
            service.addRow(otherRow);
            otherRow.volume.value = 4;
            otherRow.volume.entered = true;
            otherRow.density.value = 1;
            otherRow.density.entered = true;

            service.onFieldValueChanged(otherRow, fieldTypes.density);

            expect(otherRow.weight.value).toBe(4000);
            expect(otherRow.eq.value).toBe(2000);
        });

        it('set density 0, volume is 0, should update mol from limiting row', function() {
            var limitingRow = new ReagentField();
            limitingRow.molWeight.value = 1;
            limitingRow.weight.value = 2;
            limitingRow.weight.entered = true;
            limitingRow.mol.value = 2;
            service.addRow(limitingRow);

            var otherRow = new ReagentField();
            otherRow.molWeight.value = 1;
            otherRow.density.value = 0;
            service.addRow(otherRow);

            service.onFieldValueChanged(otherRow, fieldTypes.density);

            expect(otherRow.weight.value).toBe(2);
            expect(otherRow.mol.value).toBe(2);
        });
    });
}

module.exports = changeDensity;
