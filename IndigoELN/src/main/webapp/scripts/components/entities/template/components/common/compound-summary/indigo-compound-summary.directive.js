(function() {
    angular
        .module('indigoeln')
        .directive('indigoCompoundSummary', indigoCompoundSummary);

    function indigoCompoundSummary() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/common/compound-summary/compound-summary.html',
            scope: {
                model: '=',
                batches: '=',
                selectedBatch: '=',
                selectedBatchTrigger: '=',
                experimentName: '=',
                structureSize: '=',
                isHideColumnSettings: '=',
                onShowStructure: '&',
                onAddedBatch: '&',
                onSelectBatch: '&',
                onRemoveBatches: '&'
            },
            bindToController: true,
            controllerAs: 'vm',
            controller: 'IndigoCompoundSummaryController'
        };
    }
})();
