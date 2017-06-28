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
                share: '=',
                experimentName: '=',
                structureSize: '=',
                isHideColumnSettings: '=',
                onShowStructure: '&'
            },
            bindToController: true,
            controllerAs: 'vm',
            controller: 'IndigoCompoundSummaryController'
        };
    }
})();
