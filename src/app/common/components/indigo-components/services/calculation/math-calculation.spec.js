var mathCalculation = require('./math-calculation');

describe('service: mathCalculation', function() {
    describe('computeMol function', function() {
        it('should be 1.5', function() {
            var res = mathCalculation.computeMol(0.3, 0.2, 100);
            expect(res).toBe(1.5);
        });
    });

    describe('computeDissolvedMol function', function() {
        it('should be 0.075', function() {
            var res = mathCalculation.computeDissolvedMol(0.1, 0.75);
            expect(res).toBe(0.075);
        });
    });

    describe('computeMolByPurity function', function() {
        it('should be 0.6075', function() {
            var res = mathCalculation.computeMolByPurity(0.75, 81, 100);
            expect(res).toBe(0.6075);
        });
    });

    describe('computeMolByEq function', function() {
        it('should be 4', function() {
            var res = mathCalculation.computeMolByEq(2, 4, 2);
            expect(res).toBe(4);
        });
    });

    describe('computeNonLimitingMolByEq function', function() {
        it('should be 2', function() {
            var res = mathCalculation.computeNonLimitingMolByEq(10, 1, 5);
            expect(res).toBe(2);
        });
    });

    describe('computeWeight function', function() {
        it('should be 0.075', function() {
            var res = mathCalculation.computeWeight(0.1, 0.75, 100);
            expect(res).toBe(0.075);
        });
    });
    describe('computeWeightByPurity function', function() {
        it('should be 1500', function() {
            var res = mathCalculation.computeWeightByPurity(75, 5, 100);
            expect(res).toBe(1500);
        });
    });

    describe('computeWeightByEq function', function() {
        it('should be 8', function() {
            var res = mathCalculation.computeWeightByEq(4, 4, 2);
            expect(res).toBe(8);
        });
    });

    describe('computeEq function', function() {
        it('should be 3', function() {
            var res = mathCalculation.computeEq(0.3, 2, 0.2);
            expect(res).toBe(3);
        });
    });

    describe('computeMolWeight function', function() {
        it('should be 1.5', function() {
            var res = mathCalculation.computeMolWeight(0.3, 0.2);
            expect(res).toBe(1.5);
        });
    });

    describe('computeMolWeightBySalt function', function() {
        it('should be 0.3', function() {
            var res = mathCalculation.computeMolWeightBySalt(0.25, 0.5, 0.1);
            expect(res).toBe(0.3);
        });
    });

    describe('computeVolumeByMolarity function', function() {
        it('should be 1.5', function() {
            var res = mathCalculation.computeVolumeByMolarity(0.3, 0.2);
            expect(res).toBe(1.5);
        });
    });

    describe('computeVolumeByDensity function', function() {
        it('should be 0.05', function() {
            var res = mathCalculation.computeVolumeByDensity(100, 2);
            expect(res).toBe(0.05);
        });
    });

    describe('computeWeightByDensity function', function() {
        it('should be 20', function() {
            var res = mathCalculation.computeWeightByDensity(0.005, 4);
            expect(res).toBe(20);
        });
    });

    describe('computeYield function', function() {
        it('should be 133', function() {
            var res = mathCalculation.computeYield(4, 3);
            expect(res).toBe(133);
        });
    });

    describe('multiply function', function() {
        it('should be 0.02', function() {
            var res = mathCalculation.multiply(0.005, 4);
            expect(res).toBe(0.02);
        });
    });

    describe('divide function', function() {
        it('should be 0.005', function() {
            var res = mathCalculation.divide(0.02, 4);
            expect(res).toBe(0.005);
        });
    });
});
