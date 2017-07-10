(function() {
    angular
        .module('indigoeln')
        .directive('indigoPreferredCompoundsSummary', indigoPreferredCompoundsSummary);

    function indigoPreferredCompoundsSummary() {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                batches: '=',
                model: '=',
                experiment: '=',
                share: '='
            },
            controller: controller,
            controllerAs: 'vm',
            templateUrl: 'scripts/components/entities/template/components/prefer-compounds-summary/prefer-compound-summary.html'
        };

        /* @ngInject */
        function controller() {
            var vm = this;

            vm.model = vm.model || {};
            vm.share = vm.share || {};
            vm.experiment = vm.experiment || {};
            vm.structureSize = 0.3;

            vm.showStructure = showStructure;

            function showStructure(value) {
                vm.isStructure = value;
            }
        }
    }
})();
