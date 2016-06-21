angular.module('indigoeln')
    .factory('AppValues', function () {
        var grams = ['mg', 'g', 'kg'];
        var liters = ['ul', 'ml', 'l'];
        var moles = ['umol', 'mmol', 'mol'];
        var density = ['g/mL'];
        var molarity = ['mM', 'M'];
        var rxnValues = [{name: 'REACTANT'}, {name: 'REAGENT'}, {name: 'SOLVENT'}];
        var compoundValues = [{name: 'Solid'}, {name: 'Glass'}, {name: 'Gum'}, {name: 'Mix'}, {name: 'Liquid/Oil'}, {name: 'Solution'}];
        var sourceValues = [{name: 'Internal'}, {name: 'External'}];
        var sourceDetailExternal = [{name: 'External group 1'}, {name: 'External group 2'}, {name: 'External group 3'}];
        var sourceDetailInternal = [{name: 'Internal group 1'}, {name: 'Internal group 2'}, {name: 'Internal group 3'}];
        var compoundProtectionValues = [{name: 'NONE - None'}, {name: 'ST1 - Standard 1'}, {name: 'ST2 - Standard 2'}];
        var loadFactorUnits = ['mmol/g'];
        var saltCodeValues = [
            {name: '00 - Parent Structure', value: '0'},
            {name: '01 - HYDROCHLORIDE', value: '1'},
            {name: '02 - SODIUM', value: '2'},
            {name: '03 - HYDRATE', value: '3'},
            {name: '04 - HYDROBROMIDE', value: '4'},
            {name: '05 - HYDROIODIDE', value: '5'},
            {name: '06 - POTASSIUM', value: '6'},
            {name: '07 - CALCIUM', value: '7'},
            {name: '08 - SULFATE', value: '8'},
            {name: '09 - PHOSPHATE', value: '9'},
            {name: '10 - CITRATE', value: '10'}];
        var stereoisomerValues = [
            {name: 'NOSTC - Achiral - No Stereo Centers'},
            {name: 'AMESO - Achiral - Meso Stereomers'},
            {name: 'CISTR - Achiral - Cis/Trans Stereomers'},
            {name: 'SNENK - Single Enantiomer (chirality known)'},
            {name: 'RMCMX - Racemic (stereochemistry known)'},
            {name: 'ENENK - Enantio-Enriched (chirality known)'},
            {name: 'DSTRK - Diastereomers (stereochemistry known)'},
            {name: 'SNENU - Other - Single Enantiomer (chirality unknown)'}];
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
            saltCode: {name: '00 - Parent Structure', value: '0', entered: false},
            molWeight: {value: 0, entered: false},
            stoicPurity: {value: 100, entered: false},
            saltEq: {value: 0, entered: false},
            eq: {value: 1, entered: false},
            yield: 0
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
            getRxnValues: function () {
                return rxnValues;
            },
            getCompoundValues: function () {
                return compoundValues;
            },
            getSaltCodeValues: function () {
                return saltCodeValues;
            },
            getStereoisomerValues: function () {
                return stereoisomerValues;
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
                return defaultBatch;
            }
        };
    });