(function() {
    angular
        .module('indigoeln.componentsModule')
        .directive('indigoCompoundSummary', indigoCompoundSummary);

    function indigoCompoundSummary() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/app/common/components/indigo-components/common/compound-summary/compound-summary.html',
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
