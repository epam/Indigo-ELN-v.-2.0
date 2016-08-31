angular.module('indigoeln')
    .directive('preferredCompoundsSummary', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'PreferCompoundSummaryController',
            templateUrl: 'scripts/components/entities/template/components/preferCompoundsSummary/prefer-compound-summary.html'
        };
    });