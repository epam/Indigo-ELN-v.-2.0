var StoichRow = require('../../stoich-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changeMolarity() {
    describe('Change molarity', function() {
        var service;

        beforeEach(function() {
            var config = {
                table: {product: [], reactants: []}
            };

            service = stoichTable(config);
        });

        it('volume is defined, should compute mol, weight; eq should be default, therefore there is not limiting' +
            ' row', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 2;
            stoichRow.weight.value = 22;
            stoichRow.mol.value = 11;
            stoichRow.volume.value = 4;
            stoichRow.molarity.value = 3;
            stoichRow.limiting = false;
            service.addRow(stoichRow);

            service.onFieldValueChanged(stoichRow, fieldTypes.molarity);

            expect(stoichRow.mol.value).toBe(12);
            expect(stoichRow.weight.value).toBe(24);
            expect(stoichRow.limiting).toBeTruthy();
            expect(stoichRow.eq.value).toBe(1);
            expect(stoichRow.molWeight.value).toBe(2);
            expect(stoichRow.volume.value).toBe(4);
        });

        it('volume is defined, should compute mol, weight; eq should be 6, therefore there is limiting row', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 5;
            limitingRow.weight.value = 10;
            limitingRow.mol.value = 2;
            limitingRow.limiting = true;
            service.getStoichTable().reactants.push(limitingRow);

            var otherRow = new StoichRow();
            otherRow.molWeight.value = 2;
            otherRow.volume.value = 4;
            otherRow.molarity.value = 3;
            service.getStoichTable().reactants.push(otherRow);

            service.onFieldValueChanged(otherRow, fieldTypes.molarity);

            expect(otherRow.mol.value).toBe(12);
            expect(otherRow.weight.value).toBe(24);
            expect(otherRow.limiting).toBeFalsy();
            expect(otherRow.eq.value).toBe(6);
            expect(otherRow.molWeight.value).toBe(2);
            expect(otherRow.volume.value).toBe(4);
        });

        it('volume is computed, mol is manually entered, volume should be 0', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 2;
            stoichRow.weight.value = 30;
            stoichRow.mol.value = 15;
            stoichRow.mol.entered = true;
            stoichRow.volume.value = 5;
            stoichRow.molarity.value = 0;

            service.onFieldValueChanged(stoichRow, fieldTypes.molarity);

            expect(stoichRow.volume.value).toBe(0);
            expect(stoichRow.mol.value).toBe(15);
            expect(stoichRow.mol.entered).toBeTruthy();
        });

        it('mol is computed, volume is manually entered, mol should be 0', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 2;
            stoichRow.weight.value = 30;
            stoichRow.mol.value = 15;
            stoichRow.volume.value = 5;
            stoichRow.volume.entered = true;
            stoichRow.molarity.value = 0;

            service.onFieldValueChanged(stoichRow, fieldTypes.molarity);

            expect(stoichRow.volume.value).toBe(5);
            expect(stoichRow.volume.entered).toBeTruthy();
            expect(stoichRow.mol.value).toBe(0);
            expect(stoichRow.weight.value).toBe(0);
        });

        it('volume is not defined, mol is defined, should compute volume', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 2;
            stoichRow.weight.value = 30;
            stoichRow.mol.value = 15;

            stoichRow.molarity.value = 3;

            service.onFieldValueChanged(stoichRow, fieldTypes.molarity);

            expect(stoichRow.volume.value).toBe(5);
        });
    });
}

module.exports = changeMolarity;
