angular
    .module('indigoeln')
    .factory('stoichHelper', stoichHelperFactory);

/* @ngInject */
function stoichHelperFactory() {
    return {
        cleanReactants: cleanReactants
    };

    function cleanReactants(reactants) {
        return _.map(reactants, function(batch) {
            return {
                compoundId: batch.compoundId,
                fullNbkBatch: batch.fullNbkBatch,
                molWeight: batch.molWeight,
                formula: batch.formula,
                saltCode: batch.saltCode,
                saltEq: batch.saltEq,
                rxnRole: batch.rxnRole
            };
        });
    }
}
