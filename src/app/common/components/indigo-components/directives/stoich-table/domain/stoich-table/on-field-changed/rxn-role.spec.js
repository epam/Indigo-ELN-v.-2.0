var ReagentField = require('../../reagent/reagent-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changeRxnRole() {
    describe('Change rxnRole', function() {
        var service;

        beforeEach(function() {
            var config = {
                table: {product: [], reactants: []}
            };

            service = stoichTable(config);
        });

        it('set solvent role, should reset and disable weight, mol, eq, density, stoicPurity, molarity,' +
            ' saltCode, saltEq', function() {
            var reagentRow = new ReagentField();
            reagentRow.weight.value = 10;
            reagentRow.mol.value = 11;
            reagentRow.eq.value = 2;
            reagentRow.density.value = 3;
            reagentRow.stoicPurity.value = 11;
            reagentRow.molarity.value = 11;
            reagentRow.saltCode = {name: '01 - HYDROCHLORIDE', weight: 1};
            reagentRow.saltEq.value = 11;
            reagentRow.rxnRole = {name: 'SOLVENT'};

            service.onFieldValueChanged(reagentRow, fieldTypes.rxnRole);

            expect(reagentRow.weight.value).toBe(0);
            expect(reagentRow.weight.readonly).toBeTruthy();
            expect(reagentRow.mol.value).toBe(0);
            expect(reagentRow.mol.readonly).toBeTruthy();
            expect(reagentRow.eq.value).toBe(1);
            expect(reagentRow.eq.readonly).toBeTruthy();
            expect(reagentRow.density.value).toBe(0);
            expect(reagentRow.density.readonly).toBeTruthy();
            expect(reagentRow.stoicPurity.value).toBe(100);
            expect(reagentRow.stoicPurity.readonly).toBeTruthy();
            expect(reagentRow.molarity.value).toBe(0);
            expect(reagentRow.molarity.readonly).toBeTruthy();
            expect(reagentRow.saltCode.weight).toBe(0);
            expect(reagentRow.saltCode.readonly).toBeTruthy();
            expect(reagentRow.saltEq.value).toBe(0);
            expect(reagentRow.saltEq.readonly).toBeTruthy();
        });

        it('row is limiting, set solvent role, should reset and disable fields' +
            ' and set limiting to the next line', function() {
            var limitingRow = new ReagentField();
            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 110;
            limitingRow.mol.value = 11;
            limitingRow.rxnRole = {name: 'SOLVENT'};
            service.addRow(limitingRow);

            var otherRow = new ReagentField();
            otherRow.molWeight.value = 5;
            service.addRow(otherRow);

            service.onFieldValueChanged(limitingRow, fieldTypes.rxnRole);

            expect(limitingRow.limiting).toBeFalsy();
            expect(otherRow.limiting).toBeTruthy();
        });

        it('previous role was solvent, set reactant, should reset volume, enable fields and set mol of limiting' +
            ' row', function() {
            var readonlyFields = [
                fieldTypes.weight,
                fieldTypes.mol,
                fieldTypes.eq,
                fieldTypes.molarity,
                fieldTypes.density,
                fieldTypes.stoicPurity,
                fieldTypes.saltCode,
                fieldTypes.saltEq
            ];
            var limitingRow = new ReagentField();
            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 100;
            limitingRow.mol.value = 10;
            service.addRow(limitingRow);

            var otherRow = new ReagentField();
            otherRow.molWeight.value = 5;
            otherRow.volume.value = 5;
            otherRow.setReadonly(readonlyFields, true);
            otherRow.rxnRole = {name: 'REACTANT'};
            otherRow.prevRxnRole = {name: 'SOLVENT'};
            service.addRow(otherRow);

            service.onFieldValueChanged(otherRow, fieldTypes.rxnRole);

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

        it('stoichTable has solvent row, change weight of limiting it should not effect on solvent row', function() {
            var limitingRow = new ReagentField();
            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 100;
            limitingRow.weight.entered = true;
            limitingRow.mol.value = 10;
            service.addRow(limitingRow);

            var otherRow = new ReagentField();
            otherRow.molWeight.value = 5;
            otherRow.rxnRole = {name: 'SOLVENT'};
            service.addRow(otherRow);

            service.onFieldValueChanged(limitingRow, 'weight');

            expect(otherRow.weight.readonly).toBeTruthy();
            expect(otherRow.mol.readonly).toBeTruthy();
            expect(otherRow.eq.readonly).toBeTruthy();
            expect(otherRow.density.readonly).toBeTruthy();
            expect(otherRow.stoicPurity.readonly).toBeTruthy();
            expect(otherRow.molarity.readonly).toBeTruthy();
            expect(otherRow.saltCode.readonly).toBeTruthy();
            expect(otherRow.saltEq.readonly).toBeTruthy();
        });
    });
}

module.exports = changeRxnRole;
