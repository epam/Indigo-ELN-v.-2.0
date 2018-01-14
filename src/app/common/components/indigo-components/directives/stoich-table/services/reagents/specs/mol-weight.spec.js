var ReagentViewRow = require('../../../domain/reagent/view-row/reagent-view-row');
var ReagentRow = require('../../../domain/reagent/calculation-row/reagent-row');
var fieldTypes = require('../../../domain/field-types');

function onMolWeightChanged() {
    describe('Change mol weight', function() {
        var service;
        var reagentsData;
        var firstRow;

        beforeEach(angular.mock.inject(function(_reagentsCalculation_) {
            service = _reagentsCalculation_;
        }));

        beforeEach(function() {
            var rows = [];
            firstRow = new ReagentRow(new ReagentViewRow());
            rows.push(firstRow);

            reagentsData = {
                rows: rows,
                idOfChangedRow: firstRow.id,
                changedField: fieldTypes.molWeight
            };
        });

        it('weight is defined, row is limiting, should compute mol', function() {
            firstRow.molWeight.value = 23;
            firstRow.weight.value = 23;
            firstRow.weight.entered = true;
            firstRow.limiting.value = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].mol.value).toBe(1);
        });

        it('mol is defined, row is not limiting, should compute weight', function() {
            firstRow.molWeight.value = 23;
            firstRow.mol.value = 2;
            firstRow.mol.entered = true;

            var calculatedRows = service.calculate(reagentsData);

            expect(calculatedRows[0].weight.value).toBe(46);
        });
    });
}

module.exports = onMolWeightChanged;
