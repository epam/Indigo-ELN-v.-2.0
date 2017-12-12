var StoichRow = require('../../stoich-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changeVolume() {
    describe('Change volume', function() {
        var service;

        beforeEach(function() {
            var config = {
                table: {product: [], reactants: []}
            };

            service = stoichTable(config);
        });

        it('should set mol, weight to 0 and limiting to false, set limiting to next line with solvent role and' +
            ' eq=1, should not update rows with new limiting mol', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 22;
            limitingRow.mol.value = 11;
            limitingRow.limiting = true;
            limitingRow.volume.value = 4;
            service.getStoichTable().reactants.push(limitingRow);

            var secondRow = new StoichRow();
            secondRow.molWeight.value = 1;
            secondRow.mol.value = 10;
            secondRow.weight.value = 10;
            secondRow.eq.value = 2;
            service.getStoichTable().reactants.push(secondRow);

            var thirdRow = new StoichRow();
            thirdRow.molWeight.value = 1;
            thirdRow.mol.value = 5;
            thirdRow.weight.value = 5;
            service.getStoichTable().reactants.push(thirdRow);

            service.onFieldValueChanged(limitingRow, fieldTypes.volume);

            expect(limitingRow.weight.value).toBe(0);
            expect(limitingRow.mol.value).toBe(0);
            expect(limitingRow.limiting).toBeFalsy();
            expect(limitingRow.molWeight.value).toBe(10);

            expect(secondRow.limiting).toBeFalsy();
            expect(secondRow.weight.value).toBe(10);
            expect(secondRow.mol.value).toBe(10);

            expect(thirdRow.limiting).toBeTruthy();
            expect(thirdRow.mol.value).toBe(5);
            expect(thirdRow.weight.value).toBe(5);
        });

        it('row is not limiting; should set mol, weight to 0', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 10;
            stoichRow.weight.value = 22;
            stoichRow.mol.value = 11;
            stoichRow.limiting = false;

            stoichRow.volume.value = 4;

            service.onFieldValueChanged(stoichRow, fieldTypes.volume);

            expect(stoichRow.weight.value).toBe(0);
            expect(stoichRow.mol.value).toBe(0);
            expect(stoichRow.limiting).toBeFalsy();
            expect(stoichRow.molWeight.value).toBe(10);
        });

        it('row is not limiting; weight, mol are 0, set volume to 0 or remove, should set mol from limiting row', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 2;
            limitingRow.weight.value = 22;
            limitingRow.mol.value = 11;
            service.addRow(limitingRow);

            var secondRow = new StoichRow();
            secondRow.molWeight.value = 3;
            secondRow.mol.value = 0;
            secondRow.weight.value = 0;
            service.addRow(secondRow);

            service.onFieldValueChanged(secondRow, fieldTypes.volume);

            expect(limitingRow.limiting).toBeTruthy();
            expect(limitingRow.mol.value).toBe(11);

            expect(secondRow.limiting).toBeFalsy();
            expect(secondRow.mol.value).toBe(11);
            expect(secondRow.weight.value).toBe(33);
        });

        it('molarity, molWeight are defined, should compute mol, weight and eq', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 2;
            stoichRow.molarity.value = 4;

            stoichRow.volume.value = 5;

            service.onFieldValueChanged(stoichRow, fieldTypes.volume);

            expect(stoichRow.molarity.value).toBe(4);
            expect(stoichRow.volume.value).toBe(5);
            expect(stoichRow.mol.value).toBe(20);
            expect(stoichRow.weight.value).toBe(40);
            expect(stoichRow.eq.value).toBe(1);
        });

        it('molarity, molWeight, mol are defined, set volume to 0, should remove mol, weight', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 2;
            stoichRow.molarity.value = 4;
            stoichRow.mol.value = 20;
            stoichRow.weight.value = 40;

            stoichRow.volume.value = 0;

            service.onFieldValueChanged(stoichRow, fieldTypes.volume);

            expect(stoichRow.molarity.value).toBe(4);
            expect(stoichRow.volume.value).toBe(0);
            expect(stoichRow.mol.value).toBe(0);
            expect(stoichRow.weight.value).toBe(0);
            expect(stoichRow.eq.value).toBe(1);
        });

        it('density is defined, should compute weight', function() {
            var stoichRow = new StoichRow();
            stoichRow.volume.value = 5;
            stoichRow.density.value = 20;

            service.onFieldValueChanged(stoichRow, fieldTypes.volume);

            expect(stoichRow.weight.value).toBe(100000);
        });

        it('density is defined, should compute weight and mol', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 1000;
            stoichRow.volume.value = 5;
            stoichRow.density.value = 20;

            service.onFieldValueChanged(stoichRow, fieldTypes.volume);

            expect(stoichRow.mol.value).toBe(100);
            expect(stoichRow.weight.value).toBe(100000);
        });

        it('density is defined, set volume 0, should set weight 0', function() {
            var stoichRow = new StoichRow();
            stoichRow.volume.value = 0;
            stoichRow.weight.value = 3;
            stoichRow.density.value = 20;

            service.onFieldValueChanged(stoichRow, fieldTypes.volume);

            expect(stoichRow.weight.value).toBe(0);
        });
    });
}

module.exports = changeVolume;
