var template = require('./product-batch-summary.html');

function indigoProductBatchSummary() {
    return {
        restrict: 'E',
        template: template,
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

module.export = indigoProductBatchSummary;

