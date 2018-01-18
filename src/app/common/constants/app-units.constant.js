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
    defaultSaltCode: {name: '00 - Parent Structure', value: '0', regValue: '00', weight: 0},

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
        saltCode: {name: '00 - Parent Structure', value: '0', regValue: '00', entered: false},
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
