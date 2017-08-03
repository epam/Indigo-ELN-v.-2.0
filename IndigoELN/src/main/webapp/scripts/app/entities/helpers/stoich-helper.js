angular
    .module('indigoeln')
    .factory('stoichHelper', stoichHelperFactory);

/* @ngInject */
function stoichHelperFactory() {
    return {
        cleanReactant: cleanReactant,
        cleanReactants: cleanReactants
    };

    function cleanReactant(batch) {
        return {
            compoundId: batch.compoundId,
            fullNbkBatch: batch.fullNbkBatch,
            molWeight: batch.molWeight,
            formula: batch.formula,
            saltCode: batch.saltCode,
            saltEq: batch.saltEq,
            rxnRole: batch.rxnRole
        };
    }

    function cleanReactants(reactants) {
        return _.map(reactants, cleanReactant);
    }
}
