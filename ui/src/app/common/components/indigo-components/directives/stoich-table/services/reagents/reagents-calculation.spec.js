/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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

    beforeEach(angular.mock.module('indigoeln.indigoComponents'));

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
