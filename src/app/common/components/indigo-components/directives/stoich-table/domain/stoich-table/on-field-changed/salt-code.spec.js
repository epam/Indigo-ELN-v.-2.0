var StoichRow = require('../../stoich-row');
var stoichTable = require('../stoich-table');

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
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 3;
            limitingRow.molWeight.originalValue = 3;
            limitingRow.weight.value = 45;
            limitingRow.mol.value = 15;
            limitingRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
            limitingRow.saltEq.value = 12;
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
            service.addRow(otherRow);

            service.onFieldValueChanged(limitingRow, 'saltEq');

            expect(limitingRow.molWeight.value).toBe(15);
            expect(limitingRow.mol.value).toBe(3);
            expect(otherRow.mol.value).toBe(3);
        });

        it('row is not limiting, should compute weight', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 3;
            stoichRow.molWeight.originalValue = 3;
            stoichRow.weight.value = 45;
            stoichRow.mol.value = 15;
            stoichRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
            stoichRow.saltEq.value = 12;
            stoichRow.limiting = false;

            service.onFieldValueChanged(stoichRow, 'saltEq');

            expect(stoichRow.molWeight.value).toBe(15);
            expect(stoichRow.weight.value).toBe(225);
            expect(stoichRow.mol.value).toBe(15);
        });

        it('row is not limiting, should set original mol weight', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 3;
            stoichRow.molWeight.originalValue = 3;
            stoichRow.weight.value = 45;
            stoichRow.mol.value = 15;
            stoichRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
            stoichRow.saltEq.value = 12;
            stoichRow.limiting = false;

            service.onFieldValueChanged(stoichRow, 'saltEq');

            expect(stoichRow.molWeight.value).toBe(15);
            expect(stoichRow.weight.value).toBe(225);
            expect(stoichRow.mol.value).toBe(15);

            stoichRow.saltCode = {name: '00 - Parent Structure', value: '0', regValue: '00', weight: 0};

            service.onFieldValueChanged(stoichRow, 'saltEq');

            expect(stoichRow.molWeight.value).toBe(3);
            expect(stoichRow.weight.value).toBe(45);
            expect(stoichRow.mol.value).toBe(15);
        });
    });
}

module.exports = changeSaltCode;
