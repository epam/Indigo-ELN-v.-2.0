angular
    .module('indigoeln')
    .controller('editExternalSupplierController', editExternalSupplierController);

/* @ngInject */
function editExternalSupplierController($uibModalInstance, data) {
    var vm = this;

    init();

    function init() {
        vm.externalSupplier = data || {};

        vm.externalSupplierCodeAndNameSelect = [
            {name: 'SPP1 - Supplier 1'},
            {name: 'SPP2 - Supplier 2'},
            {name: 'SPP3 - Supplier 3'}];

        vm.resultToString = resultToString;
        vm.cancel = cancel;
        vm.save = save;
    }

    function resultToString() {
        if (vm.externalSupplier.codeAndName && vm.externalSupplier.catalogRegistryNumber) {
            return '<' + vm.externalSupplier.codeAndName.name + '> ' +
                vm.externalSupplier.catalogRegistryNumber;
        }

        return null;
    }

    function save() {
        vm.externalSupplier.asString = resultToString();
        $uibModalInstance.close(vm.externalSupplier);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}
