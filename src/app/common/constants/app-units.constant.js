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

var appUnits = {
    grams: ['mg', 'g', 'kg'],
    liters: ['ml', 'ul', 'l'],
    moles: ['mmol', 'umol', 'mol'],
    density: ['g/mL'],
    molarity: ['M', 'mM'],
    rxnValues: [{name: 'REACTANT'}, {name: 'REAGENT'}, {name: 'SOLVENT'}],
    rxnRoleReactant: {name: 'REACTANT'},
    rxnRoleSolvent: {name: 'SOLVENT'},
    compoundProtectionValues: [{name: 'NONE - None'}, {name: 'ST1 - Standard 1'}, {name: 'ST2 - Standard 2'}],
    loadFactorUnits: ['mmol/g'],
    defaultSaltCode: {name: '00 - Parent Structure', value: '00', regValue: '00', weight: 0},

    defaultBatch: {
        limiting: false,
        weight: {value: 0, unit: 'mg', entered: false},
        volume: {value: 0, unit: 'mL', entered: false},
        density: {value: 0, unit: 'g/mL', entered: false},
        molarity: {value: 0, unit: 'M', entered: false},
        // mol is stoich, totalMoles is batch
        // mol copy to theoMoles
        mol: {value: 0, unit: 'mmol', entered: false},
        loadFactor: {value: 0, unit: '', entered: false},
        theoWeight: {value: 0, unit: '', entered: false},
        theoMoles: {value: 0, unit: '', entered: false},
        totalVolume: {value: 0, unit: 'mL', entered: false},
        totalWeight: {value: 0, unit: 'mg', entered: false},
        totalMoles: {value: 0, unit: 'mmol', entered: false},
        rxnRole: {name: 'REACTANT', entered: false},
        saltCode: {name: '00 - Parent Structure', value: '00', regValue: '00', entered: false},
        molWeight: {value: 0, entered: false},
        structure: {
            image: null,
            molfile: null,
            structureId: null
        },
        stoicPurity: {value: 100, entered: false},
        saltEq: {value: 0, entered: false},
        eq: {value: 1, entered: false},
        yield: 0,
        prevMolarAmount: {value: 0, unit: 'M', entered: false},
        precursors: ''
    },
    operatorSelect: [{name: '>'}, {name: '<'}, {name: '='}, {name: '~'}]
};

module.exports = appUnits;
