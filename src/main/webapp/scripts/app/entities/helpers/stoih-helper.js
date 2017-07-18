angular
    .module('indigoeln')
    .factory('stoihHelper', stoihHelperFactory);

/* @ngInject */
function stoihHelperFactory() {
    return {
        cleanReactants: cleanReactants
    };

    function cleanReactants(reactants) {
        return reactants.map(function(batch) {
            return {
                compoundId: batch.compoundId,
                fullNbkBatch: batch.fullNbkBatch,
                molWeight: batch.molWeight,
                formula: batch.formula,
                saltCode: batch.saltCode,
                saltEq: batch.saltEq
            };
        });
    }
}
