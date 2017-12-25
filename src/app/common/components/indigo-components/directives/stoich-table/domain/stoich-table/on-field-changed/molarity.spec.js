var ReagentField = require('../../reagent/reagent-row');
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
            var reagentRow = new ReagentField();
            reagentRow.molWeight.value = 2;
            reagentRow.weight.value = 22;
            reagentRow.mol.value = 11;
            reagentRow.volume.value = 4;
            reagentRow.molarity.value = 3;
            reagentRow.limiting = false;
            service.addRow(reagentRow);

            service.onFieldValueChanged(reagentRow, fieldTypes.molarity);

            expect(reagentRow.mol.value).toBe(12);
            expect(reagentRow.weight.value).toBe(24);
            expect(reagentRow.limiting).toBeTruthy();
            expect(reagentRow.eq.value).toBe(1);
            expect(reagentRow.molWeight.value).toBe(2);
            expect(reagentRow.volume.value).toBe(4);
        });

        it('volume is defined, should compute mol, weight; eq should be 6,' +
            ' therefore there is limiting row', function() {
            var limitingRow = new ReagentField();
            limitingRow.molWeight.value = 5;
            limitingRow.weight.value = 10;
            limitingRow.mol.value = 2;
            limitingRow.limiting = true;
            service.getStoichTable().reactants.push(limitingRow);

            var otherRow = new ReagentField();
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
            var reagentRow = new ReagentField();
            reagentRow.molWeight.value = 2;
            reagentRow.weight.value = 30;
            reagentRow.mol.value = 15;
            reagentRow.mol.entered = true;
            reagentRow.volume.value = 5;
            reagentRow.molarity.value = 0;

            service.onFieldValueChanged(reagentRow, fieldTypes.molarity);

            expect(reagentRow.volume.value).toBe(0);
            expect(reagentRow.mol.value).toBe(15);
            expect(reagentRow.mol.entered).toBeTruthy();
        });

        it('mol is computed, volume is manually entered, mol should be 0', function() {
            var reagentRow = new ReagentField();
            reagentRow.molWeight.value = 2;
            reagentRow.weight.value = 30;
            reagentRow.mol.value = 15;
            reagentRow.volume.value = 5;
            reagentRow.volume.entered = true;
            reagentRow.molarity.value = 0;

            service.onFieldValueChanged(reagentRow, fieldTypes.molarity);

            expect(reagentRow.volume.value).toBe(5);
            expect(reagentRow.volume.entered).toBeTruthy();
            expect(reagentRow.mol.value).toBe(0);
            expect(reagentRow.weight.value).toBe(0);
        });

        it('volume is not defined, mol is defined, should compute volume', function() {
            var reagentRow = new ReagentField();
            reagentRow.molWeight.value = 2;
            reagentRow.weight.value = 30;
            reagentRow.mol.value = 15;

            reagentRow.molarity.value = 3;

            service.onFieldValueChanged(reagentRow, fieldTypes.molarity);

            expect(reagentRow.volume.value).toBe(5);
        });

        it('set molarity 0, volume is 0, should update mol from limiting row', function() {
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

            service.onFieldValueChanged(otherRow, fieldTypes.molarity);

            expect(otherRow.weight.value).toBe(2);
            expect(otherRow.mol.value).toBe(2);
        });
    });
}

module.exports = changeMolarity;
