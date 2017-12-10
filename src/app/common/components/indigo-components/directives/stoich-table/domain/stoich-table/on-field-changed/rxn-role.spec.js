var StoichRow = require('../../stoich-row');
var stoichTable = require('../stoich-table');

function changeRxnRole() {
    describe('Change rxnRole', function() {
        var service;

        beforeEach(function() {
            service = stoichTable({product: [], reactants: []});
        });

        it('set solvent role, should reset and disable weight, mol, eq, density, stoicPurity, molarity,' +
            ' saltCode, saltEq', function() {
            var stoichRow = new StoichRow();
            stoichRow.weight.value = 10;
            stoichRow.mol.value = 11;
            stoichRow.eq.value = 2;
            stoichRow.density.value = 3;
            stoichRow.stoicPurity.value = 11;
            stoichRow.molarity.value = 11;
            stoichRow.saltCode = {name: '01 - HYDROCHLORIDE', weight: 1, prevWeight: 0};
            stoichRow.saltEq.value = 11;
            stoichRow.rxnRole = {name: 'SOLVENT', entered: true};

            service.onFieldValueChanged(stoichRow, 'rxnRole');

            expect(stoichRow.weight.value).toBe(0);
            expect(stoichRow.weight.readonly).toBeTruthy();
            expect(stoichRow.mol.value).toBe(0);
            expect(stoichRow.mol.readonly).toBeTruthy();
            expect(stoichRow.eq.value).toBe(1);
            expect(stoichRow.eq.readonly).toBeTruthy();
            expect(stoichRow.density.value).toBe(0);
            expect(stoichRow.density.readonly).toBeTruthy();
            expect(stoichRow.stoicPurity.value).toBe(100);
            expect(stoichRow.stoicPurity.readonly).toBeTruthy();
            expect(stoichRow.molarity.value).toBe(0);
            expect(stoichRow.molarity.readonly).toBeTruthy();
            expect(stoichRow.saltCode.weight).toBe(0);
            expect(stoichRow.saltCode.readonly).toBeTruthy();
            expect(stoichRow.saltEq.value).toBe(0);
            expect(stoichRow.saltEq.readonly).toBeTruthy();
        });

        it('row is limiting, set solvent role, should reset and disable fields and set limiting to the next line', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 110;
            limitingRow.mol.value = 11;
            limitingRow.rxnRole = {name: 'SOLVENT', entered: true};
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
            otherRow.molWeight.value = 5;
            service.addRow(otherRow);

            service.onFieldValueChanged(limitingRow, 'rxnRole');

            expect(limitingRow.limiting).toBeFalsy();
            expect(otherRow.limiting).toBeTruthy();
        });

        it('previous role was solvent, set reactant, should reset volume, enable fields and set mol of limiting' +
            ' row', function() {
            var readonlyFields = ['weight', 'mol', 'eq', 'molarity', 'density', 'limiting', 'stoicPurity', 'saltCode', 'saltEq'];
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 100;
            limitingRow.mol.value = 10;
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
            otherRow.molWeight.value = 5;
            otherRow.volume.value = 5;
            otherRow.setReadonly(readonlyFields, true);
            service.addRow(otherRow);

            service.onFieldValueChanged(otherRow, 'rxnRole', 'SOLVENT');

            expect(otherRow.volume.value).toBe(0);
            expect(otherRow.weight.value).toBe(50);
            expect(otherRow.weight.readonly).toBeFalsy();
            expect(otherRow.mol.value).toBe(10);
            expect(otherRow.mol.readonly).toBeFalsy();
            expect(otherRow.eq.value).toBe(1);
            expect(otherRow.eq.readonly).toBeFalsy();
            expect(otherRow.density.value).toBe(0);
            expect(otherRow.density.readonly).toBeFalsy();
            expect(otherRow.stoicPurity.value).toBe(100);
            expect(otherRow.stoicPurity.readonly).toBeFalsy();
            expect(otherRow.molarity.value).toBe(0);
            expect(otherRow.molarity.readonly).toBeFalsy();
            expect(otherRow.saltCode.weight).toBe(0);
            expect(otherRow.saltCode.readonly).toBeFalsy();
            expect(otherRow.saltEq.value).toBe(0);
            expect(otherRow.saltEq.readonly).toBeFalsy();
        });
    });
}

module.exports = changeRxnRole;
