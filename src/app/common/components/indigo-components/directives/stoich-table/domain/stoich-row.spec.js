var StoichRow = require('./stoich-row');

//TODO: add tests
describe('stoichRow', function() {
    var row;

    beforeEach(function() {
        row = new StoichRow();
    });

    it('should be defined', function() {
        expect(row).toBeDefined();
    });

    describe('areFieldsPresent function', function() {
        it('purity and eq should be present', function() {
            expect(row.areFieldsPresent(['stoicPurity', 'eq'])).toBe(true);
        });

        it('mol and weight should not be present', function() {
            expect(row.areFieldsPresent(['mol', 'weight'])).toBe(false);
        });
    });
});
