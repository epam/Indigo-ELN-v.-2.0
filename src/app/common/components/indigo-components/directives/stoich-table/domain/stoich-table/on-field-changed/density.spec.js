var StoichRow = require('../../stoich-row');
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
            var stoichRow = new StoichRow();
            stoichRow.weight.value = 20;
            stoichRow.density.value = 4;

            service.onFieldValueChanged(stoichRow, fieldTypes.density);

            expect(stoichRow.volume.value).toBe(0.005);
        });

        it('volume is defined, should compute weight', function() {
            var stoichRow = new StoichRow();
            stoichRow.volume.value = 0.005;
            stoichRow.density.value = 4;

            service.onFieldValueChanged(stoichRow, fieldTypes.density);

            expect(stoichRow.weight.value).toBe(20);
        });

        it('set density 0, volume is entered, weight should be 0', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 1;
            stoichRow.mol.value = 2;
            stoichRow.volume.value = 0.005;
            stoichRow.volume.entered = true;
            stoichRow.weight.value = 2;
            stoichRow.density.value = 0;

            service.onFieldValueChanged(stoichRow, fieldTypes.density);

            expect(stoichRow.volume.value).toBe(0.005);
            expect(stoichRow.volume.entered).toBeTruthy();
            expect(stoichRow.weight.value).toBe(0);
            expect(stoichRow.mol.value).toBe(0);
        });

        it('volume is entered, set density, should recalculate eq', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 1;
            limitingRow.weight.value = 2;
            limitingRow.weight.entered = true;
            limitingRow.mol.value = 2;
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
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
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 1;
            limitingRow.weight.value = 2;
            limitingRow.weight.entered = true;
            limitingRow.mol.value = 2;
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
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
