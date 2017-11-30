/* @ngInject */
function stoichTableHelper() {
    return {
        getPrecursors: getPrecursors,
        getReactantsWithMolfile: getReactantsWithMolfile,
        isReactantWithMolfile: isReactantWithMolfile
    };

    function getPrecursors(rows) {
        var reactants = getReactantsWithMolfile(rows);

        return _.compact(_.map(reactants, function(r) {
            return r.compoundId || r.fullNbkBatch;
        })).join(', ');
    }

    function isReactantWithMolfile(item) {
        return item.structure && item.structure.molfile && isReactant(item);
    }

    function getReactantsWithMolfile(stoichReactants) {
        return _.filter(stoichReactants, function(item) {
            return isReactantWithMolfile(item);
        });
    }

    function isReactant(item) {
        return item.rxnRole.name === 'REACTANT';
    }
}

module.exports = stoichTableHelper;
