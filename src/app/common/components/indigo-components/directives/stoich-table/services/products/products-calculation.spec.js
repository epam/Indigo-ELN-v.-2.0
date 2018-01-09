var onLimitingMolChanged = require('./specs/limiting-mol.spec');
var onSaltChanged = require('./specs/salt-code.spec');
var onEqChanged = require('./specs/eq.spec');

describe('service: productsCalculation', function() {
    var service;

    beforeEach(angular.mock.module('indigoeln.indigoComponents'));

    beforeEach(angular.mock.inject(function(_productsCalculation_) {
        service = _productsCalculation_;
    }));

    it('should be defined', function() {
        expect(service).toBeDefined();
    });

    describe('calculate function', function() {
        onLimitingMolChanged();
        onSaltChanged();
        onEqChanged();
    });
});
