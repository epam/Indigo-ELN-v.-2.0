angular.module('indigoeln')
    .factory('AppValues', function () {
        var grams = ['mg', 'g', 'kg'];
        var liters = ['ml', 'ul', 'l'];
        var moles = ['mmol', 'umol', 'mol'];
        var density = ['g/mL'];
        var molarity = ['M', 'mM'];
        var rxnValues = [{name: 'REACTANT'}, {name: 'REAGENT'}, {name: 'SOLVENT'}];
        var rxnRoleReactant = {name: 'REACTANT'};
        var rxnRoleSolvent = {name: 'SOLVENT'};
        var sourceValues = [{name: 'Internal'}, {name: 'External'}];
        var sourceDetailExternal = [{name: 'External group 1'}, {name: 'External group 2'}, {name: 'External group 3'}];
        var sourceDetailInternal = [{name: 'Internal group 1'}, {name: 'Internal group 2'}, {name: 'Internal group 3'}];
        var compoundProtectionValues = [{name: 'NONE - None'}, {name: 'ST1 - Standard 1'}, {name: 'ST2 - Standard 2'}];
        var loadFactorUnits = ['mmol/g'];
        var defaultSaltCode = {name: '00 - Parent Structure', value: '0', regValue: '00'};
        var saltCodeValues = [
            {name: '00 - Parent Structure', value: '0', regValue: '00'},
            {name: '01 - HYDROCHLORIDE', value: '1', regValue: '01'},
            {name: '02 - SODIUM', value: '2', regValue: '02'},
            {name: '03 - HYDRATE', value: '3', regValue: '03'},
            {name: '04 - HYDROBROMIDE', value: '4', regValue: '04'},
            {name: '05 - HYDROIODIDE', value: '5', regValue: '05'},
            {name: '06 - POTASSIUM', value: '6', regValue: '06'},
            {name: '07 - CALCIUM', value: '7', regValue: '07'},
            {name: '08 - SULFATE', value: '8', regValue: '08'},
            {name: '09 - PHOSPHATE', value: '9', regValue: '09'},
            {name: '10 - CITRATE', value: '10', regValue: '10'}];
        var defaultBatch = {
            limiting: false,
            weight: {value: 0, unit: '', entered: false},
            volume: {value: 0, unit: '', entered: false},
            density: {value: 0, unit: '', entered: false},
            molarity: {value: 0, unit: '', entered: false},
            mol: {value: 0, unit: '', entered: false},
            loadFactor: {value: 0, unit: '', entered: false},
            theoWeight: {value: 0, unit: '', entered: false},
            theoMoles: {value: 0, unit: '', entered: false},
            totalVolume: {value: 0, unit: '', entered: false},
            totalWeight: {value: 0, unit: '', entered: false},
            totalMoles: {value: 0, unit: '', entered: false},
            rxnRole: {name: 'REACTANT', entered: false},
            saltCode: {name: '00 - Parent Structure', value: '0', regValue: '00', entered: false},
            molWeight: {value: 0, entered: false},
            stoicPurity: {value: 100, entered: false},
            saltEq: {value: 0, entered: false},
            eq: {value: 1, entered: false},
            yield: 0,
            prevMolarAmount: {value: 0, unit: '', entered: false}
        };

        return {
            getGrams: function () {
                return grams;
            },
            getLiters: function () {
                return liters;
            },
            getMoles: function () {
                return moles;
            },
            getDensity: function () {
                return density;
            },
            getMolarity: function () {
                return molarity;
            },
            getRxnRoleReactant: function () {
                return rxnRoleReactant;
            },
            getRxnRoleSolvent: function () {
                return rxnRoleSolvent;
            },
            getRxnValues: function () {
                return rxnValues;
            },
            getDefaultSaltCode: function () {
                return defaultSaltCode;
            },
            getSaltCodeValues: function () {
                return saltCodeValues;
            },
            getSourceValues: function () {
                return sourceValues;
            },
            getSourceDetailExternal: function () {
                return sourceDetailExternal;
            },
            getSourceDetailInternal: function () {
                return sourceDetailInternal;
            },
            getCompoundProtectionValues: function () {
                return compoundProtectionValues;
            },
            getLoadFactorUnits: function () {
                return loadFactorUnits;
            },
            getDefaultBatch: function () {
                return angular.copy(defaultBatch);
            }
        };
    });