var ReagentField = require('../../reagent/reagent-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changeMol() {
    describe('Change mol', function() {
        var service;
        var limitingRow;
        var nonLimitingRow;

        beforeEach(function() {
            limitingRow = new ReagentField();
            nonLimitingRow = new ReagentField();

            var config = {
                table: {product: [], reactants: []}
            };

            service = stoichTable(config);
        });

        it('mol weight is defined, row is not limiting; should compute weight, it should be 110', function() {
            nonLimitingRow.molWeight.value = 10;
            nonLimitingRow.mol.value = 11;
            nonLimitingRow.mol.entered = true;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.mol);

            expect(nonLimitingRow.weight.value).toBe(110);
        });

        it('mol weight is defined, row is not limiting; mol is removed or 0, weight should be 0', function() {
            nonLimitingRow.molWeight.value = 10;
            nonLimitingRow.mol.value = 0;
            nonLimitingRow.mol.entered = true;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.mol);

            expect(nonLimitingRow.weight.value).toBe(0);
            expect(nonLimitingRow.mol.entered).toBeFalsy();
        });

        it('mol weight is defined, row is limiting; should compute weight and update mol in other rows', function() {
            limitingRow.molWeight.value = 10;
            limitingRow.mol.value = 11;

            nonLimitingRow.molWeight.value = 2;
            nonLimitingRow.weight.value = 32;

            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            service.onFieldValueChanged(limitingRow, fieldTypes.mol);

            expect(limitingRow.weight.value).toBe(110);
            expect(nonLimitingRow.mol.value).toBe(11);
            expect(nonLimitingRow.weight.value).toBe(22);
        });

        it('mol weight is defined, row is limiting, mol is removed or 0; weight of limiting row should be 0 and' +
            ' mol of other rows are the same', function() {
            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 22;

            nonLimitingRow.molWeight.value = 1;
            nonLimitingRow.mol.value = 10;
            nonLimitingRow.weight.value = 10;

            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            limitingRow.mol.value = 0;
            limitingRow.mol.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.mol);

            expect(limitingRow.weight.value).toBe(0);
            expect(nonLimitingRow.mol.value).toBe(10);
            expect(nonLimitingRow.weight.value).toBe(10);
        });

        it('molarity is entered, volume is computed, mol set to 0, should reset volume and weight', function() {
            nonLimitingRow.molWeight.value = 10;
            nonLimitingRow.weight.value = 22;
            nonLimitingRow.volume.value = 2;
            nonLimitingRow.molarity.value = 2;
            nonLimitingRow.molarity.entered = true;

            nonLimitingRow.mol.value = 0;
            nonLimitingRow.mol.entered = true;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.mol);

            expect(nonLimitingRow.weight.value).toBe(0);
            expect(nonLimitingRow.volume.value).toBe(0);
        });

        it('mol weight is not defined, but weight is defined, should compute mol weight', function() {
            nonLimitingRow.molWeight.value = 0;
            nonLimitingRow.weight.value = 11;
            nonLimitingRow.mol.value = 11;
            nonLimitingRow.mol.entered = true;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.mol);

            expect(nonLimitingRow.molWeight.value).toBe(1);
        });

        it('mol weight, volume, weight, molarity, eq are defined, should recompute volume, weight and eq', function() {
            limitingRow.molWeight.value = 2;
            limitingRow.weight.value = 40;
            limitingRow.volume.value = 10;
            limitingRow.molarity.value = 2;
            limitingRow.molarity.entered = true;
            service.addRow(limitingRow);

            limitingRow.mol.value = 10;
            limitingRow.mol.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.mol);

            expect(limitingRow.weight.value).toBe(20);
            expect(limitingRow.volume.value).toBe(5);
            expect(limitingRow.mol.value).toBe(10);
            expect(limitingRow.molarity.value).toBe(2);
            expect(limitingRow.eq.value).toBe(1);
            expect(limitingRow.molWeight.value).toBe(2);
        });

        it('mol weight, purity are defined, should compute weight', function() {
            limitingRow.molWeight.value = 2;
            limitingRow.stoicPurity.value = 50;
            limitingRow.stoicPurity.entered = true;
            service.addRow(limitingRow);

            limitingRow.mol.value = 10;
            limitingRow.mol.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.mol);

            expect(limitingRow.weight.value).toBe(40);
            expect(limitingRow.mol.value).toBe(10);
        });
    });
}

module.exports = changeMol;
