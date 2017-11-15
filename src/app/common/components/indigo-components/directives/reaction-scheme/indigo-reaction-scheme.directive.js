var template = require('./reaction-scheme.html');

function indigoReactionScheme() {
    return {
        restrict: 'E',
        template: template,
        controller: IndigoReactionSchemeController,
        controllerAs: 'vm',
        bindToController: true,
        scope: {
            model: '=',
            reaction: '=',
            reactionTrigger: '=',
            experiment: '=',
            isReadonly: '=',
            onChanged: '&'
        }
    };
}

IndigoReactionSchemeController.$inject = ['$scope', '$rootScope', 'calculationService', '$q', 'notifyService',
    'appValuesService'];

function IndigoReactionSchemeController($scope, $rootScope, calculationService, $q, notifyService, appValuesService) {
    var vm = this;

    init();

    function init() {
        vm.onChangedStructure = onChangedStructure;
        vm.modelTrigger = 0;

        checkReaction();
        bindEvents();
    }

    function checkReaction() {
        if (_.get(vm.model.reaction, 'molfile') && !_.has(vm.model.reaction, 'molReactants')) {
            onChangedStructure(vm.model.reaction).then(function() {
                notifyService.info('The old version of reaction scheme is updated. ' +
                    'Save Experiment for apply changes');
            });
        }
    }

    function bindEvents() {
        $scope.$on('new-reaction-scheme', function(event, reactionData) {
            if (reactionData.structure !== _.get(vm.model.reaction, 'molfile')) {
                vm.model.reaction.molfile = reactionData.structure;
                vm.model.reaction.image = null;
                vm.modelTrigger++;
            }
        });
    }

    function getInfo(molfiles) {
        return $q.all(_.map(molfiles, calculationService.getMoleculeInfo));
    }

    function updateReaction(response) {
        vm.model.reaction.molfile = response.structure;
        vm.model.reaction.molReactants = response.reactants;
        vm.model.reaction.molProducts = response.products;

        return $q.all([
            getInfo(vm.model.reaction.molReactants),
            getInfo(vm.model.reaction.molProducts)
        ]).then(function(results) {
            vm.model.reaction.infoReactants = moleculeInfoResponseCallback(results[0]);
            vm.model.reaction.infoProducts = moleculeInfoResponseCallback(results[1], true);
            $rootScope.$broadcast('REACTION_CHANGED', vm.model.reaction);
            vm.onChanged();
        });
    }

    function updateImage(structure) {
        if (structure.image !== vm.model.reaction.image) {
            vm.model.reaction.image = structure.image;
        }
    }

    function onChangedStructure(structure) {
        if (!structure.molfile) {
            vm.model.reaction = structure;
        }
        updateImage(structure);

        if (structure.molfile !== vm.model.reaction.molfile) {
            vm.loading = calculationService.getReactionProductsAndReactants(structure.molfile)
                .then(function(response) {
                    return updateReaction(response, structure);
                });
        }

        return vm.loading;
    }

    function moleculeInfoResponseCallback(results, isProduct) {
        if (!results || results.length === 0) {
            return null;
        }

        return _.map(results, function(result, index) {
            var batch = appValuesService.getDefaultBatch();
            batch.chemicalName = isProduct ? getDefaultChemicalName(index) : result.name;
            batch.formula = result.molecularFormula;
            batch.molWeight.value = result.molecularWeight;
            batch.exactMass = result.exactMolecularWeight;
            batch.structure = {
                image: result.image,
                molfile: result.molecule,
                structureType: 'molecule'
            };

            return batch;
        });
    }

    function getDefaultChemicalName(index) {
        return 'P' + index;
    }
}

module.exports = indigoReactionScheme;
