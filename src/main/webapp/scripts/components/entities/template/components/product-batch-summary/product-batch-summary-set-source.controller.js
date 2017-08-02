(function() {
    angular
        .module('indigoeln')
        .controller('ProductBatchSummarySetSourceController', ProductBatchSummarySetSourceController);

    /* @ngInject */
    function ProductBatchSummarySetSourceController($uibModalInstance, name, $q, Dictionary) {
        var vm = this;
        var sources = Dictionary.getByName({name: 'Source'}).$promise;
        var sourceDetails = Dictionary.getByName({name: 'Source Details'}).$promise;
        $q.all([sources, sourceDetails]).then(function(results) {
            vm.name = name;
            vm.sourceValues = results[0].words;
            vm.sourceDetails = results[1].words;
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
        });
    }
})();
