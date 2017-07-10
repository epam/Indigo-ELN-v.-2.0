(function() {
    angular
        .module('indigoeln')
        .directive('indigoProductBatchSummary', indigoProductBatchSummary);

    function indigoProductBatchSummary() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/product-batch-summary/product-batch-summary.html',
            controller: 'ProductBatchSummaryController',
            controllerAs: 'vm',
            bindToController: true,
            scope: {
                model: '=',
                share: '=',
                experiment: '=',
                indigoReadonly: '=readonly',
                indigoSaveExperimentFn: '&'
            }
        };
    }
})();
