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
                share: '=',
                // TODO: experimentName is not used
                experimentName: '=',
                isHideColumnSettings: '=',
                structureSize: '=',
                onShowStructure: '&',
                indigoSaveExperimentFn: '&'
            },
            bindToController: true,
            controller: 'IndigoBatchSummaryController',
            controllerAs: 'vm'
        };
    }
})();
