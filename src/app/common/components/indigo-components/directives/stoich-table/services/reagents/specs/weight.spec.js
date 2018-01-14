var ReagentViewRow = require('../../../domain/reagent/view-row/reagent-view-row');
var ReagentRow = require('../../../domain/reagent/calculation-row/reagent-row');
var fieldTypes = require('../../../domain/field-types');

function onWeightChanged() {
    describe('Change weight', function() {
        var service;
        var reagentsData;
        var firstRow;
        var secondRow;

        beforeEach(angular.mock.inject(function(_reagentsCalculation_) {
            service = _reagentsCalculation_;
        }));

        beforeEach(function() {
            var rows = [];

            firstRow = new ReagentRow(new ReagentViewRow());
            secondRow = new ReagentRow(new ReagentViewRow());

            rows.push(firstRow);
            rows.push(secondRow);

            reagentsData = {
                rows: rows,
                idOfChangedRow: firstRow.id,
                changedField: fieldTypes.weight
            };
        });

        it('mol weight is defined; should compute mol, it should be 1', function() {
            firstRow.molWeight.value = 23;
            firstRow.weight.value = 23;
            firstRow.weight.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].mol.value).toBe(1);
        });

        it('mol weight is defined, row is limiting; should compute mol and change mol in other rows', function() {
            firstRow.molWeight.value = 10;
            firstRow.weight.value = 10;
            firstRow.weight.entered = true;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 5;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].mol.value).toBe(1);
            expect(calculatedRows[1].weight.value).toBe(5);
            expect(calculatedRows[1].mol.value).toBe(1);
        });

        it('mol weight is not defined, but mol is defined; should compute mol weight', function() {
            firstRow.weight.value = 20;
            firstRow.weight.entered = true;
            firstRow.mol.value = 10;
            firstRow.mol.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].molWeight.value).toBe(2);
        });

        it('mol weight is defined, row is limiting, weight is removed or 0; mol of limiting row should be 0 and' +
            ' mol of other rows are the same', function() {
            firstRow.molWeight.value = 10;
            firstRow.weight.value = 0;
            firstRow.weight.entered = true;
            firstRow.mol.value = 10;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 1;
            secondRow.mol.value = 10;
            secondRow.weight.value = 10;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].mol.value).toBe(0);
            expect(calculatedRows[0].eq.value).toBe(1);
            expect(calculatedRows[1].mol.value).toBe(10);
        });

        it('mol weight is defined, row is not limiting, weight is removed or 0;' +
            ' mol and eq should be default', function() {
            firstRow.molWeight.value = 10;
            firstRow.weight.value = 0;
            firstRow.weight.entered = true;
            firstRow.mol.value = 10;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].mol.value).toBe(0);
            expect(calculatedRows[0].eq.value).toBe(1);
        });

        it('density is defined, should compute volume', function() {
            firstRow.density.value = 10;
            firstRow.density.entered = true;
            firstRow.weight.value = 2;
            firstRow.weight.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].volume.value).toBe(0.0002);
        });

        it('mol weight, volume, mol, molarity, eq are defined, should recompute volume, mol and eq', function() {
            firstRow.molWeight.value = 2;
            firstRow.weight.value = 10;
            firstRow.weight.entered = true;
            firstRow.mol.value = 20;
            firstRow.volume.value = 4;
            firstRow.molarity.value = 5;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].mol.value).toBe(5);
            expect(calculatedRows[0].volume.value).toBe(1);
            expect(calculatedRows[0].molarity.value).toBe(5);
            expect(calculatedRows[0].weight.value).toBe(10);
            expect(calculatedRows[0].eq.value).toBe(1);
            expect(calculatedRows[0].molWeight.value).toBe(2);
        });

        it('mol weight, volume, mol, molarity, eq are defined, remove weight, should reset volume and mol', function() {
            firstRow.molWeight.value = 2;
            firstRow.mol.value = 20;
            firstRow.volume.value = 4;
            firstRow.molarity.value = 5;
            firstRow.weight.value = 0;
            firstRow.weight.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].mol.value).toBe(0);
            expect(calculatedRows[0].volume.value).toBe(0);
            expect(calculatedRows[0].molarity.value).toBe(5);
            expect(calculatedRows[0].weight.value).toBe(0);
            expect(calculatedRows[0].eq.value).toBe(1);
            expect(calculatedRows[0].molWeight.value).toBe(2);
        });

        it('mol weight, volume, mol, molarity, eq are defined, correct weight, should compute mol, volume', function() {
            firstRow.molWeight.value = 2;
            firstRow.mol.value = 20;
            firstRow.volume.value = 4;
            firstRow.volume.entered = true;
            firstRow.molarity.value = 5;
            firstRow.molarity.entered = true;
            firstRow.weight.value = 12;
            firstRow.weight.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].mol.value).toBe(6);
            expect(calculatedRows[0].volume.value).toBe(1.2);
            expect(calculatedRows[0].volume.entered).toBeFalsy();
            expect(calculatedRows[0].molarity.value).toBe(5);
            expect(calculatedRows[0].molarity.entered).toBeTruthy();
            expect(calculatedRows[0].weight.value).toBe(12);
            expect(calculatedRows[0].weight.entered).toBeTruthy();
            expect(calculatedRows[0].eq.value).toBe(1);
            expect(calculatedRows[0].molWeight.value).toBe(2);
        });

        it('row is limiting, non limiting row has manually entered mol, ' +
            'should not update this row with new mol', function() {
            firstRow.molWeight.value = 3;
            firstRow.mol.value = 5;
            firstRow.limiting.value = true;
            firstRow.weight.value = 15;
            firstRow.weight.entered = true;

            secondRow.molWeight.value = 10;
            secondRow.mol.value = 10;
            secondRow.mol.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[1].mol.value).toBe(10);
            expect(calculatedRows[1].mol.entered).toBeTruthy();
            expect(calculatedRows[1].eq.value).toBe(2);
        });

        it('row is limiting, eq is manually entered, should not reset eq', function() {
            firstRow.molWeight.value = 3;
            firstRow.eq.value = 2;
            firstRow.eq.entered = true;
            firstRow.limiting.value = true;
            firstRow.weight.value = 15;
            firstRow.weight.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].mol.value).toBe(5);
            expect(calculatedRows[0].eq.value).toBe(2);
            expect(calculatedRows[0].eq.entered).toBeTruthy();
        });

        it('row is not limiting, eq is manually entered, should not reset eq', function() {
            firstRow.molWeight.value = 3;
            firstRow.weight.value = 15;
            firstRow.weight.entered = true;
            firstRow.eq.value = 2;
            firstRow.eq.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].mol.value).toBe(5);
            expect(calculatedRows[0].eq.value).toBe(2);
        });

        it('row is not limiting, purity is manually entered, set weight in limiting row,' +
            ' should update mol and weight(depending on purity) in other row', function() {
            firstRow.molWeight.value = 3;
            firstRow.mol.value = 5;
            firstRow.limiting.value = true;
            firstRow.weight.value = 15;
            firstRow.weight.entered = true;

            secondRow.molWeight.value = 10;
            secondRow.stoicPurity.value = 50;
            secondRow.stoicPurity.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[1].mol.value).toBe(5);
            expect(calculatedRows[1].weight.value).toBe(100);
        });

        it('stoichTable has solvent row, change weight of limiting it should not effect on solvent row', function() {
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

            firstRow.molWeight.value = 10;
            firstRow.weight.value = 100;
            firstRow.weight.entered = true;
            firstRow.limiting.value = true;

            secondRow.molWeight.value = 5;
            secondRow.rxnRole = {name: 'SOLVENT'};
            secondRow.setReadonly(readonlyFields, true);

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[1].weight.readonly).toBeTruthy();
            expect(calculatedRows[1].mol.readonly).toBeTruthy();
            expect(calculatedRows[1].eq.readonly).toBeTruthy();
            expect(calculatedRows[1].density.readonly).toBeTruthy();
            expect(calculatedRows[1].stoicPurity.readonly).toBeTruthy();
            expect(calculatedRows[1].molarity.readonly).toBeTruthy();
            expect(calculatedRows[1].saltCode.readonly).toBeTruthy();
            expect(calculatedRows[1].saltEq.readonly).toBeTruthy();
            expect(calculatedRows[1].loadFactor.readonly).toBeTruthy();
        });
    });
}

module.exports = onWeightChanged;
