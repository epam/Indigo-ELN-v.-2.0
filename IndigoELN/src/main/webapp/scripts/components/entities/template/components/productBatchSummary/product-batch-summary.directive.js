'use strict';

angular.module('indigoeln')
    .directive('productBatchSummary', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'ProductBatchSummaryController',
            templateUrl: 'scripts/components/entities/template/components/productBatchSummary/product-batch-summary.html'
        };
    });