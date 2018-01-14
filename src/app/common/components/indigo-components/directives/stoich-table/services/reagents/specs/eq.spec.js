var ReagentViewRow = require('../../../domain/reagent/view-row/reagent-view-row');
var ReagentRow = require('../../../domain/reagent/calculation-row/reagent-row');
var fieldTypes = require('../../../domain/field-types');

function onEqChanged() {
    describe('Change eq', function() {
        var service;
        var reagentsData;
        var rows;
        var firstRow;
        var secondRow;

        beforeEach(angular.mock.inject(function(_reagentsCalculation_) {
            service = _reagentsCalculation_;
        }));

        beforeEach(function() {
            rows = [];

            firstRow = new ReagentRow(new ReagentViewRow());
            secondRow = new ReagentRow(new ReagentViewRow());

            rows.push(firstRow);
            rows.push(secondRow);

            reagentsData = {
                rows: rows,
                idOfChangedRow: firstRow.id,
                changedField: fieldTypes.eq
            };
        });

        it('complex test.' +
            '1. Apply new EQ in Limiting row - non limiting Mol and Weight are updated' +
            '2. Enter manual weight in non limiting row - Mols are recalculated for this particular row' +
            '3. Update EQ for Limiting' +
            'Expected results: Mols are not update in Limiting and Non limiting row.' +
            'EQ of non limiting row is adjusted by formula:' +
            '(Non Limiting Mol * Limiting EQ)/Limiting Mol', function() {
            firstRow.molWeight.value = 10;
            firstRow.weight.value = 100;
            firstRow.weight.entered = true;
            firstRow.mol.value = 10;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 10;

            firstRow.eq.value = 2;
            firstRow.eq.entered = true;

            rows = service.calculate(reagentsData);
            firstRow = rows[0];
            secondRow = rows[1];

            expect(secondRow.weight.value).toBe(50);
            expect(secondRow.mol.value).toBe(5);
            expect(secondRow.eq.value).toBe(1);

            secondRow.weight.value = 5;
            secondRow.weight.entered = true;

            reagentsData.rows = rows;
            reagentsData.idOfChangedRow = secondRow.id;
            reagentsData.changedField = fieldTypes.weight;

            rows = service.calculate(reagentsData);
            firstRow = rows[0];
            secondRow = rows[1];

            expect(secondRow.weight.value).toBe(5);
            expect(secondRow.weight.entered).toBeTruthy();
            expect(secondRow.mol.value).toBe(0.5);
            expect(secondRow.eq.value).toBe(0.1);

            firstRow.eq.value = 4;
            firstRow.eq.entered = true;

            reagentsData.rows = rows;
            reagentsData.idOfChangedRow = firstRow.id;
            reagentsData.changedField = fieldTypes.eq;

            rows = service.calculate(reagentsData);
            firstRow = rows[0];
            secondRow = rows[1];

            expect(secondRow.weight.value).toBe(5);
            expect(secondRow.weight.entered).toBeTruthy();
            expect(secondRow.mol.value).toBe(0.5);
            expect(secondRow.eq.value).toBe(0.2);
        });

        it('row is not limiting, set eq to 2 and then to 1, should return original values', function() {
            firstRow.molWeight.value = 10;
            firstRow.weight.value = 100;
            firstRow.weight.entered = true;
            firstRow.mol.value = 10;
            firstRow.eq.value = 2;
            firstRow.eq.entered = true;

            rows = service.calculate(reagentsData);
            firstRow = rows[0];
            secondRow = rows[1];

            expect(firstRow.weight.value).toBe(200);
            expect(firstRow.mol.value).toBe(20);
            expect(firstRow.eq.value).toBe(2);

            firstRow.eq.value = 1;

            reagentsData.rows = rows;
            reagentsData.idOfChangedRow = firstRow.id;

            rows = service.calculate(reagentsData);
            firstRow = rows[0];
            secondRow = rows[1];

            expect(firstRow.weight.value).toBe(100);
            expect(firstRow.mol.value).toBe(10);
            expect(firstRow.eq.value).toBe(1);
        });

        describe('', function() {
            beforeEach(function() {
                firstRow.molWeight.value = 10;
                firstRow.weight.value = 110;
                firstRow.mol.value = 11;
                firstRow.eq.value = 2;
                firstRow.eq.entered = true;
            });

            it('row is not limiting, should update mol and weight', function() {
                firstRow.mol.entered = true;
                firstRow.weight.entered = true;

                var calculatedRows = service.calculate(reagentsData);

                expect(calculatedRows[0].weight.value).toBe(220);
                expect(calculatedRows[0].mol.value).toBe(22);
                expect(calculatedRows[0].weight.entered).toBeFalsy();
                expect(calculatedRows[0].mol.entered).toBeFalsy();
            });

            it('row is limiting, mol is manually entered, should update weight', function() {
                firstRow.mol.entered = true;
                firstRow.limiting.value = true;

                var calculatedRows = service.calculate(reagentsData);

                expect(calculatedRows[0].weight.value).toBe(220);
                expect(calculatedRows[0].mol.value).toBe(11);
                expect(calculatedRows[0].mol.entered).toBeTruthy();
            });

            it('row is limiting, should update mol in other rows', function() {
                firstRow.weight.entered = true;
                firstRow.limiting.value = true;

                secondRow.molWeight.value = 10;

                var calculatedRows = service.calculate(reagentsData);

                expect(calculatedRows[1].weight.value).toBe(55);
                expect(calculatedRows[1].mol.value).toBe(5.5);
                expect(calculatedRows[1].eq.value).toBe(1);
            });
        });

        describe('row is not limiting', function() {
            beforeEach(function() {
                firstRow.molWeight.value = 10;
                firstRow.weight.value = 100;
                firstRow.weight.entered = true;
                firstRow.mol.value = 10;
                firstRow.limiting.value = true;

                secondRow.molWeight.value = 10;
                secondRow.volume.value = 2;
            });

            it('should delete volume', function() {
                secondRow.eq.value = 2;
                secondRow.eq.entered = true;

                reagentsData.idOfChangedRow = secondRow.id;

                var calculatedRows = service.calculate(reagentsData);

                expect(calculatedRows[1].weight.value).toBe(200);
                expect(calculatedRows[1].mol.value).toBe(20);
                expect(calculatedRows[1].eq.value).toBe(2);
                expect(calculatedRows[1].volume.value).toBe(0);
            });

            it('molarity is defined, should recalculate volume', function() {
                secondRow.weight.value = 100;
                secondRow.mol.value = 10;
                secondRow.molarity.value = 2;
                secondRow.molarity.entered = true;
                secondRow.eq.value = 2;
                secondRow.eq.entered = true;

                reagentsData.idOfChangedRow = secondRow.id;

                var calculatedRows = service.calculate(reagentsData);

                expect(calculatedRows[1].weight.value).toBe(200);
                expect(calculatedRows[1].mol.value).toBe(20);
                expect(calculatedRows[1].eq.value).toBe(2);
                expect(calculatedRows[1].volume.value).toBe(10);
            });

            it('density is defined, should recalculate volume', function() {
                secondRow.weight.value = 100;
                secondRow.mol.value = 10;
                secondRow.density.value = 2;
                secondRow.density.entered = true;
                secondRow.eq.value = 2;
                secondRow.eq.entered = true;

                reagentsData.idOfChangedRow = secondRow.id;

                var calculatedRows = service.calculate(reagentsData);

                expect(calculatedRows[1].weight.value).toBe(200);
                expect(calculatedRows[1].mol.value).toBe(20);
                expect(calculatedRows[1].eq.value).toBe(2);
                expect(calculatedRows[1].volume.value).toBe(0.1);
            });
        });
    });
}

module.exports = onEqChanged;
