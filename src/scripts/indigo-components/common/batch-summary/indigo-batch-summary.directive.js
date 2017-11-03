(function() {
    angular
        .module('indigoeln.componentsModule')
        .directive('indigoBatchSummary', indigoBatchSummary);

    function indigoBatchSummary() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/indigo-components/common/batch-summary/batch-summary.html',
            scope: {
                batches: '=',
                isReadonly: '=',
                isHideColumnSettings: '=',
                structureSize: '=',
                selectedBatch: '=',
                selectedBatchTrigger: '=',
                isExistStoichTable: '=',
                batchOperation: '=',
                onShowStructure: '&',
                saveExperimentFn: '&',
                onAddedBatch: '&',
                onSelectBatch: '&',
                onRemoveBatches: '&',
                onChanged: '&'
            },
            bindToController: true,
            controller: 'IndigoBatchSummaryController',
            controllerAs: 'vm'
        };
    }
})();
