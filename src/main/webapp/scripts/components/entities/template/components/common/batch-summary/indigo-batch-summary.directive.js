(function () {
    angular
        .module('indigoeln')
        .directive('indigoBatchSummary', indigoBatchSummary);

    function indigoBatchSummary() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/common/batch-summary/batch-summary.html',
            scope: {
                model: '=',
                share: '=',
                experimentName: '=',
                isHideColumnSettings: '=',
                structureSize: '=',
                onShowStructure: '&'
            },
            controller: 'IndigoBatchSummaryController'
        };
    }
})();