(function() {
    angular
        .module('indigoeln')
        .directive('indigoBatchSummary', indigoBatchSummary);

    function indigoBatchSummary() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/common/batch-summary/batch-summary.html',
            scope: {
                model: '=',
                batches: '=',
                isReadonly: '=',
                // TODO: experimentName is not used
                experimentName: '=',
                isHideColumnSettings: '=',
                structureSize: '=',
                selectedBatch: '=',
                selectedBatchTrigger: '=',
                stoichTable: '=',
                onShowStructure: '&',
                saveExperimentFn: '&',
                onAddedBatch: '&',
                onSelectBatch: '&',
                onRemoveBatches: '&'
            },
            bindToController: true,
            controller: 'IndigoBatchSummaryController',
            controllerAs: 'vm'
        };
    }
})();