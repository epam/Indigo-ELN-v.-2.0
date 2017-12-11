var StoichRow = require('../../stoich-row');
var stoichTable = require('../stoich-table');

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
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 23;
            stoichRow.weight.value = 23;
            service.addRow(stoichRow);

            service.onFieldValueChanged(stoichRow, 'molWeight');

            expect(stoichRow.mol.value).toBe(1);
        });

        it('mol is defined, row is not limiting, should compute weight', function() {
            var stoichRow = new StoichRow();
            stoichRow.molWeight.value = 23;
            stoichRow.mol.value = 2;
            stoichRow.limiting = false;

            service.onFieldValueChanged(stoichRow, 'molWeight');

            expect(stoichRow.weight.value).toBe(46);
        });
    });
}

module.exports = changeMolWeight;
