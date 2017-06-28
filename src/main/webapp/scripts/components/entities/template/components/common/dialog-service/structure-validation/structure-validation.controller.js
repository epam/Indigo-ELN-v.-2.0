angular
    .module('indigoeln')
    .controller('StructureValidationController', StructureValidationController);

/* @ngInject */
function StructureValidationController($uibModalInstance, batches, searchQuery, AppValues) {
    var vm = this;

    init();

    function init() {
        vm.batches = batches;
        vm.searchQuery = searchQuery;
        vm.selectedBatch = null;
        vm.defaultSaltCodeName = AppValues.getDefaultSaltCode().name;

        vm.save = save;
        vm.cancel = cancel;
        vm.selectBatch = selectBatch;
    }

    function selectBatch(batch) {
        if (vm.selectedBatch) {
            vm.selectedBatch.$$isSelected = false;
        }
        batch.$$isSelected = true;
        vm.selectedBatch = batch;
    }

    function save() {
        $uibModalInstance.close(vm.selectedBatch);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

