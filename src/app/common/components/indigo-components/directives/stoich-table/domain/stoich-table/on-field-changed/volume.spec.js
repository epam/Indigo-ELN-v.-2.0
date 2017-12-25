var ReagentField = require('../../reagent/reagent-row');
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

        it('should set mol, weight to 0, limiting to false and eq to 1, set limiting to next line with' +
            ' reactant/reaction role and eq=1, should not update rows with new limiting mol', function() {
            var limitingRow = new ReagentField();
            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 220;
            limitingRow.weight.entered = true;
            limitingRow.mol.value = 22;
            limitingRow.eq.value = 2;
            limitingRow.volume.value = 4;
            limitingRow.volume.entered = true;

            var secondRow = new ReagentField();
            secondRow.molWeight.value = 1;

            service.addRow(limitingRow);
            service.addRow(secondRow);

            service.onFieldValueChanged(limitingRow, fieldTypes.volume);

            expect(limitingRow.weight.value).toBe(0);
            expect(limitingRow.mol.value).toBe(0);
            expect(limitingRow.eq.value).toBe(1);
            expect(limitingRow.limiting).toBeFalsy();
            expect(limitingRow.molWeight.value).toBe(10);

            expect(secondRow.limiting).toBeTruthy();
            expect(secondRow.weight.value).toBe(11);
            expect(secondRow.mol.value).toBe(11);
        });

        it('row is not limiting; should set mol, weight to 0', function() {
            var reagentRow = new ReagentField();
            reagentRow.molWeight.value = 10;
            reagentRow.weight.value = 22;
            reagentRow.mol.value = 11;
            reagentRow.limiting = false;

            reagentRow.volume.value = 4;

            service.onFieldValueChanged(reagentRow, fieldTypes.volume);

            expect(reagentRow.weight.value).toBe(0);
            expect(reagentRow.mol.value).toBe(0);
            expect(reagentRow.limiting).toBeFalsy();
            expect(reagentRow.molWeight.value).toBe(10);
        });

        it('row is not limiting; weight, mol are 0, set volume to 0 or remove,' +
            ' should set mol from limiting row', function() {
            var limitingRow = new ReagentField();
            limitingRow.molWeight.value = 2;
            limitingRow.weight.value = 22;
            limitingRow.mol.value = 11;
            service.addRow(limitingRow);

            var secondRow = new ReagentField();
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

        it('row is limiting; volume is entered for non limiting row, should not update mol of this row', function() {
            var limitingRow = new ReagentField();
            limitingRow.molWeight.value = 2;
            limitingRow.weight.value = 22;
            limitingRow.mol.value = 11;
            service.addRow(limitingRow);

            var secondRow = new ReagentField();
            secondRow.molWeight.value = 3;
            secondRow.volume.value = 3;
            secondRow.volume.entered = true;
            service.addRow(secondRow);

            service.onFieldValueChanged(secondRow, fieldTypes.volume);

            expect(limitingRow.limiting).toBeTruthy();
            expect(limitingRow.mol.value).toBe(11);

            expect(secondRow.limiting).toBeFalsy();
            expect(secondRow.mol.value).toBe(0);
            expect(secondRow.weight.value).toBe(0);
        });

        it('molarity, molWeight are defined, should compute mol, weight and eq', function() {
            var reagentRow = new ReagentField();
            reagentRow.molWeight.value = 2;
            reagentRow.molarity.value = 4;

            reagentRow.volume.value = 5;

            service.onFieldValueChanged(reagentRow, fieldTypes.volume);

            expect(reagentRow.molarity.value).toBe(4);
            expect(reagentRow.volume.value).toBe(5);
            expect(reagentRow.mol.value).toBe(20);
            expect(reagentRow.weight.value).toBe(40);
            expect(reagentRow.eq.value).toBe(1);
        });

        it('molarity, molWeight, mol are defined, set volume to 0, should remove mol, weight', function() {
            var reagentRow = new ReagentField();
            reagentRow.molWeight.value = 2;
            reagentRow.molarity.value = 4;
            reagentRow.mol.value = 20;
            reagentRow.weight.value = 40;

            reagentRow.volume.value = 0;

            service.onFieldValueChanged(reagentRow, fieldTypes.volume);

            expect(reagentRow.molarity.value).toBe(4);
            expect(reagentRow.volume.value).toBe(0);
            expect(reagentRow.mol.value).toBe(0);
            expect(reagentRow.weight.value).toBe(0);
            expect(reagentRow.eq.value).toBe(1);
        });

        it('density is defined, should compute weight', function() {
            var reagentRow = new ReagentField();
            reagentRow.volume.value = 5;
            reagentRow.density.value = 20;

            service.onFieldValueChanged(reagentRow, fieldTypes.volume);

            expect(reagentRow.weight.value).toBe(100000);
        });

        it('density is defined, should compute weight and mol', function() {
            var reagentRow = new ReagentField();
            reagentRow.molWeight.value = 1000;
            reagentRow.volume.value = 5;
            reagentRow.density.value = 20;

            service.onFieldValueChanged(reagentRow, fieldTypes.volume);

            expect(reagentRow.mol.value).toBe(100);
            expect(reagentRow.weight.value).toBe(100000);
        });

        it('density is defined, set volume 0, should set weight 0', function() {
            var reagentRow = new ReagentField();
            reagentRow.volume.value = 0;
            reagentRow.weight.value = 3;
            reagentRow.density.value = 20;

            service.onFieldValueChanged(reagentRow, fieldTypes.volume);

            expect(reagentRow.weight.value).toBe(0);
        });
    });
}

module.exports = changeVolume;
