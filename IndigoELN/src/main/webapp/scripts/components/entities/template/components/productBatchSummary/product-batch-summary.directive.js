(function () {
    angular
        .module('indigoeln')
        .directive('indigoProductBatchSummary', indigoProductBatchSummary);

    function indigoProductBatchSummary() {
        return {
            restrict: 'E',
            replace: true,
            controller: 'ProductBatchSummaryController',
            templateUrl: 'scripts/components/entities/template/components/productBatchSummary/product-batch-summary.html'
        };
    }
})();