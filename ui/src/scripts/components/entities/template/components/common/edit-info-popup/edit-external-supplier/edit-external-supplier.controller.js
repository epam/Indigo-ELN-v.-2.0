angular
    .module('indigoeln')
    .controller('EditExternalSupplierController', EditExternalSupplierController);

/* @ngInject */
function EditExternalSupplierController($uibModalInstance, data) {
    var vm = this;

    init();

    function init() {
        vm.externalSupplier = data || {};

        vm.externalSupplierCodeAndNameSelect = [
            {
                name: 'SPP1 - Supplier 1'
            },
            {
                name: 'SPP2 - Supplier 2'
            },
            {
                name: 'SPP3 - Supplier 3'
            }];

        vm.resultToString = resultToString;
        vm.cancel = cancel;
        vm.save = save;
    }

    function resultToString() {
        var res = '';
        if (vm.externalSupplier.codeAndName) {
            res += '<' + vm.externalSupplier.codeAndName.name + '> ';
        }
        if (vm.externalSupplier.catalogRegistryNumber) {
            res += vm.externalSupplier.catalogRegistryNumber;
        }

        return res;
    }

    function save() {
        vm.externalSupplier.asString = resultToString();
        $uibModalInstance.close(vm.externalSupplier);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}
