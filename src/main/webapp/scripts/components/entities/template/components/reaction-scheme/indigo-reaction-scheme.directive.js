(function() {
    angular
        .module('indigoeln')
        .directive('indigoReactionScheme', indigoReactionScheme);

    function indigoReactionScheme() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/reaction-scheme/reaction-scheme.html',
            controller: indigoReactionSchemeController,
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

    /* @ngInject */
    function indigoReactionSchemeController($scope, $rootScope, CalculationService, $q, notifyService) {
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
            return $q.all(_.map(molfiles, CalculationService.getMoleculeInfo));
        }

        function updateReaction(response) {
            vm.model.reaction.molfile = response.structure;
            vm.model.reaction.molReactants = response.reactants;
            vm.model.reaction.molProducts = response.products;

            return $q.all([
                getInfo(vm.model.reaction.molReactants),
                getInfo(vm.model.reaction.molProducts)
            ]).then(function(results) {
                vm.model.reaction.infoReactants = results[0];
                vm.model.reaction.infoProducts = results[1];
                $rootScope.$broadcast('REACTION_CHANGED', vm.model.reaction);
                vm.onChanged();
            });
        }

        function onChangedStructure(structure) {
            vm.loading = CalculationService.getReactionProductsAndReactants(structure.molfile).then(function(response) {
                return updateReaction(response, structure);
            });

            return vm.loading;
        }
    }
})();
