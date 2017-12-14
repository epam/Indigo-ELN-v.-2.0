var StoichRow = require('../../stoich-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changeEq() {
    describe('Change eq', function() {
        var service;

        beforeEach(function() {
            var config = {
                table: {product: [], reactants: []}
            };

            service = stoichTable(config);
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

            service.onFieldValueChanged(stoichRow, fieldTypes.eq);

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

            service.onFieldValueChanged(limitingRow, fieldTypes.eq);

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

            service.onFieldValueChanged(limitingRow, fieldTypes.eq);

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

            service.onFieldValueChanged(limitingRow, fieldTypes.eq);

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

            service.onFieldValueChanged(stoichRow, fieldTypes.eq);

            expect(stoichRow.weight.value).toBe(200);
            expect(stoichRow.mol.value).toBe(20);
            expect(stoichRow.eq.value).toBe(2);

            stoichRow.eq.value = 1;

            service.onFieldValueChanged(stoichRow, fieldTypes.eq);

            expect(stoichRow.weight.value).toBe(100);
            expect(stoichRow.mol.value).toBe(10);
            expect(stoichRow.eq.value).toBe(1);
        });

        describe('row is not limiting', function() {
            var limitingRow;
            var otherRow;
            beforeEach(function() {
                limitingRow = new StoichRow();
                limitingRow.molWeight.value = 10;
                limitingRow.weight.value = 100;
                limitingRow.weight.entered = true;
                limitingRow.mol.value = 10;
                service.addRow(limitingRow);

                otherRow = new StoichRow();
                otherRow.molWeight.value = 10;
                otherRow.volume.value = 2;
                service.addRow(otherRow);
            });
            it('should delete volume', function() {
                otherRow.eq.value = 2;
                otherRow.eq.entered = true;

                service.onFieldValueChanged(otherRow, fieldTypes.eq);

                expect(otherRow.weight.value).toBe(200);
                expect(otherRow.mol.value).toBe(20);
                expect(otherRow.eq.value).toBe(2);
                expect(otherRow.volume.value).toBe(0);
            });

            it('molarity is defined, should recalculate volume', function() {
                otherRow.molarity.value = 2;
                otherRow.molarity.entered = true;
                otherRow.eq.value = 2;
                otherRow.eq.entered = true;

                service.onFieldValueChanged(otherRow, fieldTypes.eq);

                expect(otherRow.weight.value).toBe(200);
                expect(otherRow.mol.value).toBe(20);
                expect(otherRow.eq.value).toBe(2);
                expect(otherRow.volume.value).toBe(10);
            });

            it('density is defined, should recalculate volume', function() {
                otherRow.density.value = 2;
                otherRow.density.entered = true;
                otherRow.eq.value = 2;
                otherRow.eq.entered = true;

                service.onFieldValueChanged(otherRow, fieldTypes.eq);

                expect(otherRow.weight.value).toBe(200);
                expect(otherRow.mol.value).toBe(20);
                expect(otherRow.eq.value).toBe(2);
                expect(otherRow.volume.value).toBe(0.1);
            });
        });
    });
}

module.exports = changeEq;
