var stoichCalculation = require('./stoich-calculation')();

describe('service: stoichCalculation', function() {

    it('should be defined', function() {
        expect(stoichCalculation).toBeDefined();
    });

    describe('computePureMol function', function() {
        it('should be 1.5', function() {
            var res = stoichCalculation.computePureMol(0.3, 0.2);
            expect(res).toBe(1.5);
        });
    });

    describe('computeDissolvedMol function', function() {
        it('should be 0.075', function() {
            var res = stoichCalculation.computeDissolvedMol(0.1, 0.75);
            expect(res).toBe(0.075);
        });
    });

    describe('computeMolByPurity function', function() {
        it('should be 0.6075', function() {
            var res = stoichCalculation.computeMolByPurity(81, 0.75);
            expect(res).toBe(0.6075);
        });
    });

    describe('computeWeight function', function() {
        it('should be 0.075', function() {
            var res = stoichCalculation.computeWeight(0.1, 0.75);
            expect(res).toBe(0.075);
        });
    });

    describe('computeWeightByPurity function', function() {
        it('should be 1500', function() {
            var res = stoichCalculation.computeWeightByPurity(5, 75);
            expect(res).toBe(1500);
        });
    });

    describe('computeEQ function', function() {
        it('should be 1.5', function() {
            var res = stoichCalculation.computeEQ(0.3, 0.2);
            expect(res).toBe(1.5);
        });
    });

    describe('computeMolWeight function', function() {
        it('should be 1.5', function() {
            var res = stoichCalculation.computeMolWeight(0.3, 0.2);
            expect(res).toBe(1.5);
        });
    });

    describe('computeMolWeightBySalt function', function() {
        it('should be 0.3', function() {
            var res = stoichCalculation.computeMolWeightBySalt(0.25, 0.5, 0.1);
            expect(res).toBe(0.3);
        });
    });

    describe('computeVolume function', function() {
        it('should be 1.5', function() {
            var res = stoichCalculation.computeVolume(0.3, 0.2);
            expect(res).toBe(1.5);
        });
    });

    describe('computeVolumeByDensity function', function() {
        it('should be 0.05', function() {
            var res = stoichCalculation.computeVolumeByDensity(100, 2);
            expect(res).toBe(0.05);
        });
    });
});
