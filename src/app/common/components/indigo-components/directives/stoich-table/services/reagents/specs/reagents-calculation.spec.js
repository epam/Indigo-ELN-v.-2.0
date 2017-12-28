var changeMolWeight = require('./mol-weight.spec');
var changeWeight = require('./weight.spec');
var changeMol = require('./mol.spec');
var changeEq = require('./eq.spec');
var changeRxnRole = require('./rxn-role.spec');
var changeVolume = require('./volume.spec');
var changeMolarity = require('./molarity.spec');
var changePurity = require('./purity.spec');
var changeSaltCode = require('./salt-code.spec');
var changeDensity = require('./density.spec');

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
        changeMolWeight();
        changeWeight();
        changeMol();
        changeEq();
        changeRxnRole();
        changeVolume();
        changeMolarity();
        changePurity();
        changeSaltCode();
        changeDensity();
    });
});
