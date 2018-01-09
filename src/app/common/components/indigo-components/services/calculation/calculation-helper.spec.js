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
            {weight: {value: 1, entered: false}}
        ];

        clonedRows = service.getClonedRows(rows);
    });

    describe('getClonedRows function', function() {
        it('should return copied array', function() {
            expect(clonedRows).not.toBe(rows);
            expect(clonedRows[0]).not.toBe(rows[0]);
        });
    });

    describe('findChangedRow function', function() {
        it('should find changed row', function() {
            var changedRow = service.findChangedRow(clonedRows, rows[0]);

            expect(changedRow).toEqual(rows[0]);
        });
    });
});
