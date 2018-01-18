describe('service: calculationHelper', function() {
    var service;
    var rows;
    var clonedRows;

    beforeEach(angular.mock.module('indigoeln.indigoComponents'));

    beforeEach(angular.mock.inject(function(_calculationHelper_) {
        service = _calculationHelper_;
    }));

    it('should be defined', function() {
        expect(service).toBeDefined();
    });

    beforeEach(function() {
        rows = [
            {
                id: '77165c',
                weight: {value: 1, entered: false},
                molWeight: {value: 0, baseValue: 0},
                formula: {value: null, baseValue: 'C6 H6'},
                saltCode: {name: '01 - HYDROCHLORIDE', value: '1', regValue: '01', weight: 1},
                saltEq: {value: 1}
            }
        ];

        clonedRows = service.clone(rows);
    });

    describe('clone function', function() {
        it('should return copied array', function() {
            expect(clonedRows).not.toBe(rows);
            expect(clonedRows[0]).not.toBe(rows[0]);
        });
    });

    describe('findChangedRow function', function() {
        it('should find changed row', function() {
            var changedRow = service.findChangedRow(clonedRows, rows[0].id);

            expect(changedRow).toEqual(rows[0]);
        });
    });

    describe('getFormula function', function() {
        it('should return formula with SaltCode/Eq', function() {
            var expectedFormula = 'C6 H6*1(HYDROCHLORIDE)';
            var actualFormula = service.getFormula(rows[0]);

            expect(actualFormula).toBe(expectedFormula);
        });
    });
});
