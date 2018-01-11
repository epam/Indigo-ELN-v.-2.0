var ReagentViewRow = require('../../../domain/reagent/view-row/reagent-view-row');
var ReagentRow = require('../../../domain/reagent/calculation-row/reagent-row');
var ProductRow = require('../../../domain/product/product-row');
var fieldTypes = require('../../../domain/field-types');

function onSaltChanged() {
    describe('Change salt code/eq', function() {
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

            firstRow = new ProductRow();
            secondRow = new ProductRow();

            rows.push(firstRow);
            rows.push(secondRow);

            firstRow.molWeight.value = 3;
            firstRow.molWeight.baseValue = 3;
            firstRow.formula.baseValue = 'C6 H6';
            firstRow.theoWeight.value = 12;
            firstRow.theoMoles.value = 4;
            firstRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
            firstRow.saltEq.value = 12;

            var limitingRow = new ReagentRow(new ReagentViewRow());
            limitingRow.limiting = true;
            limitingRow.mol.value = 4;

            productsData = {
                rows: rows,
                changedRow: firstRow,
                changedField: fieldTypes.saltCode,
                limitingRow: limitingRow
            };
        });

        it('should update molWeight and formula, then compute theoMoles and theoWeight', function() {
            var calculatedRows = service.calculate(productsData);

            expect(calculatedRows[0].molWeight.value).toBe(15);
            expect(calculatedRows[0].formula.value).toBe('C6 H6*12(HYDROCHLORIDE)');
            expect(calculatedRows[0].theoMoles.value).toBe(4);
            expect(calculatedRows[0].theoWeight.value).toBe(60);
        });

        it('should set original mol weight and formula', function() {
            rows = service.calculate(productsData);
            firstRow = rows[0];

            expect(firstRow.molWeight.value).toBe(15);
            expect(firstRow.theoMoles.value).toBe(4);
            expect(firstRow.theoWeight.value).toBe(60);

            firstRow.saltCode = {name: '00 - Parent Structure', value: '0', regValue: '00', weight: 0};

            productsData.rows = rows;
            productsData.changedRow = firstRow;

            rows = service.calculate(productsData);
            firstRow = rows[0];

            expect(firstRow.molWeight.value).toBe(3);
            expect(firstRow.theoMoles.value).toBe(4);
            expect(firstRow.theoWeight.value).toBe(12);
            expect(firstRow.formula.value).toBe('C6 H6');
        });
    });
}

module.exports = onSaltChanged;
