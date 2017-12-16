var StoichRow = require('../../stoich-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changeWeight() {
    describe('Change weight', function() {
        var service;

        beforeEach(function() {
            var config = {
                table: {product: [], reactants: []}
            };

            service = stoichTable(config);
        });

        it('mol weight is defined; should compute mol, it should be 1', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 23;
            stoichRow.weight.value = 23;
            service.addRow(stoichRow);

            service.onFieldValueChanged(stoichRow, fieldTypes.weight);

            expect(stoichRow.mol.value).toBe(1);
        });

        it('mol weight is defined, row is limiting; should compute mol and change mol in other rows', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 10;
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
            otherRow.molWeight.value = 5;
            service.addRow(otherRow);

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(limitingRow.mol.value).toBe(1);
            expect(otherRow.weight.value).toBe(5);
            expect(otherRow.mol.value).toBe(1);
        });

        it('mol weight is not defined, but mol is defined; should compute mol weight', function() {
            var stoichRow = new StoichRow();
            stoichRow.weight.value = 20;
            stoichRow.mol.value = 10;

            service.onFieldValueChanged(stoichRow, fieldTypes.weight);

            expect(stoichRow.molWeight.value).toBe(2);
        });

        it('mol weight is defined, row is limiting, weight is removed or 0; mol of limiting row should be 0 and' +
            ' mol of other rows are the same', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 0;
            limitingRow.mol.value = 10;
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
            otherRow.molWeight.value = 1;
            otherRow.mol.value = 10;
            otherRow.weight.value = 10;
            service.addRow(otherRow);

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(limitingRow.mol.value).toBe(0);
            expect(limitingRow.eq.value).toBe(1);
            expect(otherRow.mol.value).toBe(10);
        });

        it('mol weight is defined, row is not limiting, weight is removed or 0;' +
            ' mol and eq should be default', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 10;
            stoichRow.weight.value = 0;
            stoichRow.mol.value = 10;

            service.onFieldValueChanged(stoichRow, fieldTypes.weight);

            expect(stoichRow.mol.value).toBe(0);
            expect(stoichRow.eq.value).toBe(1);
        });

        it('density is defined, should compute volume', function() {
            var stoichRow = new StoichRow();
            stoichRow.density.value = 10;
            stoichRow.weight.value = 2;

            service.onFieldValueChanged(stoichRow, fieldTypes.weight);

            expect(stoichRow.volume.value).toBe(0.0002);
        });

        it('mol weight, volume, mol, molarity, eq are defined, should recompute volume, mol and eq', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 2;
            limitingRow.mol.value = 20;
            limitingRow.volume.value = 4;
            limitingRow.molarity.value = 5;
            service.addRow(limitingRow);

            limitingRow.weight.value = 10;
            limitingRow.weight.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(limitingRow.mol.value).toBe(5);
            expect(limitingRow.volume.value).toBe(1);
            expect(limitingRow.molarity.value).toBe(5);
            expect(limitingRow.weight.value).toBe(10);
            expect(limitingRow.eq.value).toBe(1);
            expect(limitingRow.molWeight.value).toBe(2);
        });

        it('mol weight, volume, mol, molarity, eq are defined, remove weight, should reset volume and mol', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 2;
            limitingRow.mol.value = 20;
            limitingRow.volume.value = 4;
            limitingRow.molarity.value = 5;
            service.addRow(limitingRow);

            limitingRow.weight.value = 0;
            limitingRow.weight.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(limitingRow.mol.value).toBe(0);
            expect(limitingRow.volume.value).toBe(0);
            expect(limitingRow.molarity.value).toBe(5);
            expect(limitingRow.weight.value).toBe(0);
            expect(limitingRow.eq.value).toBe(1);
            expect(limitingRow.molWeight.value).toBe(2);
        });

        it('mol weight, volume, mol, molarity, eq are defined, correct weight, should compute mol, volume', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 2;
            limitingRow.mol.value = 20;
            limitingRow.volume.value = 4;
            limitingRow.volume.entered = true;
            limitingRow.molarity.value = 5;
            limitingRow.molarity.entered = true;
            service.addRow(limitingRow);

            limitingRow.weight.value = 12;
            limitingRow.weight.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(limitingRow.mol.value).toBe(6);
            expect(limitingRow.volume.value).toBe(1.2);
            expect(limitingRow.volume.entered).toBeFalsy();
            expect(limitingRow.molarity.value).toBe(5);
            expect(limitingRow.molarity.entered).toBeTruthy();
            expect(limitingRow.weight.value).toBe(12);
            expect(limitingRow.weight.entered).toBeTruthy();
            expect(limitingRow.eq.value).toBe(1);
            expect(limitingRow.molWeight.value).toBe(2);
        });

        it('row is limiting, non limiting row has manually entered mol, ' +
            'should not update this row with new mol', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 3;
            limitingRow.weight.value = 15;
            limitingRow.weight.entered = true;
            limitingRow.mol.value = 5;
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
            otherRow.molWeight.value = 10;
            service.addRow(otherRow);
            otherRow.mol.value = 10;
            otherRow.mol.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(otherRow.mol.value).toBe(10);
            expect(otherRow.mol.entered).toBeTruthy();
            expect(otherRow.eq.value).toBe(2);
        });

        it('row is limiting, eq is manually entered, should not reset eq', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 3;
            limitingRow.eq.value = 2;
            limitingRow.eq.entered = true;
            service.addRow(limitingRow);

            limitingRow.weight.value = 15;
            limitingRow.weight.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(limitingRow.mol.value).toBe(5);
            expect(limitingRow.eq.value).toBe(2);
            expect(limitingRow.eq.entered).toBeTruthy();
        });

        it('row is not limiting, eq is manually entered, should not reset eq', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 3;
            limitingRow.weight.value = 15;
            limitingRow.weight.entered = true;
            limitingRow.mol.value = 5;
            limitingRow.eq.value = 2;
            limitingRow.eq.entered = true;
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
            otherRow.molWeight.value = 10;
            service.addRow(otherRow);
            otherRow.weight.value = 10;
            otherRow.weight.entered = true;

            service.onFieldValueChanged(otherRow, fieldTypes.weight);

            expect(otherRow.mol.value).toBe(1);
            expect(otherRow.eq.value).toBe(0.4);
        });
    });
}

module.exports = changeWeight;
