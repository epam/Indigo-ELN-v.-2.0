(function() {
    angular
        .module('indigoeln')
        .directive('indigoProductBatchDetails', indigoProductBatchDetails);

    function indigoProductBatchDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/product-batch-details/product-batch-details.html',
            controller: 'IndigoProductBatchDetailsController',
            controllerAs: 'vm',
            bindToController: true,
            scope: {
                model: '=',
                share: '=',
                experiment: '=',
                indigoReadonly: '=readonly'
            }
        };
    }
})();
