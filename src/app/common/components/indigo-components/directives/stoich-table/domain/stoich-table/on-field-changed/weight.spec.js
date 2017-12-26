var ReagentField = require('../../reagent/reagent-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changeWeight() {
    describe('Change weight', function() {
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

        it('mol weight is defined; should compute mol, it should be 1', function() {
            limitingRow.molWeight.value = 23;
            service.addRow(limitingRow);

            limitingRow.weight.value = 23;
            limitingRow.weight.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(limitingRow.mol.value).toBe(1);
        });

        it('mol weight is defined, row is limiting; should compute mol and change mol in other rows', function() {
            limitingRow.molWeight.value = 10;
            nonLimitingRow.molWeight.value = 5;

            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            limitingRow.weight.value = 10;
            limitingRow.weight.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(limitingRow.mol.value).toBe(1);
            expect(nonLimitingRow.weight.value).toBe(5);
            expect(nonLimitingRow.mol.value).toBe(1);
        });

        it('mol weight is not defined, but mol is defined; should compute mol weight', function() {
            nonLimitingRow.weight.value = 20;
            nonLimitingRow.weight.entered = true;
            nonLimitingRow.mol.value = 10;
            nonLimitingRow.mol.entered = true;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.weight);

            expect(nonLimitingRow.molWeight.value).toBe(2);
        });

        it('mol weight is defined, row is limiting, weight is removed or 0; mol of limiting row should be 0 and' +
            ' mol of other rows are the same', function() {
            limitingRow.molWeight.value = 10;
            limitingRow.mol.value = 10;

            nonLimitingRow.molWeight.value = 1;
            nonLimitingRow.mol.value = 10;
            nonLimitingRow.weight.value = 10;

            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            limitingRow.weight.value = 0;
            limitingRow.weight.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(limitingRow.mol.value).toBe(0);
            expect(limitingRow.eq.value).toBe(1);
            expect(nonLimitingRow.mol.value).toBe(10);
        });

        it('mol weight is defined, row is not limiting, weight is removed or 0;' +
            ' mol and eq should be default', function() {
            nonLimitingRow.molWeight.value = 10;
            nonLimitingRow.weight.value = 0;
            nonLimitingRow.weight.entered = true;
            nonLimitingRow.mol.value = 10;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.weight);

            expect(nonLimitingRow.mol.value).toBe(0);
            expect(nonLimitingRow.eq.value).toBe(1);
        });

        it('density is defined, should compute volume', function() {
            nonLimitingRow.density.value = 10;
            nonLimitingRow.density.entered = true;
            nonLimitingRow.weight.value = 2;
            nonLimitingRow.weight.entered = true;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.weight);

            expect(nonLimitingRow.volume.value).toBe(0.0002);
        });

        it('mol weight, volume, mol, molarity, eq are defined, should recompute volume, mol and eq', function() {
            limitingRow.molWeight.value = 2;
            limitingRow.mol.value = 20;
            limitingRow.volume.value = 4;
            limitingRow.molarity.value = 5;
            service.addRow(limitingRow);

            limitingRow.weight.value = 10;
            limitingRow.weight.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(limitingRow.mol.value).toBe(5);
            expect(limitingRow.volume.value).toBe(1);
            expect(limitingRow.molarity.value).toBe(5);
            expect(limitingRow.weight.value).toBe(10);
            expect(limitingRow.eq.value).toBe(1);
            expect(limitingRow.molWeight.value).toBe(2);
        });

        it('mol weight, volume, mol, molarity, eq are defined, remove weight, should reset volume and mol', function() {
            limitingRow.molWeight.value = 2;
            limitingRow.mol.value = 20;
            limitingRow.volume.value = 4;
            limitingRow.molarity.value = 5;
            service.addRow(limitingRow);

            limitingRow.weight.value = 0;
            limitingRow.weight.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(limitingRow.mol.value).toBe(0);
            expect(limitingRow.volume.value).toBe(0);
            expect(limitingRow.molarity.value).toBe(5);
            expect(limitingRow.weight.value).toBe(0);
            expect(limitingRow.eq.value).toBe(1);
            expect(limitingRow.molWeight.value).toBe(2);
        });

        it('mol weight, volume, mol, molarity, eq are defined, correct weight, should compute mol, volume', function() {
            limitingRow.molWeight.value = 2;
            limitingRow.mol.value = 20;
            limitingRow.volume.value = 4;
            limitingRow.volume.entered = true;
            limitingRow.molarity.value = 5;
            limitingRow.molarity.entered = true;
            service.addRow(limitingRow);

            limitingRow.weight.value = 12;
            limitingRow.weight.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(limitingRow.mol.value).toBe(6);
            expect(limitingRow.volume.value).toBe(1.2);
            expect(limitingRow.volume.entered).toBeFalsy();
            expect(limitingRow.molarity.value).toBe(5);
            expect(limitingRow.molarity.entered).toBeTruthy();
            expect(limitingRow.weight.value).toBe(12);
            expect(limitingRow.weight.entered).toBeTruthy();
            expect(limitingRow.eq.value).toBe(1);
            expect(limitingRow.molWeight.value).toBe(2);
        });

        it('row is limiting, non limiting row has manually entered mol, ' +
            'should not update this row with new mol', function() {
            limitingRow.molWeight.value = 3;
            limitingRow.mol.value = 5;
            service.addRow(limitingRow);

            nonLimitingRow.molWeight.value = 10;
            service.addRow(nonLimitingRow);

            limitingRow.weight.value = 15;
            limitingRow.weight.entered = true;
            nonLimitingRow.mol.value = 10;
            nonLimitingRow.mol.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(nonLimitingRow.mol.value).toBe(10);
            expect(nonLimitingRow.mol.entered).toBeTruthy();
            expect(nonLimitingRow.eq.value).toBe(2);
        });

        it('row is limiting, eq is manually entered, should not reset eq', function() {
            limitingRow.molWeight.value = 3;
            limitingRow.eq.value = 2;
            limitingRow.eq.entered = true;
            service.addRow(limitingRow);

            limitingRow.weight.value = 15;
            limitingRow.weight.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(limitingRow.mol.value).toBe(5);
            expect(limitingRow.eq.value).toBe(2);
            expect(limitingRow.eq.entered).toBeTruthy();
        });

        it('row is not limiting, eq is manually entered, should not reset eq', function() {
            limitingRow.molWeight.value = 3;
            limitingRow.weight.value = 15;
            limitingRow.weight.entered = true;
            limitingRow.mol.value = 5;
            limitingRow.eq.value = 2;
            limitingRow.eq.entered = true;
            service.addRow(limitingRow);

            nonLimitingRow.molWeight.value = 10;
            service.addRow(nonLimitingRow);

            nonLimitingRow.weight.value = 10;
            nonLimitingRow.weight.entered = true;

            service.onFieldValueChanged(nonLimitingRow, fieldTypes.weight);

            expect(nonLimitingRow.mol.value).toBe(1);
            expect(nonLimitingRow.eq.value).toBe(0.4);
        });

        it('row is not limiting, purity is manually entered, set weight in limiting row,' +
            ' should update mol and weight(depending on purity) in other row', function() {
            limitingRow.molWeight.value = 3;
            limitingRow.mol.value = 5;
            service.addRow(limitingRow);

            nonLimitingRow.molWeight.value = 10;
            service.addRow(nonLimitingRow);

            nonLimitingRow.stoicPurity.value = 50;
            nonLimitingRow.stoicPurity.entered = true;

            limitingRow.weight.value = 15;
            limitingRow.weight.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(nonLimitingRow.mol.value).toBe(5);
            expect(nonLimitingRow.weight.value).toBe(100);
        });

        it('stoichTable has solvent row, change weight of limiting it should not effect on solvent row', function() {
            limitingRow.molWeight.value = 10;

            nonLimitingRow.molWeight.value = 5;
            nonLimitingRow.rxnRole = {name: 'SOLVENT'};

            service.addRow(limitingRow);
            service.addRow(nonLimitingRow);

            limitingRow.weight.value = 100;
            limitingRow.weight.entered = true;

            service.onFieldValueChanged(limitingRow, fieldTypes.weight);

            expect(nonLimitingRow.weight.readonly).toBeTruthy();
            expect(nonLimitingRow.mol.readonly).toBeTruthy();
            expect(nonLimitingRow.eq.readonly).toBeTruthy();
            expect(nonLimitingRow.density.readonly).toBeTruthy();
            expect(nonLimitingRow.stoicPurity.readonly).toBeTruthy();
            expect(nonLimitingRow.molarity.readonly).toBeTruthy();
            expect(nonLimitingRow.saltCode.readonly).toBeTruthy();
            expect(nonLimitingRow.saltEq.readonly).toBeTruthy();
            expect(nonLimitingRow.loadFactor.readonly).toBeTruthy();
        });
    });
}

module.exports = changeWeight;
