(function() {
    angular
        .module('indigoeln')
        .directive('indigoProductBatchSummary', indigoProductBatchSummary);

    function indigoProductBatchSummary() {
        return {
            restrict: 'E',
            replace: true,
            controller: 'ProductBatchSummaryController',
            controllerAs: 'vm',
            templateUrl: 'scripts/components/entities/template/components/product-batch-summary/product-batch-summary.html'
        };
    }
})();
