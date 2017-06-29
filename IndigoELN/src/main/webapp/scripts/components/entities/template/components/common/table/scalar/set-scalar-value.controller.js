angular
    .module('indigoeln')
    .controller('SetScalarValueController', SetScalarValueController);

/* @ngInject */
function SetScalarValueController(name, $uibModalInstance) {
    var vm = this;

    init();

    function init() {
        vm.name = name;

        vm.save = save;
        vm.clear = clear;
    }

    function save() {
        $uibModalInstance.close(vm.value);
    }

    function clear() {
        $uibModalInstance.dismiss('cancel');
    }
}
