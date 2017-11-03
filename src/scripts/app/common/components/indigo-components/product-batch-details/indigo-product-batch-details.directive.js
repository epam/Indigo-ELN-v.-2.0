(function() {
    angular
        .module('indigoeln.componentsModule')
        .directive('indigoProductBatchDetails', indigoProductBatchDetails);

    function indigoProductBatchDetails() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/app/common/components/indigo-components/product-batch-details/product-batch-details.html',
            controller: 'IndigoProductBatchDetailsController',
            controllerAs: 'vm',
            bindToController: true,
            scope: {
                model: '=',
                batches: '=',
                experiment: '=',
                selectedBatch: '=',
                selectedBatchTrigger: '=',
                reactants: '=',
                reactantsTrigger: '=',
                isReadonly: '=',
                saveExperimentFn: '&',
                batchOperation: '=',
                onAddedBatch: '&',
                onSelectBatch: '&',
                onRemoveBatches: '&',
                onChanged: '&'
            }
        };
    }
})();
