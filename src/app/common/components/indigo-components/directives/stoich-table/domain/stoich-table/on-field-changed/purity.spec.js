var StoichRow = require('../../stoich-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changePurity() {
    describe('Change stoicPurity', function() {
        var service;
        var limitingRow;
        var nonLimitingRow;

        beforeEach(function() {
            var config = {
                table: {product: [], reactants: []}
            };

            service = stoichTable(config);

            limitingRow = new StoichRow();
            limitingRow.molWeight.value = 2;
            limitingRow.weight.value = 30;
            limitingRow.mol.value = 15;

            nonLimitingRow = new StoichRow();
            nonLimitingRow.molWeight.value = 3;
            nonLimitingRow.weight.value = 45;
            nonLimitingRow.mol.value = 15;
        });

        it('row is limiting, weight is manually set, should compute mol and update mol in other rows', function() {
            limitingRow.weight.entered = true;
            limitingRow.stoicPurity.value = 10;
            limitingRow.stoicPurity.entered = true;
            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            service.onFieldValueChanged(limitingRow, fieldTypes.stoicPurity);

            expect(limitingRow.mol.value).toBe(1.5);
            expect(limitingRow.weight.value).toBe(30);
            expect(nonLimitingRow.mol.value).toBe(1.5);
            expect(nonLimitingRow.weight.value).toBe(4.5);
        });

        it('row is limiting, mol is manually set, should compute only weight', function() {
            limitingRow.mol.entered = true;
            limitingRow.stoicPurity.value = 10;
            limitingRow.stoicPurity.entered = true;
            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            service.onFieldValueChanged(limitingRow, fieldTypes.stoicPurity);

            expect(limitingRow.mol.value).toBe(15);
            expect(limitingRow.weight.value).toBe(300);
            expect(nonLimitingRow.mol.value).toBe(15);
            expect(nonLimitingRow.weight.value).toBe(45);
        });

        it('row is limiting, mol is manually set, should compute weight', function() {
            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            limitingRow.mol.entered = true;
            limitingRow.stoicPurity.entered = true;
            limitingRow.stoicPurity.value = 50;

            service.onFieldValueChanged(limitingRow, fieldTypes.stoicPurity);

            expect(limitingRow.mol.value).toBe(15);
            expect(limitingRow.mol.entered).toBeTruthy();
            expect(limitingRow.weight.value).toBe(60);
        });

        it('row is not limiting, mol and weight are not entered, should compute only weight', function() {
            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            nonLimitingRow.stoicPurity.entered = true;
            nonLimitingRow.stoicPurity.value = 10;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.stoicPurity);

            expect(nonLimitingRow.mol.value).toBe(15);
            expect(nonLimitingRow.weight.value).toBe(450);
        });

        it('row is not limiting, set purity to 50 and then to 100, weight should be original', function() {
            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            nonLimitingRow.stoicPurity.entered = true;
            nonLimitingRow.stoicPurity.value = 50;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.stoicPurity);

            expect(nonLimitingRow.mol.value).toBe(15);
            expect(nonLimitingRow.weight.value).toBe(90);

            nonLimitingRow.stoicPurity.value = 100;
            service.onFieldValueChanged(nonLimitingRow, fieldTypes.stoicPurity);

            expect(nonLimitingRow.mol.value).toBe(15);
            expect(nonLimitingRow.weight.value).toBe(45);
        });

        it('row is not limiting, weight is manually set, should compute mol and update eq', function() {
            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            nonLimitingRow.weight.entered = true;
            nonLimitingRow.stoicPurity.entered = true;
            nonLimitingRow.stoicPurity.value = 10;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.stoicPurity);

            expect(nonLimitingRow.mol.value).toBe(1.5);
            expect(nonLimitingRow.weight.value).toBe(45);
            expect(nonLimitingRow.eq.value).toBe(0.1);
        });

        it('row is not limiting, mol is manually set, should compute weight', function() {
            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            nonLimitingRow.mol.entered = true;
            nonLimitingRow.stoicPurity.entered = true;
            nonLimitingRow.stoicPurity.value = 50;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.stoicPurity);

            expect(nonLimitingRow.mol.value).toBe(15);
            expect(nonLimitingRow.mol.entered).toBeTruthy();
            expect(nonLimitingRow.weight.value).toBe(90);
        });

        it('complex test ' +
            '1. Change Purity of non-limiting row to 50, should update weight' +
            '2. Change Purity of limiting row to 50, should update mol' +
            '3. Change Purity of non-limiting row to 100, should update', function() {
            limitingRow.weight.entered = true;

            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            nonLimitingRow.stoicPurity.value = 50;
            nonLimitingRow.stoicPurity.entered = true;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.stoicPurity);

            expect(nonLimitingRow.mol.value).toBe(15);
            expect(nonLimitingRow.weight.value).toBe(90);

            limitingRow.stoicPurity.value = 50;
            limitingRow.stoicPurity.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.stoicPurity);

            expect(limitingRow.mol.value).toBe(7.5);
            expect(limitingRow.weight.value).toBe(30);
            expect(nonLimitingRow.mol.value).toBe(7.5);
            expect(nonLimitingRow.weight.value).toBe(45);

            nonLimitingRow.stoicPurity.value = 100;
            nonLimitingRow.stoicPurity.entered = true;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.stoicPurity);

            expect(nonLimitingRow.mol.value).toBe(7.5);
            expect(nonLimitingRow.weight.value).toBe(22.5);
        });
    });
}

module.exports = changePurity;
