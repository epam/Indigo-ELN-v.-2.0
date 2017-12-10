var StoichRow = require('../../stoich-row');
var stoichTable = require('../stoich-table');

function changeDensity() {
    describe('Change density', function() {
        var service;

        beforeEach(function() {
            service = stoichTable({product: [], reactants: []});
        });

        it('weight is defined, should compute volume', function() {
            var stoichRow = new StoichRow();
            stoichRow.weight.value = 20;
            stoichRow.density.value = 4;

            service.onFieldValueChanged(stoichRow, 'density');

            expect(stoichRow.volume.value).toBe(0.005);
        });

        it('volume is defined, should compute weight', function() {
            var stoichRow = new StoichRow();
            stoichRow.volume.value = 0.005;
            stoichRow.density.value = 4;

            service.onFieldValueChanged(stoichRow, 'density');

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

            service.onFieldValueChanged(stoichRow, 'density');

            expect(stoichRow.volume.value).toBe(0.005);
            expect(stoichRow.volume.entered).toBeTruthy();
            expect(stoichRow.weight.value).toBe(0);
            expect(stoichRow.mol.value).toBe(0);
        });
    });
}

module.exports = changeDensity;
