(function() {
    angular
        .module('indigoeln')
        .controller('ProductBatchSummaryController', ProductBatchSummaryController);

    /* @ngInject */
    function ProductBatchSummaryController() {
        var vm = this;

        vm.model = vm.model || {};
        vm.structureSize = 0.3;

        vm.showStructure = showStructure;

        function showStructure(value) {
            vm.isStructure = value;
        }
    }
})();
