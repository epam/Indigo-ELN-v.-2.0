(function () {
    angular
        .module('indigoeln')
        .directive('indigoPreferredCompoundsSummary', indigoPreferredCompoundsSummary);

    function indigoPreferredCompoundsSummary() {
        return {
            restrict: 'E',
            replace: true,
            controller: 'PreferCompoundSummaryController',
            templateUrl: 'scripts/components/entities/template/components/prefer-compounds-summary/prefer-compound-summary.html'
        };
    }
})();