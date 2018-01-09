var ReagentViewRow = require('../../../domain/reagent/view-row/reagent-view-row');
var ReagentRow = require('../../../domain/reagent/calculation-row/reagent-row');
var fieldTypes = require('../../../domain/field-types');

function onSaltCodeChanged() {
    describe('Change salt code/eq', function() {
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

            firstRow.molWeight.value = 3;
            firstRow.molWeight.originalValue = 3;
            firstRow.weight.value = 45;
            firstRow.mol.value = 15;
            firstRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
            firstRow.saltEq.value = 12;

            reagentsData = {
                rows: rows,
                changedRow: firstRow,
                changedField: fieldTypes.saltEq
            };
        });

        it('row is limiting, should compute mol, then weight and update mol in other rows', function() {
            firstRow.limiting.value = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].molWeight.value).toBe(15);
            expect(calculatedRows[0].mol.value).toBe(3);
            expect(calculatedRows[1].mol.value).toBe(3);
        });

        it('row is not limiting, should compute weight', function() {
            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].molWeight.value).toBe(15);
            expect(calculatedRows[0].weight.value).toBe(225);
            expect(calculatedRows[0].mol.value).toBe(15);
        });

        it('row is not limiting, should set original mol weight', function() {
            rows = service.calculate(reagentsData);
            firstRow = rows[0];

            expect(firstRow.molWeight.value).toBe(15);
            expect(firstRow.weight.value).toBe(225);
            expect(firstRow.mol.value).toBe(15);

            firstRow.saltCode = {name: '00 - Parent Structure', value: '0', regValue: '00', weight: 0};

            reagentsData.rows = rows;
            reagentsData.changedRow = firstRow;

            rows = service.calculate(reagentsData);
            firstRow = rows[0];

            expect(firstRow.molWeight.value).toBe(3);
            expect(firstRow.weight.value).toBe(45);
            expect(firstRow.mol.value).toBe(15);
        });
    });
}

module.exports = onSaltCodeChanged;
