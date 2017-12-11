var StoichRow = require('../stoich-row');
var stoichTable = require('./stoich-table');
var changeMolWeight = require('./on-field-changed/mol-weight.spec');
var changeWeight = require('./on-field-changed/weight.spec');
var changeMol = require('./on-field-changed/mol.spec');
var changeEq = require('./on-field-changed/eq.spec');
var changeRxnRole = require('./on-field-changed/rxn-role.spec');
var changeVolume = require('./on-field-changed/volume.spec');
var changeMolarity = require('./on-field-changed/molarity.spec');
var changePurity = require('./on-field-changed/purity.spec');
var changeSaltCode = require('./on-field-changed/salt-code.spec');
var changeDensity = require('./on-field-changed/density.spec');

describe('stoichTable', function() {
    var service;

    beforeEach(function() {
        service = stoichTable({table: {}});
    });

    it('should be defined', function() {
        expect(service).toBeDefined();
    });

    describe('setStoichTable function', function() {
        it('stoichTable should be defined', function() {
            service.setStoichTable({product: [], reactants: []});
            expect(service.getStoichTable().reactants).toBeDefined();
        });
    });

    describe('addRow function', function() {
        beforeEach(function() {
            var config = {
                table: {product: [], reactants: []}
            };

            service = stoichTable(config);
        });

        it('should add new row to stoichTable', function() {
            var stoichRow = new StoichRow();
            service.addRow(stoichRow);

            expect(stoichRow.limiting).toBeTruthy();
        });

        it('limiting row is exist, should add row and set mol of limiting row', function() {
            var limitingRow = new StoichRow();
            var otherRow = new StoichRow();
            limitingRow.mol.value = 12;
            otherRow.molWeight.value = 2;

            service.addRow(limitingRow);
            service.addRow(otherRow);

            expect(limitingRow.limiting).toBeTruthy();
            expect(otherRow.limiting).toBeFalsy();
            expect(otherRow.mol.value).toBe(12);
            expect(otherRow.weight.value).toBe(24);
        });
    });

    describe('onFieldValueChanged function', function() {
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
