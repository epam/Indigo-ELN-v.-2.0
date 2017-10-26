(function() {
    angular
        .module('indigoeln.Components')
        .directive('indigoCompoundSummary', indigoCompoundSummary);

    function indigoCompoundSummary() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/indigo-components/common/compound-summary/compound-summary.html',
            scope: {
                model: '=',
                batches: '=',
                batchesTrigger: '=',
                selectedBatch: '=',
                selectedBatchTrigger: '=',
                experimentName: '=',
                structureSize: '=',
                isHideColumnSettings: '=',
                isReadonly: '=',
                batchOperation: '=',
                onShowStructure: '&',
                onAddedBatch: '&',
                onSelectBatch: '&',
                onRemoveBatches: '&',
                onChanged: '&'
            },
            bindToController: true,
            controllerAs: 'vm',
            controller: 'IndigoCompoundSummaryController'
        };
    }
})();
