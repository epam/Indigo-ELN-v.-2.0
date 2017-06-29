angular
    .module('indigoeln')
    .controller('SetInputValueController', SetInputValueController);

/* @ngInject */
function SetInputValueController(name, $uibModalInstance) {
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
