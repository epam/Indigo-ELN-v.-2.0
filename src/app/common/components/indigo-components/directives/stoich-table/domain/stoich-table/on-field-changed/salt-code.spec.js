var ReagentField = require('../../reagent/reagent-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changeSaltCode() {
    describe('Change salt code/eq', function() {
        var service;

        beforeEach(function() {
            var config = {
                table: {product: [], reactants: []}
            };

            service = stoichTable(config);
        });

        it('row is limiting, should compute mol, then weight and update mol in other rows', function() {
            var limitingRow = new ReagentField();
            limitingRow.molWeight.value = 3;
            limitingRow.molWeight.originalValue = 3;
            limitingRow.weight.value = 45;
            limitingRow.mol.value = 15;
            limitingRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
            limitingRow.saltEq.value = 12;
            service.addRow(limitingRow);

            var otherRow = new ReagentField();
            service.addRow(otherRow);

            service.onFieldValueChanged(limitingRow, fieldTypes.saltEq);

            expect(limitingRow.molWeight.value).toBe(15);
            expect(limitingRow.mol.value).toBe(3);
            expect(otherRow.mol.value).toBe(3);
        });

        it('row is not limiting, should compute weight', function() {
            var reagentRow = new ReagentField();
            reagentRow.molWeight.value = 3;
            reagentRow.molWeight.originalValue = 3;
            reagentRow.weight.value = 45;
            reagentRow.mol.value = 15;
            reagentRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
            reagentRow.saltEq.value = 12;
            reagentRow.limiting = false;

            service.onFieldValueChanged(reagentRow, fieldTypes.saltEq);

            expect(reagentRow.molWeight.value).toBe(15);
            expect(reagentRow.weight.value).toBe(225);
            expect(reagentRow.mol.value).toBe(15);
        });

        it('row is not limiting, should set original mol weight', function() {
            var reagentRow = new ReagentField();
            reagentRow.molWeight.value = 3;
            reagentRow.molWeight.originalValue = 3;
            reagentRow.weight.value = 45;
            reagentRow.mol.value = 15;
            reagentRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
            reagentRow.saltEq.value = 12;
            reagentRow.limiting = false;

            service.onFieldValueChanged(reagentRow, fieldTypes.saltEq);

            expect(reagentRow.molWeight.value).toBe(15);
            expect(reagentRow.weight.value).toBe(225);
            expect(reagentRow.mol.value).toBe(15);

            reagentRow.saltCode = {name: '00 - Parent Structure', value: '0', regValue: '00', weight: 0};

            service.onFieldValueChanged(reagentRow, fieldTypes.saltEq);

            expect(reagentRow.molWeight.value).toBe(3);
            expect(reagentRow.weight.value).toBe(45);
            expect(reagentRow.mol.value).toBe(15);
        });
    });
}

module.exports = changeSaltCode;
