var ReagentViewRow = require('../../../domain/reagent/view-row/reagent-view-row');
var ReagentRow = require('../../../domain/reagent/calculation-row/reagent-row');
var ProductViewRow = require('../../../domain/product/view-row/product-view-row');
var ProductRow = require('../../../domain/product/calculation-row/product-row');
var fieldTypes = require('../../../domain/field-types');

function onEqChanged() {
    describe('Change eq', function() {
        var service;
        var productsData;
        var rows;
        var firstRow;
        var secondRow;

        beforeEach(angular.mock.inject(function(_productsCalculation_) {
            service = _productsCalculation_;
        }));

        beforeEach(function() {
            rows = [];

            firstRow = new ProductRow(new ProductViewRow());
            secondRow = new ProductRow(new ProductViewRow());

            rows.push(firstRow);
            rows.push(secondRow);

            firstRow.molWeight.value = 3;
            firstRow.molWeight.baseValue = 3;
            firstRow.eq.value = 4;

            var limitingRow = new ReagentRow(new ReagentViewRow());
            limitingRow.limiting = true;
            limitingRow.mol.value = 4;

            productsData = {
                rows: rows,
                idOfChangedRow: firstRow.id,
                changedField: fieldTypes.eq,
                limitingRow: limitingRow
            };
        });

        it('should recalculate theoMoles and then theoWeight', function() {
            var calculatedRows = service.calculate(productsData);

            expect(calculatedRows[0].theoWeight.value).toBe(48);
            expect(calculatedRows[0].theoMoles.value).toBe(16);
        });
    });
}

module.exports = onEqChanged;
