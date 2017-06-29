(function() {
    angular
        .module('indigoeln')
        .controller('ProductBatchSummaryController', ProductBatchSummaryController);

    /* @ngInject */
    function ProductBatchSummaryController($scope) {
        var vm = this;

        vm.model = $scope.model || {};
        vm.share = $scope.share || {};
        vm.experiment = $scope.experiment;
        vm.indigoSaveExperimentFn = $scope.indigoSaveExperimentFn;
        vm.structureSize = 0.3;

        vm.showStructure = showStructure;

        function showStructure(value) {
            vm.isStructure = value;
        }
    }
})();
