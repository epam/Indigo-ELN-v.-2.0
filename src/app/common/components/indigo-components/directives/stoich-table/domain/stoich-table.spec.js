var StoichRow = require('./stoich-row');
var stoichTable = require('./stoich-table');

describe('stoichTable', function() {
    var service;

    beforeEach(function() {
        service = stoichTable();
    });

    it('should be defined', function() {
        expect(service).toBeDefined();
    });

    describe('setStoichTable function', function() {
        it('stoichTable should not be defined', function() {
            expect(service.getStoichTable()).toBeUndefined();
        });

        it('stoichTable should be defined', function() {
            service.setStoichTable({});
            expect(service.getStoichTable()).toBeDefined();
        });
    });

    describe('addRow function', function() {
        beforeEach(function() {
            service.setStoichTable({product: [], reactants: []});
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

            console.log(otherRow);
            expect(limitingRow.limiting).toBeTruthy();
            expect(otherRow.limiting).toBeFalsy();
            expect(otherRow.mol.value).toBe(12);
            expect(otherRow.weight.value).toBe(24);
        });
    });

    describe('onColumnValueChanged function', function() {
        var stoichTable;

        beforeEach(function() {
            stoichTable = {product: [], reactants: []};
            service.setStoichTable(stoichTable);
        });

        describe('Change mol weight', function() {
            it('weight is defined, row is limiting, should compute mol', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 23;
                stoichRow.weight.value = 23;
                service.addRow(stoichRow);

                service.onColumnValueChanged(stoichRow, 'molWeight');

                expect(stoichRow.mol.value).toBe(1);
            });

            it('mol is defined, row is not limiting, should compute weight', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 23;
                stoichRow.mol.value = 2;
                stoichRow.limiting = false;

                service.onColumnValueChanged(stoichRow, 'molWeight');

                expect(stoichRow.weight.value).toBe(46);
            });
        });

        describe('Change weight', function() {
            it('mol weight is defined; should compute mol, it should be 1', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 23;
                stoichRow.weight.value = 23;
                service.addRow(stoichRow);

                service.onColumnValueChanged(stoichRow, 'weight');

                expect(stoichRow.mol.value).toBe(1);
            });

            it('mol weight is defined, row is limiting; should compute mol and change mol in other rows', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 10;
                limitingRow.weight.value = 10;
                service.addRow(limitingRow);

                var otherRow = new StoichRow();
                otherRow.molWeight.value = 5;
                service.addRow(otherRow);

                service.onColumnValueChanged(limitingRow, 'weight');

                expect(limitingRow.mol.value).toBe(1);
                expect(otherRow.weight.value).toBe(5);
                expect(otherRow.mol.value).toBe(1);
            });

            it('mol weight is not defined, but mol is defined; should compute mol weight', function() {
                var stoichRow = new StoichRow();
                stoichRow.weight.value = 20;
                stoichRow.mol.value = 10;

                service.onColumnValueChanged(stoichRow, 'weight');

                expect(stoichRow.molWeight.value).toBe(2);
            });

            it('mol weight is defined, row is limiting, weight is removed or 0; mol of limiting row should be 0 and' +
                ' mol of other rows are the same', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 10;
                limitingRow.weight.value = 0;
                limitingRow.mol.value = 10;
                service.addRow(limitingRow);

                var otherRow = new StoichRow();
                otherRow.molWeight.value = 1;
                otherRow.mol.value = 10;
                otherRow.weight.value = 10;
                service.addRow(otherRow);

                service.onColumnValueChanged(limitingRow, 'weight');

                expect(limitingRow.mol.value).toBe(0);
                expect(limitingRow.eq.value).toBe(1);
                expect(otherRow.mol.value).toBe(10);
            });

            it('mol weight is defined, row is not limiting, weight is removed or 0; mol and eq should be default', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 10;
                stoichRow.weight.value = 0;
                stoichRow.mol.value = 10;

                service.onColumnValueChanged(stoichRow, 'weight');

                expect(stoichRow.mol.value).toBe(0);
                expect(stoichRow.eq.value).toBe(1);
            });

            it('density is defined, should compute volume', function() {
                var stoichRow = new StoichRow();
                stoichRow.density.value = 10;
                stoichRow.weight.value = 2;

                service.onColumnValueChanged(stoichRow, 'weight');

                expect(stoichRow.volume.value).toBe(0.0002);
            });

            it('mol weight, volume, mol, molarity, eq are defined, should recompute volume, mol and eq', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 2;
                limitingRow.mol.value = 20;
                limitingRow.volume.value = 4;
                limitingRow.molarity.value = 5;
                service.addRow(limitingRow);

                limitingRow.weight.value = 10;
                limitingRow.weight.entered = true;

                service.onColumnValueChanged(limitingRow, 'weight');

                expect(limitingRow.mol.value).toBe(5);
                expect(limitingRow.volume.value).toBe(1);
                expect(limitingRow.molarity.value).toBe(5);
                expect(limitingRow.weight.value).toBe(10);
                expect(limitingRow.eq.value).toBe(1);
                expect(limitingRow.molWeight.value).toBe(2);
            });

            it('mol weight, volume, mol, molarity, eq are defined, remove weight, should reset volume and mol', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 2;
                limitingRow.mol.value = 20;
                limitingRow.volume.value = 4;
                limitingRow.molarity.value = 5;
                service.addRow(limitingRow);

                limitingRow.weight.value = 0;
                limitingRow.weight.entered = true;

                service.onColumnValueChanged(limitingRow, 'weight');

                expect(limitingRow.mol.value).toBe(0);
                expect(limitingRow.volume.value).toBe(0);
                expect(limitingRow.molarity.value).toBe(5);
                expect(limitingRow.weight.value).toBe(0);
                expect(limitingRow.eq.value).toBe(1);
                expect(limitingRow.molWeight.value).toBe(2);
            });

            it('mol weight, volume, mol, molarity, eq are defined, correct weight, should compute mol, volume', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 2;
                limitingRow.mol.value = 20;
                limitingRow.volume.value = 4;
                limitingRow.volume.entered = true;
                limitingRow.molarity.value = 5;
                limitingRow.molarity.entered = true;
                service.addRow(limitingRow);

                limitingRow.weight.value = 12;
                limitingRow.weight.entered = true;

                service.onColumnValueChanged(limitingRow, 'weight');

                expect(limitingRow.mol.value).toBe(6);
                expect(limitingRow.volume.value).toBe(1.2);
                expect(limitingRow.volume.entered).toBeFalsy();
                expect(limitingRow.molarity.value).toBe(5);
                expect(limitingRow.molarity.entered).toBeTruthy();
                expect(limitingRow.weight.value).toBe(12);
                expect(limitingRow.weight.entered).toBeTruthy();
                expect(limitingRow.eq.value).toBe(1);
                expect(limitingRow.molWeight.value).toBe(2);
            });
        });

        describe('Change mol', function() {
            it('mol weight is defined, row is not limiting; should compute weight, it should be 110', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 10;
                stoichRow.mol.value = 11;
                stoichRow.limiting = false;

                service.onColumnValueChanged(stoichRow, 'mol');

                expect(stoichRow.weight.value).toBe(110);
            });

            it('mol weight is defined, row is not limiting; mol is removed or 0, weight should be 0', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 10;
                stoichRow.mol.value = 0;
                stoichRow.limiting = false;

                service.onColumnValueChanged(stoichRow, 'mol');

                expect(stoichRow.weight.value).toBe(0);
            });

            it('mol weight is defined, row is limiting; should compute weight and update mol in other rows', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 10;
                limitingRow.mol.value = 11;
                service.addRow(limitingRow);

                var otherRow = new StoichRow();
                otherRow.molWeight.value = 2;
                otherRow.weight.value = 32;
                service.addRow(otherRow);

                service.onColumnValueChanged(limitingRow, 'mol');

                expect(limitingRow.weight.value).toBe(110);
                expect(otherRow.mol.value).toBe(11);
                expect(otherRow.weight.value).toBe(22);
            });

            it('mol weight is defined, row is limiting, mol is removed or 0; weight of limiting row should be 0 and' +
                ' mol of other rows are the same', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 10;
                limitingRow.weight.value = 22;
                limitingRow.mol.value = 0;
                service.getStoichTable().reactants.push(limitingRow);

                var otherRow = new StoichRow();
                otherRow.molWeight.value = 1;
                otherRow.mol.value = 10;
                otherRow.weight.value = 10;
                service.getStoichTable().reactants.push(otherRow);

                service.onColumnValueChanged(limitingRow, 'mol');

                expect(limitingRow.weight.value).toBe(0);
                expect(otherRow.mol.value).toBe(10);
                expect(otherRow.weight.value).toBe(10);
            });

            it('molarity is entered, volume is computed, mol set to 0, should reset volume and weight', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 10;
                stoichRow.weight.value = 22;
                stoichRow.volume.value = 2;
                stoichRow.molarity.value = 2;

                stoichRow.mol.value = 0;

                service.onColumnValueChanged(stoichRow, 'mol');

                expect(stoichRow.weight.value).toBe(0);
                expect(stoichRow.volume.value).toBe(0);
            });

            it('mol weight is not defined, but weight is defined, should compute mol weight', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 0;
                stoichRow.mol.value = 11;
                stoichRow.weight.value = 11;

                service.onColumnValueChanged(stoichRow, 'mol');

                expect(stoichRow.molWeight.value).toBe(1);
            });

            it('mol weight, volume, weight, molarity, eq are defined, should recompute volume, weight and eq', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 2;
                limitingRow.weight.value = 40;
                limitingRow.volume.value = 10;
                limitingRow.molarity.value = 2;
                service.addRow(limitingRow);

                limitingRow.mol.value = 10;

                service.onColumnValueChanged(limitingRow, 'mol');

                expect(limitingRow.weight.value).toBe(20);
                expect(limitingRow.volume.value).toBe(5);
                expect(limitingRow.mol.value).toBe(10);
                expect(limitingRow.molarity.value).toBe(2);
                expect(limitingRow.eq.value).toBe(1);
                expect(limitingRow.molWeight.value).toBe(2);
            });
        });

        describe('Change eq', function() {
            it('row is not limiting, should update mol and weight', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 10;
                stoichRow.weight.value = 110;
                stoichRow.weight.entered = true;
                stoichRow.mol.value = 11;
                stoichRow.mol.entered = true;
                stoichRow.eq.value = 2;
                stoichRow.limiting = false;

                service.onColumnValueChanged(stoichRow, 'eq');

                expect(stoichRow.weight.value).toBe(220);
                expect(stoichRow.weight.entered).toBeFalsy();
                expect(stoichRow.mol.entered).toBeFalsy();
            });

            it('row is limiting, should update mol in other lines', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 10;
                limitingRow.weight.value = 100;
                limitingRow.weight.entered = true;
                limitingRow.mol.value = 10;
                limitingRow.eq.value = 2;
                limitingRow.eq.entered = true;
                service.addRow(limitingRow);

                var otherRow = new StoichRow();
                otherRow.molWeight.value = 10;
                service.addRow(otherRow);

                service.onColumnValueChanged(limitingRow, 'eq');

                expect(otherRow.weight.value).toBe(50);
                expect(otherRow.mol.value).toBe(5);
                expect(otherRow.eq.value).toBe(1);
            });

            it('complex test.' +
                '1. Apply new EQ in Limiting row - non limiting Mol and Weight are updated,' +
                '2. Enter manual weight in non limiting row - Mols are recalculated for this particular row' +
                '3. Update EQ for Limiting' +
                'Expected results: Mols are not update in Limiting and Non limiting row.' +
                'EQ of non limiting row is adjusted by formula:' +
                '(Non Limiting Mol* Limiting EQ)/Limiting Mol', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 10;
                limitingRow.weight.value = 100;
                limitingRow.weight.entered = true;
                limitingRow.mol.value = 10;
                limitingRow.eq.value = 2;
                limitingRow.eq.entered = true;
                service.addRow(limitingRow);

                var otherRow = new StoichRow();
                otherRow.molWeight.value = 10;
                service.addRow(otherRow);

                service.onColumnValueChanged(limitingRow, 'eq');

                expect(otherRow.weight.value).toBe(50);
                expect(otherRow.mol.value).toBe(5);
                expect(otherRow.eq.value).toBe(1);

                otherRow.weight.value = 5;
                otherRow.weight.entered = true;

                service.onColumnValueChanged(otherRow, 'weight');

                expect(otherRow.weight.value).toBe(5);
                expect(otherRow.weight.entered).toBeTruthy();
                expect(otherRow.mol.value).toBe(0.5);
                expect(otherRow.eq.value).toBe(0.05);

                limitingRow.eq.value = 4;

                service.onColumnValueChanged(limitingRow, 'eq');

                expect(otherRow.weight.value).toBe(5);
                expect(otherRow.weight.entered).toBeTruthy();
                expect(otherRow.mol.value).toBe(0.5);
                expect(otherRow.eq.value).toBe(0.2);
            });

            it('row is not limiting, set eq to 2 and then to 1, should return original values', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 10;
                stoichRow.weight.value = 100;
                stoichRow.weight.entered = true;
                stoichRow.mol.value = 10;
                stoichRow.eq.value = 2;
                stoichRow.eq.entered = true;


                service.onColumnValueChanged(stoichRow, 'eq');

                expect(stoichRow.weight.value).toBe(200);
                expect(stoichRow.mol.value).toBe(20);
                expect(stoichRow.eq.value).toBe(2);

                stoichRow.eq.value = 1;

                service.onColumnValueChanged(stoichRow, 'eq');

                expect(stoichRow.weight.value).toBe(100);
                expect(stoichRow.mol.value).toBe(10);
                expect(stoichRow.eq.value).toBe(1);
            });
        });

        describe('Change rxnRole', function() {
            it('set solvent role, should reset and disable weight, mol, eq, density, stoicPurity, molarity,' +
                ' saltCode, saltEq', function() {
                var stoichRow = new StoichRow();
                stoichRow.weight.value = 10;
                stoichRow.mol.value = 11;
                stoichRow.eq.value = 2;
                stoichRow.density.value = 3;
                stoichRow.stoicPurity.value = 11;
                stoichRow.molarity.value = 11;
                stoichRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
                stoichRow.saltEq.value = 11;
                stoichRow.rxnRole = {name: 'SOLVENT', entered: true};

                service.onColumnValueChanged(stoichRow, 'rxnRole');

                expect(stoichRow.weight.value).toBe(0);
                expect(stoichRow.weight.readonly).toBeTruthy();
                expect(stoichRow.mol.value).toBe(0);
                expect(stoichRow.mol.readonly).toBeTruthy();
                expect(stoichRow.eq.value).toBe(1);
                expect(stoichRow.eq.readonly).toBeTruthy();
                expect(stoichRow.density.value).toBe(0);
                expect(stoichRow.density.readonly).toBeTruthy();
                expect(stoichRow.stoicPurity.value).toBe(100);
                expect(stoichRow.stoicPurity.readonly).toBeTruthy();
                expect(stoichRow.molarity.value).toBe(0);
                expect(stoichRow.molarity.readonly).toBeTruthy();
                expect(stoichRow.saltCode.weight).toBe(0);
                expect(stoichRow.saltCode.readonly).toBeTruthy();
                expect(stoichRow.saltEq.value).toBe(0);
                expect(stoichRow.saltEq.readonly).toBeTruthy();
            });

            it('row is limiting, set solvent role, should reset and disable fields and set limiting to the next line', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 10;
                limitingRow.weight.value = 110;
                limitingRow.mol.value = 11;
                limitingRow.rxnRole = {name: 'SOLVENT', entered: true};
                service.addRow(limitingRow);

                var otherRow = new StoichRow();
                otherRow.molWeight.value = 5;
                service.addRow(otherRow);

                service.onColumnValueChanged(limitingRow, 'rxnRole');

                expect(limitingRow.limiting).toBeFalsy();
                expect(otherRow.limiting).toBeTruthy();
            });

            it('previous role was solvent, set reactant, should reset volume, enable fields and set mol of limiting' +
                ' row', function() {
                var readonlyFields = ['weight', 'mol', 'eq', 'molarity', 'density', 'limiting', 'stoicPurity', 'saltCode', 'saltEq'];
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 10;
                limitingRow.weight.value = 100;
                limitingRow.mol.value = 10;
                service.addRow(limitingRow);

                var otherRow = new StoichRow();
                otherRow.molWeight.value = 5;
                otherRow.volume.value = 5;
                otherRow.setReadonly(readonlyFields, true);
                service.addRow(otherRow);

                service.onColumnValueChanged(otherRow, 'rxnRole', 'SOLVENT');

                expect(otherRow.volume.value).toBe(0);
                expect(otherRow.weight.value).toBe(50);
                expect(otherRow.weight.readonly).toBeFalsy();
                expect(otherRow.mol.value).toBe(10);
                expect(otherRow.mol.readonly).toBeFalsy();
                expect(otherRow.eq.value).toBe(1);
                expect(otherRow.eq.readonly).toBeFalsy();
                expect(otherRow.density.value).toBe(0);
                expect(otherRow.density.readonly).toBeFalsy();
                expect(otherRow.stoicPurity.value).toBe(100);
                expect(otherRow.stoicPurity.readonly).toBeFalsy();
                expect(otherRow.molarity.value).toBe(0);
                expect(otherRow.molarity.readonly).toBeFalsy();
                expect(otherRow.saltCode.weight).toBe(0);
                expect(otherRow.saltCode.readonly).toBeFalsy();
                expect(otherRow.saltEq.value).toBe(0);
                expect(otherRow.saltEq.readonly).toBeFalsy();
            });
        });

        describe('Change volume', function() {
            it('should set mol, weight to 0 and limiting to false, set limiting to next line with solvent role and' +
                ' eq=1, should not update rows with new limiting mol', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 10;
                limitingRow.weight.value = 22;
                limitingRow.mol.value = 11;
                limitingRow.limiting = true;
                limitingRow.volume.value = 4;
                service.getStoichTable().reactants.push(limitingRow);

                var secondRow = new StoichRow();
                secondRow.molWeight.value = 1;
                secondRow.mol.value = 10;
                secondRow.weight.value = 10;
                secondRow.eq.value = 2;
                service.getStoichTable().reactants.push(secondRow);

                var thirdRow = new StoichRow();
                thirdRow.molWeight.value = 1;
                thirdRow.mol.value = 5;
                thirdRow.weight.value = 5;
                service.getStoichTable().reactants.push(thirdRow);

                service.onColumnValueChanged(limitingRow, 'volume');

                expect(limitingRow.weight.value).toBe(0);
                expect(limitingRow.mol.value).toBe(0);
                expect(limitingRow.limiting).toBeFalsy();
                expect(limitingRow.molWeight.value).toBe(10);

                expect(secondRow.limiting).toBeFalsy();
                expect(secondRow.weight.value).toBe(10);
                expect(secondRow.mol.value).toBe(10);

                expect(thirdRow.limiting).toBeTruthy();
                expect(thirdRow.mol.value).toBe(5);
                expect(thirdRow.weight.value).toBe(5);
            });

            it('row is not limiting; should set mol, weight to 0', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 10;
                stoichRow.weight.value = 22;
                stoichRow.mol.value = 11;
                stoichRow.limiting = false;

                stoichRow.volume.value = 4;

                service.onColumnValueChanged(stoichRow, 'volume');

                expect(stoichRow.weight.value).toBe(0);
                expect(stoichRow.mol.value).toBe(0);
                expect(stoichRow.limiting).toBeFalsy();
                expect(stoichRow.molWeight.value).toBe(10);
            });

            it('row is not limiting; weight, mol are 0, set volume to 0 or remove, should set mol from limiting row', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 2;
                limitingRow.weight.value = 22;
                limitingRow.mol.value = 11;
                service.addRow(limitingRow);

                var secondRow = new StoichRow();
                secondRow.molWeight.value = 3;
                secondRow.mol.value = 0;
                secondRow.weight.value = 0;
                service.addRow(secondRow);

                service.onColumnValueChanged(secondRow, 'volume');

                expect(limitingRow.limiting).toBeTruthy();
                expect(limitingRow.mol.value).toBe(11);

                expect(secondRow.limiting).toBeFalsy();
                expect(secondRow.mol.value).toBe(11);
                expect(secondRow.weight.value).toBe(33);
            });

            it('molarity, molWeight are defined, should compute mol, weight and eq', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 2;
                stoichRow.molarity.value = 4;

                stoichRow.volume.value = 5;

                service.onColumnValueChanged(stoichRow, 'volume');

                expect(stoichRow.molarity.value).toBe(4);
                expect(stoichRow.volume.value).toBe(5);
                expect(stoichRow.mol.value).toBe(20);
                expect(stoichRow.weight.value).toBe(40);
                expect(stoichRow.eq.value).toBe(1);
            });

            it('molarity, molWeight, mol are defined, set volume to 0, should remove mol, weight', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 2;
                stoichRow.molarity.value = 4;
                stoichRow.mol.value = 20;
                stoichRow.weight.value = 40;

                stoichRow.volume.value = 0;

                service.onColumnValueChanged(stoichRow, 'volume');

                expect(stoichRow.molarity.value).toBe(4);
                expect(stoichRow.volume.value).toBe(0);
                expect(stoichRow.mol.value).toBe(0);
                expect(stoichRow.weight.value).toBe(0);
                expect(stoichRow.eq.value).toBe(1);
            });

            it('density is defined, should compute weight', function() {
                var stoichRow = new StoichRow();
                stoichRow.volume.value = 5;
                stoichRow.density.value = 20;

                service.onColumnValueChanged(stoichRow, 'volume');

                expect(stoichRow.weight.value).toBe(100000);
            });

            it('density is defined, should compute weight and mol', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 1000;
                stoichRow.volume.value = 5;
                stoichRow.density.value = 20;

                service.onColumnValueChanged(stoichRow, 'volume');

                expect(stoichRow.mol.value).toBe(100);
                expect(stoichRow.weight.value).toBe(100000);
            });

            it('density is defined, set volume 0, should set weight 0', function() {
                var stoichRow = new StoichRow();
                stoichRow.volume.value = 0;
                stoichRow.weight.value = 3;
                stoichRow.density.value = 20;

                service.onColumnValueChanged(stoichRow, 'volume');

                expect(stoichRow.weight.value).toBe(0);
            });
        });

        describe('Change molarity', function() {
            it('volume is defined, should compute mol, weight; eq should be default, therefore there is not limiting' +
                ' row', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 2;
                stoichRow.weight.value = 22;
                stoichRow.mol.value = 11;
                stoichRow.volume.value = 4;
                stoichRow.molarity.value = 3;
                stoichRow.limiting = false;
                service.addRow(stoichRow);

                service.onColumnValueChanged(stoichRow, 'molarity');

                expect(stoichRow.mol.value).toBe(12);
                expect(stoichRow.weight.value).toBe(24);
                expect(stoichRow.limiting).toBeTruthy();
                expect(stoichRow.eq.value).toBe(1);
                expect(stoichRow.molWeight.value).toBe(2);
                expect(stoichRow.volume.value).toBe(4);
            });

            it('volume is defined, should compute mol, weight; eq should be 6, therefore there is limiting row', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 5;
                limitingRow.weight.value = 10;
                limitingRow.mol.value = 2;
                limitingRow.limiting = true;
                service.getStoichTable().reactants.push(limitingRow);

                var otherRow = new StoichRow();
                otherRow.molWeight.value = 2;
                otherRow.volume.value = 4;
                otherRow.molarity.value = 3;
                service.getStoichTable().reactants.push(otherRow);

                service.onColumnValueChanged(otherRow, 'molarity');

                expect(otherRow.mol.value).toBe(12);
                expect(otherRow.weight.value).toBe(24);
                expect(otherRow.limiting).toBeFalsy();
                expect(otherRow.eq.value).toBe(6);
                expect(otherRow.molWeight.value).toBe(2);
                expect(otherRow.volume.value).toBe(4);
            });

            it('volume is computed, mol is manually entered, volume should be 0', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 2;
                stoichRow.weight.value = 30;
                stoichRow.mol.value = 15;
                stoichRow.mol.entered = true;
                stoichRow.volume.value = 5;
                stoichRow.molarity.value = 0;

                service.onColumnValueChanged(stoichRow, 'molarity');

                expect(stoichRow.volume.value).toBe(0);
                expect(stoichRow.mol.value).toBe(15);
                expect(stoichRow.mol.entered).toBeTruthy();
            });

            it('mol is computed, volume is manually entered, mol should be 0', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 2;
                stoichRow.weight.value = 30;
                stoichRow.mol.value = 15;
                stoichRow.volume.value = 5;
                stoichRow.volume.entered = true;
                stoichRow.molarity.value = 0;

                service.onColumnValueChanged(stoichRow, 'molarity');

                expect(stoichRow.volume.value).toBe(5);
                expect(stoichRow.volume.entered).toBeTruthy();
                expect(stoichRow.mol.value).toBe(0);
                expect(stoichRow.weight.value).toBe(0);
            });

            it('volume is not defined, mol is defined, should compute volume', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 2;
                stoichRow.weight.value = 30;
                stoichRow.mol.value = 15;

                stoichRow.molarity.value = 3;

                service.onColumnValueChanged(stoichRow, 'molarity');

                expect(stoichRow.volume.value).toBe(5);
            });
        });

        describe('Change stoicPurity', function() {
            it('row is limiting, should compute mol, then weight and update mol in other rows', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 2;
                limitingRow.weight.value = 30;
                limitingRow.mol.value = 15;
                limitingRow.stoicPurity.value = 10;
                service.addRow(limitingRow);

                var otherRow = new StoichRow();
                otherRow.molWeight.value = 3;
                otherRow.weight.value = 45;
                otherRow.mol.value = 15;
                service.addRow(otherRow);

                service.onColumnValueChanged(limitingRow, 'stoicPurity');

                expect(limitingRow.mol.value).toBe(1.5);
                expect(limitingRow.weight.value).toBe(3);
                expect(otherRow.mol.value).toBe(1.5);
                expect(otherRow.weight.value).toBe(4.5);
            });

            it('row is not limiting, should compute only weight', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 3;
                stoichRow.weight.value = 45;
                stoichRow.mol.value = 15;
                stoichRow.stoicPurity.value = 10;
                stoichRow.limiting = false;

                service.onColumnValueChanged(stoichRow, 'stoicPurity');

                expect(stoichRow.mol.value).toBe(15);
                expect(stoichRow.weight.value).toBe(450);
            });

            it('row is not limiting, set purity to 50 and to 100, weight should be original', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 2;
                stoichRow.weight.value = 30;
                stoichRow.mol.value = 15;
                stoichRow.stoicPurity.value = 50;
                stoichRow.limiting = false;

                service.onColumnValueChanged(stoichRow, 'stoicPurity');

                expect(stoichRow.mol.value).toBe(15);
                expect(stoichRow.weight.value).toBe(60);

                stoichRow.stoicPurity.value = 100;
                service.onColumnValueChanged(stoichRow, 'stoicPurity');

                expect(stoichRow.mol.value).toBe(15);
                expect(stoichRow.weight.value).toBe(30);
            });

            it('row is not limiting, weight is manually set, should compute mol and update eq', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 3;
                stoichRow.weight.value = 45;
                stoichRow.weight.entered = true;
                stoichRow.mol.value = 15;
                stoichRow.stoicPurity.value = 10;
                stoichRow.limiting = false;

                service.onColumnValueChanged(stoichRow, 'stoicPurity');

                expect(stoichRow.mol.value).toBe(1.5);
                expect(stoichRow.weight.value).toBe(45);
            });
        });

        describe('Change salt code/eq', function() {
            it('row is limiting, should compute mol, then weight and update mol in other rows', function() {
                var limitingRow = new StoichRow();
                limitingRow.molWeight.value = 3;
                limitingRow.weight.value = 45;
                limitingRow.mol.value = 15;
                limitingRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
                limitingRow.saltEq.value = 12;
                service.addRow(limitingRow);

                var otherRow = new StoichRow();
                service.addRow(otherRow);

                service.onColumnValueChanged(limitingRow, 'saltEq');

                expect(limitingRow.molWeight.value).toBe(15);
                expect(limitingRow.mol.value).toBe(3);
                expect(otherRow.mol.value).toBe(3);
            });

            it('row is not limiting, should compute weight', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 3;
                stoichRow.weight.value = 45;
                stoichRow.mol.value = 15;
                stoichRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
                stoichRow.saltEq.value = 12;
                stoichRow.limiting = false;

                service.onColumnValueChanged(stoichRow, 'saltEq');

                expect(stoichRow.molWeight.value).toBe(15);
                expect(stoichRow.weight.value).toBe(225);
                expect(stoichRow.mol.value).toBe(15);
            });

            // it('row is not limiting, should set original mol weight', function() {
            //     var stoichRow = new StoichRow();
            //     stoichRow.molWeight.value = 3;
            //     stoichRow.weight.value = 45;
            //     stoichRow.mol.value = 15;
            //     stoichRow.saltCode = {name: '01 - HYDROCHLORIDE', value: 1, regValue: '01', weight: 1};
            //     stoichRow.saltEq.value = 12;
            //     stoichRow.limiting = false;
            //
            //     service.onColumnValueChanged(stoichRow, 'saltEq');
            //
            //     expect(stoichRow.molWeight.value).toBe(15);
            //     expect(stoichRow.weight.value).toBe(225);
            //     expect(stoichRow.mol.value).toBe(15);
            // });
        });

        describe('Change density', function() {
            it('weight is defined, should compute volume', function() {
                var stoichRow = new StoichRow();
                stoichRow.weight.value = 20;
                stoichRow.density.value = 4;

                service.onColumnValueChanged(stoichRow, 'density');

                expect(stoichRow.volume.value).toBe(0.005);
            });

            it('volume is defined, should compute weight', function() {
                var stoichRow = new StoichRow();
                stoichRow.volume.value = 0.005;
                stoichRow.density.value = 4;

                service.onColumnValueChanged(stoichRow, 'density');

                expect(stoichRow.weight.value).toBe(20);
            });

            it('set density 0, volume is entered, weight should be 0', function() {
                var stoichRow = new StoichRow();
                stoichRow.molWeight.value = 1;
                stoichRow.mol.value = 2;
                stoichRow.volume.value = 0.005;
                stoichRow.volume.entered = true;
                stoichRow.weight.value = 2;
                stoichRow.density.value = 0;

                service.onColumnValueChanged(stoichRow, 'density');

                expect(stoichRow.volume.value).toBe(0.005);
                expect(stoichRow.volume.entered).toBeTruthy();
                expect(stoichRow.weight.value).toBe(0);
                expect(stoichRow.mol.value).toBe(0);
            });
        });
    });
});
