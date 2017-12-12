var StoichRow = require('../../stoich-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changeMol() {
    describe('Change mol', function() {
        var service;

        beforeEach(function() {
            var config = {
                table: {product: [], reactants: []}
            };

            service = stoichTable(config);
        });

        it('mol weight is defined, row is not limiting; should compute weight, it should be 110', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 10;
            stoichRow.mol.value = 11;
            stoichRow.limiting = false;

            service.onFieldValueChanged(stoichRow, fieldTypes.mol);

            expect(stoichRow.weight.value).toBe(110);
        });

        it('mol weight is defined, row is not limiting; mol is removed or 0, weight should be 0', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 10;
            stoichRow.mol.value = 0;
            stoichRow.limiting = false;

            service.onFieldValueChanged(stoichRow, fieldTypes.mol);

            expect(stoichRow.weight.value).toBe(0);
        });

        it('mol weight is defined, row is limiting; should compute weight and update mol in other rows', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 10;
            limitingRow.mol.value = 11;
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
            otherRow.molWeight.value = 2;
            otherRow.weight.value = 32;
            service.addRow(otherRow);

            service.onFieldValueChanged(limitingRow, fieldTypes.mol);

            expect(limitingRow.weight.value).toBe(110);
            expect(otherRow.mol.value).toBe(11);
            expect(otherRow.weight.value).toBe(22);
        });

        it('mol weight is defined, row is limiting, mol is removed or 0; weight of limiting row should be 0 and' +
            ' mol of other rows are the same', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 22;
            limitingRow.mol.value = 0;
            service.getStoichTable().reactants.push(limitingRow);

            var otherRow = new StoichRow();
            otherRow.molWeight.value = 1;
            otherRow.mol.value = 10;
            otherRow.weight.value = 10;
            service.getStoichTable().reactants.push(otherRow);

            service.onFieldValueChanged(limitingRow, fieldTypes.mol);

            expect(limitingRow.weight.value).toBe(0);
            expect(otherRow.mol.value).toBe(10);
            expect(otherRow.weight.value).toBe(10);
        });

        it('molarity is entered, volume is computed, mol set to 0, should reset volume and weight', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 10;
            stoichRow.weight.value = 22;
            stoichRow.volume.value = 2;
            stoichRow.molarity.value = 2;

            stoichRow.mol.value = 0;

            service.onFieldValueChanged(stoichRow, fieldTypes.mol);

            expect(stoichRow.weight.value).toBe(0);
            expect(stoichRow.volume.value).toBe(0);
        });

        it('mol weight is not defined, but weight is defined, should compute mol weight', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 0;
            stoichRow.mol.value = 11;
            stoichRow.weight.value = 11;

            service.onFieldValueChanged(stoichRow, fieldTypes.mol);

            expect(stoichRow.molWeight.value).toBe(1);
        });

        it('mol weight, volume, weight, molarity, eq are defined, should recompute volume, weight and eq', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 2;
            limitingRow.weight.value = 40;
            limitingRow.volume.value = 10;
            limitingRow.molarity.value = 2;
            service.addRow(limitingRow);

            limitingRow.mol.value = 10;

            service.onFieldValueChanged(limitingRow, fieldTypes.mol);

            expect(limitingRow.weight.value).toBe(20);
            expect(limitingRow.volume.value).toBe(5);
            expect(limitingRow.mol.value).toBe(10);
            expect(limitingRow.molarity.value).toBe(2);
            expect(limitingRow.eq.value).toBe(1);
            expect(limitingRow.molWeight.value).toBe(2);
        });

        // TODO: first set purity and then mol (should discuss with Evgenia)
        xit('mol weight, purity are defined, should recompute mol, compute weight', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 2;
            limitingRow.mol.value = 10;
            limitingRow.mol.entered = true;
            limitingRow.stoicPurity.value = 50;
            limitingRow.stoicPurity.entered = true;
            service.addRow(limitingRow);

            service.onFieldValueChanged(limitingRow, fieldTypes.mol);

            expect(limitingRow.weight.value).toBe(20);
            expect(limitingRow.mol.value).toBe(5);
            expect(limitingRow.eq.value).toBe(1);
        });
    });
}

module.exports = changeMol;
