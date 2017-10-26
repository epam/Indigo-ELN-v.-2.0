(function() {
    angular
        .module('indigoeln.Components')
        .directive('indigoProductBatchSummary', indigoProductBatchSummary);

    function indigoProductBatchSummary() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/indigo-components/product-batch-summary/product-batch-summary.html',
            controller: ProductBatchSummaryController,
            controllerAs: 'vm',
            bindToController: true,
            scope: {
                batches: '=',
                selectedBatch: '=',
                batchOperation: '=',
                isExistStoichTable: '=',
                selectedBatchTrigger: '&',
                experiment: '=',
                isReadonly: '=',
                saveExperimentFn: '&',
                onAddedBatch: '&',
                onSelectBatch: '&',
                onRemoveBatches: '&',
                onChanged: '&'
            }
        };

        function ProductBatchSummaryController() {
            var vm = this;

            vm.structureSize = 0.3;

            vm.isStructureVisible = false;
        }
    }
})();
