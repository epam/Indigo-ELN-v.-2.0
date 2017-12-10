var StoichRow = require('../../stoich-row');
var stoichTable = require('../stoich-table');

function changeEq() {
    describe('Change eq', function() {
        var service;

        beforeEach(function() {
            service = stoichTable({product: [], reactants: []});
        });

        it('row is not limiting, should update mol and weight', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 10;
            stoichRow.weight.value = 110;
            stoichRow.weight.entered = true;
            stoichRow.mol.value = 11;
            stoichRow.mol.entered = true;
            stoichRow.eq.value = 2;
            stoichRow.limiting = false;

            service.onFieldValueChanged(stoichRow, 'eq');

            expect(stoichRow.weight.value).toBe(220);
            expect(stoichRow.weight.entered).toBeFalsy();
            expect(stoichRow.mol.entered).toBeFalsy();
        });

        it('row is limiting, should update mol in other lines', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 100;
            limitingRow.weight.entered = true;
            limitingRow.mol.value = 10;
            limitingRow.eq.value = 2;
            limitingRow.eq.entered = true;
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
            otherRow.molWeight.value = 10;
            service.addRow(otherRow);

            service.onFieldValueChanged(limitingRow, 'eq');

            expect(otherRow.weight.value).toBe(50);
            expect(otherRow.mol.value).toBe(5);
            expect(otherRow.eq.value).toBe(1);
        });

        it('complex test.' +
            '1. Apply new EQ in Limiting row - non limiting Mol and Weight are updated,' +
            '2. Enter manual weight in non limiting row - Mols are recalculated for this particular row' +
            '3. Update EQ for Limiting' +
            'Expected results: Mols are not update in Limiting and Non limiting row.' +
            'EQ of non limiting row is adjusted by formula:' +
            '(Non Limiting Mol* Limiting EQ)/Limiting Mol', function() {
            var limitingRow = new StoichRow();
            limitingRow.molWeight.value = 10;
            limitingRow.weight.value = 100;
            limitingRow.weight.entered = true;
            limitingRow.mol.value = 10;
            limitingRow.eq.value = 2;
            limitingRow.eq.entered = true;
            service.addRow(limitingRow);

            var otherRow = new StoichRow();
            otherRow.molWeight.value = 10;
            service.addRow(otherRow);

            service.onFieldValueChanged(limitingRow, 'eq');

            expect(otherRow.weight.value).toBe(50);
            expect(otherRow.mol.value).toBe(5);
            expect(otherRow.eq.value).toBe(1);

            otherRow.weight.value = 5;
            otherRow.weight.entered = true;

            service.onFieldValueChanged(otherRow, 'weight');

            expect(otherRow.weight.value).toBe(5);
            expect(otherRow.weight.entered).toBeTruthy();
            expect(otherRow.mol.value).toBe(0.5);
            expect(otherRow.eq.value).toBe(0.05);

            limitingRow.eq.value = 4;

            service.onFieldValueChanged(limitingRow, 'eq');

            expect(otherRow.weight.value).toBe(5);
            expect(otherRow.weight.entered).toBeTruthy();
            expect(otherRow.mol.value).toBe(0.5);
            expect(otherRow.eq.value).toBe(0.2);
        });

        it('row is not limiting, set eq to 2 and then to 1, should return original values', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 10;
            stoichRow.weight.value = 100;
            stoichRow.weight.entered = true;
            stoichRow.mol.value = 10;
            stoichRow.eq.value = 2;
            stoichRow.eq.entered = true;

            service.onFieldValueChanged(stoichRow, 'eq');

            expect(stoichRow.weight.value).toBe(200);
            expect(stoichRow.mol.value).toBe(20);
            expect(stoichRow.eq.value).toBe(2);

            stoichRow.eq.value = 1;

            service.onFieldValueChanged(stoichRow, 'eq');

            expect(stoichRow.weight.value).toBe(100);
            expect(stoichRow.mol.value).toBe(10);
            expect(stoichRow.eq.value).toBe(1);
        });
    });
}

module.exports = changeEq;
