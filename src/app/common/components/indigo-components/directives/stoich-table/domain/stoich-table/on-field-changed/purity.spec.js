var StoichRow = require('../../stoich-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changePurity() {
    describe('Change stoicPurity', function() {
        var service;

        beforeEach(function() {
            var config = {
                table: {product: [], reactants: []}
            };

            service = stoichTable(config);
        });

        it('row is limiting, weight is manually set, should compute mol and update mol in other rows', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 2;
            limitingRow.weight.value = 30;
            limitingRow.weight.entered = true;
            limitingRow.mol.value = 15;
            limitingRow.stoicPurity.value = 10;
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
            otherRow.molWeight.value = 3;
            otherRow.weight.value = 45;
            otherRow.mol.value = 15;
            service.addRow(otherRow);

            service.onFieldValueChanged(limitingRow, fieldTypes.stoicPurity);

            expect(limitingRow.mol.value).toBe(1.5);
            expect(limitingRow.weight.value).toBe(30);
            expect(otherRow.mol.value).toBe(1.5);
            expect(otherRow.weight.value).toBe(4.5);
        });

        it('complex test.' +
            '1. Change Purity of non-limiting row to 50, should update weight' +
            '2. Change Purity of limiting row to 50, should update mol' +
            '3. Change Purity of non-limiting row to 100, should update weight to original', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 21;
            limitingRow.weight.value = 21;
            limitingRow.weight.entered = true;
            limitingRow.mol.value = 1;
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
            otherRow.molWeight.value = 21;
            otherRow.weight.value = 21;
            otherRow.mol.value = 1;
            service.addRow(otherRow);

            otherRow.stoicPurity.value = 50;
            otherRow.stoicPurity.entered = true;

            service.onFieldValueChanged(otherRow, fieldTypes.stoicPurity);

            expect(otherRow.mol.value).toBe(1);
            expect(otherRow.weight.value).toBe(42);

            limitingRow.stoicPurity.value = 50;
            limitingRow.stoicPurity.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.stoicPurity);

            expect(limitingRow.mol.value).toBe(0.5);
            expect(limitingRow.weight.value).toBe(21);
            expect(otherRow.weight.value).toBe(42);
            expect(otherRow.mol.value).toBe(0.5);

            otherRow.stoicPurity.value = 100;
            otherRow.stoicPurity.entered = true;

            service.onFieldValueChanged(otherRow, fieldTypes.stoicPurity);

            expect(otherRow.mol.value).toBe(0.5);
            expect(otherRow.weight.value).toBe(21);
        });

        it('row is not limiting, should compute only weight', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 3;
            stoichRow.weight.value = 45;
            stoichRow.mol.value = 15;
            stoichRow.stoicPurity.value = 10;
            stoichRow.limiting = false;

            service.onFieldValueChanged(stoichRow, fieldTypes.stoicPurity);

            expect(stoichRow.mol.value).toBe(15);
            expect(stoichRow.weight.value).toBe(450);
        });

        it('row is not limiting, set purity to 50 and to 100, weight should be original', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 2;
            stoichRow.weight.value = 30;
            stoichRow.mol.value = 15;
            stoichRow.stoicPurity.value = 50;
            stoichRow.limiting = false;

            service.onFieldValueChanged(stoichRow, fieldTypes.stoicPurity);

            expect(stoichRow.mol.value).toBe(15);
            expect(stoichRow.weight.value).toBe(60);

            stoichRow.stoicPurity.value = 100;
            service.onFieldValueChanged(stoichRow, fieldTypes.stoicPurity);

            expect(stoichRow.mol.value).toBe(15);
            expect(stoichRow.weight.value).toBe(30);
        });

        it('row is not limiting, weight is manually set, should compute mol and update eq', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 3;
            stoichRow.weight.value = 45;
            stoichRow.weight.entered = true;
            stoichRow.mol.value = 15;
            stoichRow.stoicPurity.value = 10;
            stoichRow.limiting = false;

            service.onFieldValueChanged(stoichRow, fieldTypes.stoicPurity);

            expect(stoichRow.mol.value).toBe(1.5);
            expect(stoichRow.weight.value).toBe(45);
        });
    });
}

module.exports = changePurity;
