var onMolWeightChanged = require('./specs/mol-weight.spec');
var onWeightChanged = require('./specs/weight.spec');
var onMolChanged = require('./specs/mol.spec');
var onEqChanged = require('./specs/eq.spec');
var onRxnRoleChanged = require('./specs/rxn-role.spec');
var onVolumeChanged = require('./specs/volume.spec');
var onMolarityChanged = require('./specs/molarity.spec');
var onPurityChanged = require('./specs/purity.spec');
var onSaltCodeChanged = require('./specs/salt-code.spec');
var onDensityChanged = require('./specs/density.spec');

describe('service: reagentsCalculation', function() {
    var service;

    beforeEach(angular.mock.module('indigoeln.stoichTable'));

    beforeEach(angular.mock.inject(function(_reagentsCalculation_) {
        service = _reagentsCalculation_;
    }));

    it('should be defined', function() {
        expect(service).toBeDefined();
    });

    describe('calculate function', function() {
        onMolWeightChanged();
        onWeightChanged();
        onMolChanged();
        onEqChanged();
        onRxnRoleChanged();
        onVolumeChanged();
        onMolarityChanged();
        onPurityChanged();
        onSaltCodeChanged();
        onDensityChanged();
    });
});
