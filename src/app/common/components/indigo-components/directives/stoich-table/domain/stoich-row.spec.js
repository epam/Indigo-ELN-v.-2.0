var StoichRow = require('./stoich-row');
var fieldTypes = require('./field-types');

// TODO: add tests
describe('stoichRow', function() {
    var row;

    beforeEach(function() {
        row = new StoichRow();
    });

    it('should be defined', function() {
        expect(row).toBeDefined();
    });

    describe('areValuesPresent function', function() {
        it('purity and eq should be present', function() {
            expect(row.areValuesPresent([fieldTypes.stoicPurity, fieldTypes.eq])).toBe(true);
        });

        it('mol and weight should not be present', function() {
            expect(row.areValuesPresent([fieldTypes.mol, fieldTypes.weight])).toBe(false);
        });
    });
});
