(function() {
    angular
        .module('indigoeln')
        .directive('indigoPreferredCompoundsSummary', indigoPreferredCompoundsSummary);

    function indigoPreferredCompoundsSummary() {
        return {
            restrict: 'E',
            replace: true,
            controller: controller,
            controllerAs: 'vm',
            templateUrl: 'scripts/components/entities/template/components/prefer-compounds-summary/prefer-compound-summary.html'
        };

        /* @ngInject */
        function controller($scope) {
            var vm = this;

            vm.model = $scope.model || {};
            vm.share = $scope.share || {};
            vm.experiment = $scope.experiment || {};
            vm.structureSize = 0.3;

            vm.showStructure = showStructure;

            function showStructure(value) {
                vm.isStructure = value;
            }
        }
    }
})();
