var ReagentField = require('../../reagent/reagent-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changeRxnRole() {
    describe('Change rxnRole', function() {
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

            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 110;
            limitingRow.mol.value = 10;

            nonLimitingRow.molWeight.value = 5;
            nonLimitingRow.rxnRole = {name: 'REACTANT'};
        });

        it('set solvent role, should reset and disable weight, mol, eq, density, stoicPurity, molarity,' +
            ' saltCode, saltEq, loadFactor', function() {
            nonLimitingRow.weight.value = 10;
            nonLimitingRow.mol.value = 11;
            nonLimitingRow.eq.value = 2;
            nonLimitingRow.density.value = 3;
            nonLimitingRow.stoicPurity.value = 11;
            nonLimitingRow.molarity.value = 11;
            nonLimitingRow.saltCode = {name: '01 - HYDROCHLORIDE', weight: 1};
            nonLimitingRow.saltEq.value = 11;
            nonLimitingRow.rxnRole = {name: 'SOLVENT'};
            nonLimitingRow.loadFactor.value = 11;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.rxnRole);

            expect(nonLimitingRow.weight.value).toBe(0);
            expect(nonLimitingRow.weight.readonly).toBeTruthy();
            expect(nonLimitingRow.mol.value).toBe(0);
            expect(nonLimitingRow.mol.readonly).toBeTruthy();
            expect(nonLimitingRow.eq.value).toBe(1);
            expect(nonLimitingRow.eq.readonly).toBeTruthy();
            expect(nonLimitingRow.density.value).toBe(0);
            expect(nonLimitingRow.density.readonly).toBeTruthy();
            expect(nonLimitingRow.stoicPurity.value).toBe(100);
            expect(nonLimitingRow.stoicPurity.readonly).toBeTruthy();
            expect(nonLimitingRow.molarity.value).toBe(0);
            expect(nonLimitingRow.molarity.readonly).toBeTruthy();
            expect(nonLimitingRow.saltCode.weight).toBe(0);
            expect(nonLimitingRow.saltCode.readonly).toBeTruthy();
            expect(nonLimitingRow.saltEq.value).toBe(0);
            expect(nonLimitingRow.saltEq.readonly).toBeTruthy();
            expect(nonLimitingRow.loadFactor.value).toBe(1);
            expect(nonLimitingRow.loadFactor.readonly).toBeTruthy();
        });

        it('row is limiting, set solvent role, should reset and disable fields' +
            ' and set limiting to the next line', function() {
            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            limitingRow.rxnRole = {name: 'SOLVENT'};

            service.onFieldValueChanged(limitingRow, fieldTypes.rxnRole);

            expect(limitingRow.limiting).toBeFalsy();
            expect(nonLimitingRow.limiting).toBeTruthy();
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
                fieldTypes.saltEq,
                fieldTypes.loadFactor
            ];
            service.addRow(limitingRow);

            nonLimitingRow.volume.value = 5;
            nonLimitingRow.prevRxnRole = {name: 'SOLVENT'};
            nonLimitingRow.setReadonly(readonlyFields, true);
            service.addRow(nonLimitingRow);

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.rxnRole);

            expect(nonLimitingRow.volume.value).toBe(0);
            expect(nonLimitingRow.weight.value).toBe(50);
            expect(nonLimitingRow.weight.readonly).toBeFalsy();
            expect(nonLimitingRow.mol.value).toBe(10);
            expect(nonLimitingRow.mol.readonly).toBeFalsy();
            expect(nonLimitingRow.eq.value).toBe(1);
            expect(nonLimitingRow.eq.readonly).toBeFalsy();
            expect(nonLimitingRow.density.value).toBe(0);
            expect(nonLimitingRow.density.readonly).toBeFalsy();
            expect(nonLimitingRow.stoicPurity.value).toBe(100);
            expect(nonLimitingRow.stoicPurity.readonly).toBeFalsy();
            expect(nonLimitingRow.molarity.value).toBe(0);
            expect(nonLimitingRow.molarity.readonly).toBeFalsy();
            expect(nonLimitingRow.saltCode.weight).toBe(0);
            expect(nonLimitingRow.saltCode.readonly).toBeFalsy();
            expect(nonLimitingRow.saltEq.value).toBe(0);
            expect(nonLimitingRow.saltEq.readonly).toBeFalsy();
            expect(nonLimitingRow.loadFactor.readonly).toBeFalsy();
        });
    });
}

module.exports = changeRxnRole;
