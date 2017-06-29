(function() {
    angular
        .module('indigoeln')
        .controller('ProductBatchSummarySetSourceController', ProductBatchSummarySetSourceController);

    /* @ngInject */
    function ProductBatchSummarySetSourceController($uibModalInstance, name, sourceValues, sourceDetailExternal, sourceDetailInternal) {
        var vm = this;

        vm.name = name;
        vm.sourceValues = sourceValues;
        vm.source = sourceValues[0];
        vm.sourceDetailExternal = sourceDetailExternal;
        vm.sourceDetailInternal = sourceDetailInternal;

        vm.save = save;
        vm.clear = clear;

        function save() {
            $uibModalInstance.close({
                source: vm.source, sourceDetail: vm.sourceDetail
            });
        }

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }
    }
})();
