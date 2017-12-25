var ReagentField = require('../../reagent/reagent-row');
var stoichTable = require('../stoich-table');
var fieldTypes = require('../../field-types');

function changeMolWeight() {
    describe('Change mol weight', function() {
        var service;

        beforeEach(function() {
            var config = {
                table: {product: [], reactants: []}
            };

            service = stoichTable(config);
        });

        it('weight is defined, row is limiting, should compute mol', function() {
            var reagentRow = new ReagentField();
            reagentRow.molWeight.value = 23;
            reagentRow.weight.value = 23;
            service.addRow(reagentRow);

            service.onFieldValueChanged(reagentRow, fieldTypes.molWeight);

            expect(reagentRow.mol.value).toBe(1);
        });

        it('mol is defined, row is not limiting, should compute weight', function() {
            var reagentRow = new ReagentField();
            reagentRow.molWeight.value = 23;
            reagentRow.mol.value = 2;
            reagentRow.limiting = false;

            service.onFieldValueChanged(reagentRow, fieldTypes.molWeight);

            expect(reagentRow.weight.value).toBe(46);
        });
    });
}

module.exports = changeMolWeight;
